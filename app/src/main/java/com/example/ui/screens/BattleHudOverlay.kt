package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.game.engine.BattleOutcome
import com.example.game.engine.BattleSimulation
import com.example.game.engine.HudSnapshot
import com.example.ui.components.Ww1CommandButton
import com.example.ui.theme.AshGrey
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassMuted
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.HorizonBlue
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlatePanel
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TrenchBlack
import kotlin.math.sqrt

@Composable
fun BattleHudOverlay(
    simulation: BattleSimulation,
    hud: HudSnapshot,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onRestart: () -> Unit,
    onSwitchClass: () -> Unit,
    onExitToMenu: () -> Unit
) {
    BackHandler(onBack = onTogglePause)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Damage flash radial vignette
        if (hud.damageFlashAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                CrimsonAlert.copy(alpha = hud.damageFlashAlpha * 0.55f)
                            )
                        )
                    )
            )
        }

        // 2. Sniper 3.5x Scope Optic Overlay when scoped
        if (hud.isScoped && !isPaused && hud.outcome == BattleOutcome.PLAYING) {
            SniperScopeOverlay(modifier = Modifier.fillMaxSize())
        }

        // 3. Fullscreen Touch-Look Aim Surface (captures drags across center/right view)
        if (!isPaused && hud.outcome == BattleOutcome.PLAYING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(simulation) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break
                                val delta = change.positionChange()
                                if (delta != Offset.Zero) {
                                    simulation.applyLookDelta(delta.x, delta.y)
                                    change.consume()
                                }
                            }
                        }
                    }
            )
        }

        // 4. Center Reticle & Hitmarker
        if (!hud.isScoped && !isPaused && hud.outcome == BattleOutcome.PLAYING) {
            CenterCrosshair(
                isAds = hud.isAds,
                hitMarkerAlpha = hud.hitMarkerAlpha,
                killMarker = hud.killMarker,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 5. Safe-area HUD & Ergonomic Thumb Controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(10.dp)
        ) {
            // Top Tactical Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Objective & Kills Badge
                    Surface(
                        color = TrenchBlack.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, BrassMuted.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = hud.chapterTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrassGold
                            )
                            Text(
                                text = hud.objectiveText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ParchmentIvory,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Pause button (48dp+)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TrenchBlack.copy(alpha = 0.75f))
                            .border(1.dp, SlateBorder, RoundedCornerShape(4.dp))
                            .testTag("battle_pause_button")
                            .clickable(onClick = onTogglePause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = "Пауза",
                            tint = ParchmentIvory
                        )
                    }
                }

                // Active Sector Capture Bar
                if (hud.activeSectorName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = TrenchBlack.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, HorizonBlue),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .widthIn(max = 260.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ЗАХВАТ: ${hud.activeSectorName} (${(hud.activeSectorProgress * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = HorizonBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { hud.activeSectorProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = HorizonBlue,
                                trackColor = SlatePanel
                            )
                        }
                    }
                }

                // Battle Feed Dispatch Banner
                if (hud.battleFeedText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = hud.battleFeedText,
                        style = MaterialTheme.typography.labelLarge,
                        color = BrassGold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .background(TrenchBlack.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom Mobile Controls + Vitals Bar
            if (!isPaused && hud.outcome == BattleOutcome.PLAYING) {
                // Left Virtual Joystick
                VirtualJoystick(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 6.dp, bottom = 12.dp),
                    onMove = { mx, my ->
                        simulation.moveInputX = mx
                        simulation.moveInputY = my
                    }
                )

                // Bottom-Center Vitals & Ammo Strip
                Surface(
                    color = TrenchBlack.copy(alpha = 0.76f),
                    border = BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Здоровье",
                                tint = if (hud.health > 35) TacticalGreen else CrimsonAlert,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${hud.health}",
                                style = MaterialTheme.typography.titleMedium,
                                color = ParchmentIvory,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(text = "|", color = SlateBorder)

                        Text(
                            text = if (hud.isReloading) "ПЕРЕЗАРЯДКА..." else "${hud.ammoInMag} / ${hud.reserveAmmo}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (hud.ammoInMag <= 2) BrassGold else ParchmentIvory,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right Thumb Combat Cluster (Fire, ADS, Reload, Crouch, Special)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 4.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TacticalCircleButton(
                            label = if (hud.isCrouching) "СТОЯТЬ" else "ОКОП",
                            icon = Icons.Filled.ArrowDownward,
                            isActive = hud.isCrouching,
                            size = 52.dp,
                            onClick = { simulation.isCrouching = !simulation.isCrouching }
                        )
                        TacticalCircleButton(
                            label = "ПЕРЕЗ.",
                            icon = Icons.Filled.Refresh,
                            isActive = hud.isReloading,
                            progress = if (hud.isReloading) hud.reloadProgress else 1f,
                            size = 52.dp,
                            onClick = { simulation.startReload() }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TacticalCircleButton(
                            label = hud.specialNameRu.take(7),
                            icon = Icons.Filled.FlashOn,
                            isActive = hud.specialReadyRatio >= 0.99f,
                            progress = hud.specialReadyRatio,
                            size = 54.dp,
                            onClick = { simulation.triggerSpecialAbility() }
                        )

                        TacticalCircleButton(
                            label = "ПРИЦЕЛ",
                            icon = Icons.Filled.CenterFocusStrong,
                            isActive = hud.isAds,
                            size = 54.dp,
                            onClick = { simulation.isAds = !simulation.isAds }
                        )

                        // Primary FIRE button (supports tap + hold for automatics)
                        FireTriggerButton(
                            onFirePress = {
                                simulation.isFiringHeld = true
                                simulation.triggerSingleShot()
                            },
                            onFireRelease = {
                                simulation.isFiringHeld = false
                            }
                        )
                    }
                }
            }
        }

        // 6. Pause / Victory / Defeat Dispatch Modal
        if (isPaused || hud.outcome != BattleOutcome.PLAYING) {
            BattleModalOverlay(
                hud = hud,
                isPaused = isPaused,
                onResume = onTogglePause,
                onRestart = onRestart,
                onSwitchClass = onSwitchClass,
                onExitToMenu = onExitToMenu
            )
        }
    }
}

