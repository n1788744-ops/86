package com.example.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.MusicMode
import com.example.game.audio.SfxType
import com.example.game.audio.Ww1AudioEngine
import com.example.game.data.GamePreferencesRepository
import com.example.game.data.PlayerProfileState
import com.example.game.engine.BattleOutcome
import com.example.game.engine.BattleSimulation
import com.example.game.engine.HudSnapshot
import com.example.game.model.CampaignChapter
import com.example.game.model.GameCatalog
import com.example.game.model.GameScreen
import com.example.game.model.SoldierClass
import com.example.game.model.UniformStyle
import com.example.game.model.WeaponSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = GamePreferencesRepository(application)
    val audio = Ww1AudioEngine()

    val profile: StateFlow<PlayerProfileState> = repo.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PlayerProfileState()
        )

    private val _currentScreen = MutableStateFlow(GameScreen.MAIN_MENU)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    private val _selectedChapter = MutableStateFlow(GameCatalog.chapters.first())
    val selectedChapter: StateFlow<CampaignChapter> = _selectedChapter.asStateFlow()

    private val _hudSnapshot = MutableStateFlow(HudSnapshot())
    val hudSnapshot: StateFlow<HudSnapshot> = _hudSnapshot.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _activeSimulation = MutableStateFlow<BattleSimulation?>(null)
    val activeSimulation: StateFlow<BattleSimulation?> = _activeSimulation.asStateFlow()

    private var rewardRecordedForCurrentBattle = false

    init {
        audio.start()
        viewModelScope.launch {
            profile.collect { state ->
                audio.musicVolume = state.musicVolume
                audio.sfxVolume = state.sfxVolume
                _activeSimulation.value?.let { sim ->
                    sim.lookSensitivity = state.lookSensitivity
                    sim.aimAssistEnabled = state.aimAssistEnabled
                    sim.invertY = state.invertY
                }
            }
        }
    }

    fun onAppResume() {
        audio.resume()
    }

    fun onAppPause() {
        audio.pause()
        if (_currentScreen.value == GameScreen.BATTLE && _hudSnapshot.value.outcome == BattleOutcome.PLAYING) {
            _isPaused.value = true
        }
    }

    fun navigateTo(screen: GameScreen) {
        audio.playSfx(SfxType.UI_CLICK)
        _currentScreen.value = screen
        audio.musicMode = when (screen) {
            GameScreen.BATTLE -> MusicMode.COMBAT
            else -> MusicMode.MENU
        }
    }

    fun selectChapter(chapter: CampaignChapter) {
        audio.playSfx(SfxType.UI_CLICK)
        _selectedChapter.value = chapter
    }

    fun saveLoadout(
        soldierClass: SoldierClass,
        weapon: WeaponSpec,
        uniform: UniformStyle,
        perkId: String
    ) {
        audio.playSfx(SfxType.UI_CLICK)
        viewModelScope.launch {
            repo.saveLoadout(soldierClass.id, weapon.id, uniform.id, perkId)
        }
    }

    fun updateSettings(
        musicVolume: Float,
        sfxVolume: Float,
        lookSensitivity: Float,
        aimAssist: Boolean,
        invertY: Boolean
    ) {
        audio.musicVolume = musicVolume
        audio.sfxVolume = sfxVolume
        viewModelScope.launch {
            repo.updateSettings(musicVolume, sfxVolume, lookSensitivity, aimAssist, invertY)
        }
    }

    fun startBattle(chapter: CampaignChapter = _selectedChapter.value) {
        audio.playSfx(SfxType.WHISTLE)
        _selectedChapter.value = chapter
        val prof = profile.value
        val sClass = SoldierClass.entries.find { it.id == prof.selectedClassId } ?: SoldierClass.RIFLEMAN
        val weapon = GameCatalog.weapons[prof.selectedWeaponId]
            ?: GameCatalog.weapons[sClass.defaultWeaponId]!!
        val uniform = UniformStyle.entries.find { it.id == prof.selectedUniformId } ?: sClass.defaultUniform

        val sim = BattleSimulation(
            chapter = chapter,
            soldierClass = sClass,
            weapon = weapon,
            uniform = uniform,
            perkId = prof.selectedPerkId,
            audio = audio
        ).apply {
            lookSensitivity = prof.lookSensitivity
            aimAssistEnabled = prof.aimAssistEnabled
            invertY = prof.invertY
        }

        rewardRecordedForCurrentBattle = false
        _isPaused.value = false
        _activeSimulation.value = sim
        _hudSnapshot.value = sim.snapshot()
        _currentScreen.value = GameScreen.BATTLE
        audio.musicMode = MusicMode.COMBAT
    }

    fun togglePause() {
        audio.playSfx(SfxType.UI_CLICK)
        _isPaused.value = !_isPaused.value
    }

    fun exitBattleToMenu(target: GameScreen = GameScreen.MAIN_MENU) {
        audio.playSfx(SfxType.UI_CLICK)
        _isPaused.value = false
        _activeSimulation.value = null
        _currentScreen.value = target
        audio.musicMode = MusicMode.MENU
    }

    fun onHudSnapshotFromRenderer(snapshot: HudSnapshot) {
        _hudSnapshot.value = snapshot
        if (snapshot.outcome != BattleOutcome.PLAYING && !rewardRecordedForCurrentBattle) {
            rewardRecordedForCurrentBattle = true
            if (snapshot.outcome == BattleOutcome.VICTORY) {
                audio.musicMode = MusicMode.VICTORY
            }
            val wonIndex = if (snapshot.outcome == BattleOutcome.VICTORY) _selectedChapter.value.index else null
            viewModelScope.launch {
                repo.recordBattleReward(
                    earnedXp = snapshot.earnedXp,
                    earnedStars = snapshot.earnedStars,
                    chapterCompletedIndex = wonIndex
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audio.stop()
    }
}
