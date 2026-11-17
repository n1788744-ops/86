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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.data.PlayerProfileState
import com.example.game.model.Faction
import com.example.game.model.GameCatalog
import com.example.game.model.SoldierClass
import com.example.ui.components.FactionFlagBadge
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
    onOpenShop: () -> Unit,
    onQuickBattle: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val activeClass = SoldierClass.entries.find { it.id == profile.selectedClassId } ?: SoldierClass.RIFLEMAN
    val activeWeapon = GameCatalog.weapons[profile.selectedWeaponId]?.nameRu ?: "SMLE Mk III"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TrenchBlack.copy(alpha = 0.72f),
                        TrenchBlack.copy(alpha = 0.35f),
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
                title = "ШТАБ БРИТАНСКИХ СИЛ",
                profile = profile
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .align(Alignment.Start)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(GunmetalDark.copy(alpha = 0.86f), RoundedCornerShape(4.dp))
                        .border(1.dp, BrassMuted.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    FactionFlagBadge(
                        faction = Faction.GREAT_BRITAIN,
                        modifier = Modifier.width(28.dp).height(18.dp)
                    )
                    Text(
                        text = "ВЕЛИКОБРИТАНИЯ  VS  ОСМАНСКАЯ ИМПЕРИЯ",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrassGold,
                        fontWeight = FontWeight.Bold
                    )
                    FactionFlagBadge(
                        faction = Faction.OTTOMAN_EMPIRE,
                        modifier = Modifier.width(28.dp).height(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ФРОНТ ЧЕСТИ: 1916",
                    style = MaterialTheme.typography.displayLarge,
                    color = ParchmentIvory
                )
                Text(
                    text = "Первая мировая война: высадка на Галлиполи, сражения в городах Газа и Мегиддо. Настоящие флаги, 3D-город, арсенал и магазин.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ParchmentIvory.copy(alpha = 0.88f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = GunmetalDark.copy(alpha = 0.84f),
                    border = BorderStroke(1.dp, HorizonBlue.copy(alpha = 0.55f)),
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
                                text = "БОЕЦ: ${activeClass.titleRu}",
                                style = MaterialTheme.typography.titleLarge,
                                color = BrassGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$activeWeapon • Бинты: ${profile.medkitCount} • Пройдено глав: ${profile.completedChaptersCount}/4",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AshGrey
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Ww1CommandButton(
                    text = "СЮЖЕТНАЯ КАМПАНИЯ",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    isPrimary = true,
                    onClick = onOpenCampaign,
                    testTag = "menu_campaign_button",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Ww1CommandButton(
                    text = "ГОРОДСКОЙ БОЙ (В АТАКУ)",
                    icon = Icons.Filled.PlayArrow,
                    isPrimary = false,
                    onClick = onQuickBattle,
                    testTag = "menu_quick_battle_button",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Ww1CommandButton(
                        text = "КЛАССЫ",
                        icon = Icons.Filled.Shield,
                        onClick = onOpenClasses,
                        testTag = "menu_classes_button",
                        modifier = Modifier.weight(1f)
                    )
                    Ww1CommandButton(
                        text = "МАГАЗИН",
                        icon = Icons.Filled.ShoppingCart,
                        onClick = onOpenShop,
                        testTag = "menu_shop_button",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Ww1CommandButton(
                    text = "НАСТРОЙКИ",
                    icon = Icons.Filled.Settings,
                    onClick = onOpenSettings,
                    testTag = "menu_settings_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Оркестровая музыка • 3D-город и флаги • OpenGL ES 3.0",
                style = MaterialTheme.typography.labelSmall,
                color = AshGrey.copy(alpha = 0.8f)
            )
        }
    }
}