@Composable
private fun VirtualJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val outerRadiusPx = 120f

    Box(
        modifier = modifier
            .size(124.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxR = (size.width / 2f) * 0.72f

                    fun update(pos: Offset) {
                        val diff = pos - center
                        val dist = sqrt(diff.x * diff.x + diff.y * diff.y)
                        val clamped = if (dist > maxR) diff * (maxR / dist) else diff
                        knobOffset = clamped
                        onMove(clamped.x / maxR, -clamped.y / maxR)
                    }

                    update(down.position)
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        update(change.position)
                        change.consume()
                    }
                    knobOffset = Offset.Zero
                    onMove(0f, 0f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * 0.45f
            drawCircle(
                color = TrenchBlack.copy(alpha = 0.55f),
                radius = r,
                center = c
            )
            drawCircle(
                color = BrassMuted.copy(alpha = 0.65f),
                radius = r,
                center = c,
                style = Stroke(width = 3f)
            )
            // Thumb knob
            drawCircle(
                color = BrassGold.copy(alpha = 0.88f),
                radius = r * 0.44f,
                center = c + knobOffset
            )
            drawCircle(
                color = TrenchBlack.copy(alpha = 0.5f),
                radius = r * 0.44f,
                center = c + knobOffset,
                style = Stroke(width = 2.5f)
            )
        }
        // Keep compiler happy with reference
        if (outerRadiusPx < 0f) Unit
    }
}

@Composable
private fun FireTriggerButton(
    onFirePress: () -> Unit,
    onFireRelease: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (pressed) BrassGold else CrimsonAlert.copy(alpha = 0.88f)
            )
            .border(2.dp, BrassGold, CircleShape)
            .testTag("battle_fire_button")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    pressed = true
                    onFirePress()
                    while (true) {
                        val event = awaitPointerEvent()
                        val ch = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!ch.pressed) break
                    }
                    pressed = false
                    onFireRelease()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ОГОНЬ",
            style = MaterialTheme.typography.labelLarge,
            color = if (pressed) TrenchBlack else ParchmentIvory,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TacticalCircleButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    size: Dp,
    progress: Float = 1f,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isActive) BrassGold.copy(alpha = 0.30f) else TrenchBlack.copy(alpha = 0.72f)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = SlateBorder,
                style = Stroke(width = 3f)
            )
            drawArc(
                color = if (isActive) BrassGold else HorizonBlue,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) BrassGold else ParchmentIvory,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentIvory,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CenterCrosshair(
    isAds: Boolean,
    hitMarkerAlpha: Float,
    killMarker: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(44.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val gap = if (isAds) 4f else 9f
        val len = if (isAds) 8f else 11f
        val col = Color.White.copy(alpha = 0.85f)

        drawLine(col, Offset(cx - gap - len, cy), Offset(cx - gap, cy), strokeWidth = 2.2f)
        drawLine(col, Offset(cx + gap, cy), Offset(cx + gap + len, cy), strokeWidth = 2.2f)
        drawLine(col, Offset(cx, cy - gap - len), Offset(cx, cy - gap), strokeWidth = 2.2f)
        drawLine(col, Offset(cx, cy + gap), Offset(cx, cy + gap + len), strokeWidth = 2.2f)

        if (hitMarkerAlpha > 0.01f) {
            val hmCol = (if (killMarker) CrimsonAlert else BrassGold).copy(alpha = hitMarkerAlpha)
            drawLine(hmCol, Offset(cx - 14f, cy - 14f), Offset(cx - 5f, cy - 5f), strokeWidth = 3.2f)
            drawLine(hmCol, Offset(cx + 5f, cy + 5f), Offset(cx + 14f, cy + 14f), strokeWidth = 3.2f)
            drawLine(hmCol, Offset(cx + 14f, cy - 14f), Offset(cx + 5f, cy - 5f), strokeWidth = 3.2f)
            drawLine(hmCol, Offset(cx - 5f, cy + 5f), Offset(cx - 14f, cy + 14f), strokeWidth = 3.2f)
        }
    }
}

