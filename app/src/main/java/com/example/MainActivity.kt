package com.example

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.game.model.GameScreen
import com.example.game.model.TheatreBiome
import com.example.game.render3d.BattleGLRenderer
import com.example.game.viewmodel.GameViewModel
import com.example.ui.screens.BattleHudOverlay
import com.example.ui.screens.CampaignScreen
import com.example.ui.screens.ClassLoadoutScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.Ww1Theme

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ww1Theme {
                Ww1GameRoot(viewModel = gameViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.onAppResume()
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.onAppPause()
    }
}

@Composable
private fun Ww1GameRoot(viewModel: GameViewModel) {
    val profile by viewModel.profile.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val hudSnapshot by viewModel.hudSnapshot.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val activeSim by viewModel.activeSimulation.collectAsState()

    val context = LocalContext.current
    val battleRenderer = remember {
        BattleGLRenderer(onHudSnapshot = { snap ->
            viewModel.onHudSnapshotFromRenderer(snap)
        })
    }

    // Keep renderer synchronized with current mode & simulation
    battleRenderer.simulation = activeSim
    battleRenderer.isMenuPreviewMode = (currentScreen != GameScreen.BATTLE) || isPaused
    battleRenderer.menuBiome = selectedChapter.theatre

    Box(modifier = Modifier.fillMaxSize()) {
        // Shared live 3D OpenGL ES Battlefield (orbits behind MainMenu, renders full FPS in Battle!)
        if (currentScreen == GameScreen.MAIN_MENU || currentScreen == GameScreen.BATTLE) {
            val glView = remember(context) {
                GLSurfaceView(context).apply {
                    setEGLContextClientVersion(3)
                    setRenderer(battleRenderer)
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                }
            }
            DisposableEffect(glView) {
                glView.onResume()
                onDispose {
                    glView.onPause()
                }
            }
            AndroidView(
                factory = { glView },
                modifier = Modifier.fillMaxSize()
            )
        }

        when (currentScreen) {
            GameScreen.MAIN_MENU -> {
                MainMenuScreen(
                    profile = profile,
                    onOpenCampaign = { viewModel.navigateTo(GameScreen.CAMPAIGN) },
                    onOpenClasses = { viewModel.navigateTo(GameScreen.CLASS_LOADOUT) },
                    onQuickBattle = { viewModel.startBattle(selectedChapter) },
                    onOpenSettings = { viewModel.navigateTo(GameScreen.SETTINGS) }
                )
            }
            GameScreen.CAMPAIGN -> {
                CampaignScreen(
                    profile = profile,
                    selectedChapter = selectedChapter,
                    onSelectChapter = { viewModel.selectChapter(it) },
                    onLaunchBattle = { viewModel.startBattle(it) },
                    onOpenClasses = { viewModel.navigateTo(GameScreen.CLASS_LOADOUT) },
                    onBack = { viewModel.navigateTo(GameScreen.MAIN_MENU) }
                )
            }
            GameScreen.CLASS_LOADOUT -> {
                ClassLoadoutScreen(
                    profile = profile,
                    onSaveLoadout = { sClass, weapon, uniform, perkId ->
                        viewModel.saveLoadout(sClass, weapon, uniform, perkId)
                    },
                    onDeployBattle = { sClass, weapon, uniform, perkId ->
                        viewModel.saveLoadout(sClass, weapon, uniform, perkId)
                        viewModel.startBattle(selectedChapter)
                    },
                    onBack = { viewModel.navigateTo(GameScreen.MAIN_MENU) }
                )
            }
            GameScreen.SETTINGS -> {
                SettingsScreen(
                    profile = profile,
                    onUpdateSettings = { m, s, sens, aa, inv ->
                        viewModel.updateSettings(m, s, sens, aa, inv)
                    },
                    onBack = { viewModel.navigateTo(GameScreen.MAIN_MENU) }
                )
            }
            GameScreen.BATTLE -> {
                val sim = activeSim
                if (sim != null) {
                    BattleHudOverlay(
                        simulation = sim,
                        hud = hudSnapshot,
                        isPaused = isPaused,
                        onTogglePause = { viewModel.togglePause() },
                        onRestart = { viewModel.startBattle(selectedChapter) },
                        onSwitchClass = { viewModel.exitBattleToMenu(GameScreen.CLASS_LOADOUT) },
                        onExitToMenu = { viewModel.exitBattleToMenu(GameScreen.MAIN_MENU) }
                    )
                }
            }
        }
    }
}
