package com.example.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "front_1916_prefs")

data class PlayerProfileState(
    val level: Int = 1,
    val xp: Int = 0,
    val stars: Int = 250,
    val completedChaptersCount: Int = 0,
    val selectedClassId: String = "rifleman",
    val selectedWeaponId: String = "lebel_1886",
    val selectedUniformId: String = "horizon_blue",
    val selectedPerkId: String = "hardened",
    val musicVolume: Float = 0.72f,
    val sfxVolume: Float = 0.85f,
    val lookSensitivity: Float = 1.0f,
    val aimAssistEnabled: Boolean = true,
    val invertY: Boolean = false
) {
    val xpForNextLevel: Int get() = 400 + (level - 1) * 200
}

class GamePreferencesRepository(private val context: Context) {

    private object Keys {
        val LEVEL = intPreferencesKey("player_level")
        val XP = intPreferencesKey("player_xp")
        val STARS = intPreferencesKey("player_stars")
        val COMPLETED_CHAPTERS = intPreferencesKey("completed_chapters")
        val SELECTED_CLASS = stringPreferencesKey("selected_class")
        val SELECTED_WEAPON = stringPreferencesKey("selected_weapon")
        val SELECTED_UNIFORM = stringPreferencesKey("selected_uniform")
        val SELECTED_PERK = stringPreferencesKey("selected_perk")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val LOOK_SENSITIVITY = floatPreferencesKey("look_sensitivity")
        val AIM_ASSIST = booleanPreferencesKey("aim_assist")
        val INVERT_Y = booleanPreferencesKey("invert_y")
    }

    val profileFlow: Flow<PlayerProfileState> = context.gameDataStore.data.map { prefs ->
        PlayerProfileState(
            level = prefs[Keys.LEVEL] ?: 1,
            xp = prefs[Keys.XP] ?: 0,
            stars = prefs[Keys.STARS] ?: 250,
            completedChaptersCount = prefs[Keys.COMPLETED_CHAPTERS] ?: 0,
            selectedClassId = prefs[Keys.SELECTED_CLASS] ?: "rifleman",
            selectedWeaponId = prefs[Keys.SELECTED_WEAPON] ?: "lebel_1886",
            selectedUniformId = prefs[Keys.SELECTED_UNIFORM] ?: "horizon_blue",
            selectedPerkId = prefs[Keys.SELECTED_PERK] ?: "hardened",
            musicVolume = prefs[Keys.MUSIC_VOLUME] ?: 0.72f,
            sfxVolume = prefs[Keys.SFX_VOLUME] ?: 0.85f,
            lookSensitivity = prefs[Keys.LOOK_SENSITIVITY] ?: 1.0f,
            aimAssistEnabled = prefs[Keys.AIM_ASSIST] ?: true,
            invertY = prefs[Keys.INVERT_Y] ?: false
        )
    }

    suspend fun saveLoadout(
        classId: String,
        weaponId: String,
        uniformId: String,
        perkId: String
    ) {
        context.gameDataStore.edit { prefs ->
            prefs[Keys.SELECTED_CLASS] = classId
            prefs[Keys.SELECTED_WEAPON] = weaponId
            prefs[Keys.SELECTED_UNIFORM] = uniformId
            prefs[Keys.SELECTED_PERK] = perkId
        }
    }

    suspend fun updateSettings(
        musicVolume: Float,
        sfxVolume: Float,
        lookSensitivity: Float,
        aimAssist: Boolean,
        invertY: Boolean
    ) {
        context.gameDataStore.edit { prefs ->
            prefs[Keys.MUSIC_VOLUME] = musicVolume.coerceIn(0f, 1f)
            prefs[Keys.SFX_VOLUME] = sfxVolume.coerceIn(0f, 1f)
            prefs[Keys.LOOK_SENSITIVITY] = lookSensitivity.coerceIn(0.4f, 2.2f)
            prefs[Keys.AIM_ASSIST] = aimAssist
            prefs[Keys.INVERT_Y] = invertY
        }
    }

    suspend fun recordBattleReward(
        earnedXp: Int,
        earnedStars: Int,
        chapterCompletedIndex: Int?
    ) {
        context.gameDataStore.edit { prefs ->
            var level = prefs[Keys.LEVEL] ?: 1
            var xp = (prefs[Keys.XP] ?: 0) + earnedXp
            while (xp >= 400 + (level - 1) * 200) {
                xp -= (400 + (level - 1) * 200)
                level += 1
            }
            prefs[Keys.LEVEL] = level
            prefs[Keys.XP] = xp
            prefs[Keys.STARS] = (prefs[Keys.STARS] ?: 250) + earnedStars
            if (chapterCompletedIndex != null) {
                val prev = prefs[Keys.COMPLETED_CHAPTERS] ?: 0
                if (chapterCompletedIndex + 1 > prev) {
                    prefs[Keys.COMPLETED_CHAPTERS] = chapterCompletedIndex + 1
                }
            }
        }
    }
}