@Composable
private fun SniperScopeOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.36f
        // Darkened peripheral mask via thick outer stroke
        drawCircle(
            color = TrenchBlack.copy(alpha = 0.94f),
            radius = r * 2.2f,
            center = Offset(cx, cy),
            style = Stroke(width = r * 2.4f)
        )
        drawCircle(
            color = BrassMuted,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 6f)
        )
        // Crosshair lines inside optic
        drawLine(Color.Black.copy(alpha = 0.85f), Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 2.2f)
        drawLine(Color.Black.copy(alpha = 0.85f), Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 2.2f)
        // Center post tip
        drawLine(Color.Black, Offset(cx, cy), Offset(cx, cy + r), strokeWidth = 5f)
    }
}

@Composable
private fun BattleModalOverlay(
    hud: HudSnapshot,
    isPaused: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onSwitchClass: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val title = when (hud.outcome) {
        BattleOutcome.VICTORY -> "ПОБЕДА НА РУБЕЖЕ!"
        BattleOutcome.DEFEAT -> "ОТРЯД ПОТЕРПЕЛ ПОРАЖЕНИЕ"
        BattleOutcome.PLAYING -> "ПАУЗА"
    }
    val subtitle = when (hud.outcome) {
        BattleOutcome.VICTORY -> "Секторы захвачены (${hud.sectorsCaptured}/${hud.totalSectors}). Нейтрализовано врагов: ${hud.kills}."
        BattleOutcome.DEFEAT -> "Боец пал под огнём неприятеля. Перегруппируйтесь и повторите штурм."
        BattleOutcome.PLAYING -> "${hud.chapterTitle} • ${hud.objectiveText}"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrenchBlack.copy(alpha = 0.82f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = GunmetalDark,
            border = BorderStroke(1.5.dp, BrassGold),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (hud.outcome == BattleOutcome.DEFEAT) CrimsonAlert else BrassGold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ParchmentIvory,
                    textAlign = TextAlign.Center
                )

                if (hud.outcome != BattleOutcome.PLAYING) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = SlatePanel,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ОПЫТ", style = MaterialTheme.typography.labelSmall, color = AshGrey)
                                Text("+${hud.earnedXp} XP", style = MaterialTheme.typography.titleLarge, color = BrassGold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("НАГРАДА", style = MaterialTheme.typography.labelSmall, color = AshGrey)
                                Text("+${hud.earnedStars} ★", style = MaterialTheme.typography.titleLarge, color = BrassGold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ВРЕМЯ", style = MaterialTheme.typography.labelSmall, color = AshGrey)
                                Text("${hud.elapsedSec}с", style = MaterialTheme.typography.titleLarge, color = ParchmentIvory)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (isPaused && hud.outcome == BattleOutcome.PLAYING) {
                    Ww1CommandButton(
                        text = "ПРОДОЛЖИТЬ БОЙ",
                        isPrimary = true,
                        onClick = onResume,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Ww1CommandButton(
                    text = "СМЕНИТЬ КЛАСС",
                    icon = Icons.Filled.Shield,
                    onClick = onSwitchClass,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Ww1CommandButton(
                    text = "НАЧАТЬ ЗАНОВО",
                    icon = Icons.Filled.Replay,
                    isPrimary = hud.outcome != BattleOutcome.PLAYING,
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Ww1CommandButton(
                    text = "ВЫЙТИ В ШТАБ",
                    icon = Icons.Filled.Close,
                    onClick = onExitToMenu,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
