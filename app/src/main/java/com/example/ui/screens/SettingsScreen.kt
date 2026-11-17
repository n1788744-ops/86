package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.data.PlayerProfileState
import com.example.ui.components.RankHeaderBar
import com.example.ui.components.Ww1CommandButton
import com.example.ui.theme.AshGrey
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassMuted
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlatePanel
import com.example.ui.theme.TrenchBlack

@Composable
fun SettingsScreen(
    profile: PlayerProfileState,
    onUpdateSettings: (Float, Float, Float, Boolean, Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var musicVol by remember(profile.musicVolume) { mutableFloatStateOf(profile.musicVolume) }
    var sfxVol by remember(profile.sfxVolume) { mutableFloatStateOf(profile.sfxVolume) }
    var sensitivity by remember(profile.lookSensitivity) { mutableFloatStateOf(profile.lookSensitivity) }
    var aimAssist by remember(profile.aimAssistEnabled) { mutableStateOf(profile.aimAssistEnabled) }
    var invertY by remember(profile.invertY) { mutableStateOf(profile.invertY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrenchBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        RankHeaderBar(title = "НАСТРОЙКИ", profile = profile)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Audio Card
            Surface(
                color = GunmetalDark,
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = BrassGold)
                        Text(
                            text = "  ЗВУК И ОРКЕСТРОВАЯ МУЗЫКА",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrassGold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Громкость оркестровой музыки: ${(musicVol * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ParchmentIvory
                    )
                    Slider(
                        value = musicVol,
                        onValueChange = {
                            musicVol = it
                            onUpdateSettings(musicVol, sfxVol, sensitivity, aimAssist, invertY)
                        },
                        colors = SliderDefaults.colors(thumbColor = BrassGold, activeTrackColor = BrassGold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = AshGrey, modifier = Modifier.size(18.dp))
                        Text(
                            text = "  Громкость выстрелов и взрывов: ${(sfxVol * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentIvory
                        )
                    }
                    Slider(
                        value = sfxVol,
                        onValueChange = {
                            sfxVol = it
                            onUpdateSettings(musicVol, sfxVol, sensitivity, aimAssist, invertY)
                        },
                        colors = SliderDefaults.colors(thumbColor = BrassGold, activeTrackColor = BrassGold)
                    )
                }
            }

            // Mobile Controls Card
            Surface(
                color = GunmetalDark,
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.TouchApp, contentDescription = null, tint = BrassGold)
                        Text(
                            text = "  МОБИЛЬНОЕ УПРАВЛЕНИЕ",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrassGold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Чувствительность обзора: ${"%.1f".format(sensitivity)}x",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ParchmentIvory
                    )
                    Slider(
                        value = sensitivity,
                        valueRange = 0.4f..2.2f,
                        onValueChange = {
                            sensitivity = it
                            onUpdateSettings(musicVol, sfxVol, sensitivity, aimAssist, invertY)
                        },
                        colors = SliderDefaults.colors(thumbColor = BrassGold, activeTrackColor = BrassGold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Помощь в наведении (Aim Assist)", style = MaterialTheme.typography.bodyLarge, color = ParchmentIvory)
                            Text("Плавная доводка прицела на сенсорном экране", style = MaterialTheme.typography.bodyMedium, color = AshGrey)
                        }
                        Switch(
                            checked = aimAssist,
                            onCheckedChange = {
                                aimAssist = it
                                onUpdateSettings(musicVol, sfxVol, sensitivity, aimAssist, invertY)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = TrenchBlack, checkedTrackColor = BrassGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Инверсия оси Y", style = MaterialTheme.typography.bodyLarge, color = ParchmentIvory)
                            Text("Поменять направление взгляда вверх/вниз", style = MaterialTheme.typography.bodyMedium, color = AshGrey)
                        }
                        Switch(
                            checked = invertY,
                            onCheckedChange = {
                                invertY = it
                                onUpdateSettings(musicVol, sfxVol, sensitivity, aimAssist, invertY)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = TrenchBlack, checkedTrackColor = BrassGold)
                        )
                    }
                }
            }
        }

        Surface(
            color = TrenchBlack,
            border = BorderStroke(1.dp, BrassMuted.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, SlateBorder, RoundedCornerShape(4.dp))
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = ParchmentIvory
                    )
                }
                Ww1CommandButton(
                    text = "ПРИМЕНИТЬ И ВЕРНУТЬСЯ",
                    isPrimary = true,
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
