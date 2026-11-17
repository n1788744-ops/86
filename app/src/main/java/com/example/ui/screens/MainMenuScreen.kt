package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.data.PlayerProfileState
import com.example.game.model.GameCatalog
import com.example.game.model.SoldierClass
import com.example.ui.components.RankHeaderBar
import com.example.ui.components.Ww1CommandButton
import com.example.ui.theme.AshGrey
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassMuted
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.HorizonBlue
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.TrenchBlack

@Composable
fun MainMenuScreen(
    profile: PlayerProfileState,
    onOpenCampaign: () -> Unit,
    onOpenClasses: () -> Unit,
    onQuickBattle: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val activeClass = SoldierClass.entries.find { it.id == profile.selectedClassId } ?: SoldierClass.RIFLEMAN
    val activeWeapon = GameCatalog.weapons[profile.selectedWeaponId]?.nameRu ?: "Lebel M1886"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TrenchBlack.copy(alpha = 0.74f),
                        TrenchBlack.copy(alpha = 0.38f),
                        TrenchBlack.copy(alpha = 0.88f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            RankHeaderBar(
                title = "ШТАБ АНТАНТЫ",
                profile = profile
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Hero Title & Command Card
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .align(Alignment.Start)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(BrassGold.copy(alpha = 0.16f), RoundedCornerShape(3.dp))
                        .border(1.dp, BrassMuted.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = BrassGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ВЕЛИКАЯ ВОЙНА • 1914–1918",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrassGold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ФРОНТ ЧЕСТИ: 1916",
                    style = MaterialTheme.typography.displayLarge,
                    color = ParchmentIvory
                )
                Text(
                    text = "Масштабная тактическая 3D-кампания от окопов Марны и пекла Вердена до высадки на побережье и гор Изонцо.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ParchmentIvory.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Current Loadout Badge
                Surface(
                    color = GunmetalDark.copy(alpha = 0.84f),
                    border = BorderStroke(1.dp, HorizonBlue.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ТЕКУЩИЙ БОЕЦ: ${activeClass.titleRu}",
                                style = MaterialTheme.typography.titleLarge,
                                color = BrassGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Вооружение: $activeWeapon • Пройдено глав: ${profile.completedChaptersCount}/4",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AshGrey
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Ww1CommandButton(
                    text = "СЮЖЕТНАЯ КАМПАНИЯ",
                    icon = Icons.Filled.MenuBook,
                    isPrimary = true,
                    onClick = onOpenCampaign,
                    testTag = "menu_campaign_button",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Ww1CommandButton(
                    text = "БЫСТРЫЙ БОЙ (В АТАКУ)",
                    icon = Icons.Filled.PlayArrow,
                    isPrimary = false,
                    onClick = onQuickBattle,
                    testTag = "menu_quick_battle_button",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Ww1CommandButton(
                        text = "КЛАССЫ",
                        icon = Icons.Filled.Shield,
                        onClick = onOpenClasses,
                        testTag = "menu_classes_button",
                        modifier = Modifier.weight(1f)
                    )
                    Ww1CommandButton(
                        text = "НАСТРОЙКИ",
                        icon = Icons.Filled.Settings,
                        onClick = onOpenSettings,
                        testTag = "menu_settings_button",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Звуковое сопровождение: оркестровая тема • 3D OpenGL ES 3.0",
                style = MaterialTheme.typography.labelSmall,
                color = AshGrey.copy(alpha = 0.8f)
            )
        }
    }
}
