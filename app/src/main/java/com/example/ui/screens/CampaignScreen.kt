package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.data.PlayerProfileState
import com.example.game.model.CampaignChapter
import com.example.game.model.Faction
import com.example.game.model.GameCatalog
import com.example.game.model.TheatreBiome
import com.example.ui.components.FactionFlagBadge
import com.example.ui.components.RankHeaderBar
import com.example.ui.components.Ww1CommandButton
import com.example.ui.theme.AshGrey
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassMuted
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.HorizonBlue
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlatePanel
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TrenchBlack

@Composable
fun CampaignScreen(
    profile: PlayerProfileState,
    selectedChapter: CampaignChapter,
    onSelectChapter: (CampaignChapter) -> Unit,
    onLaunchBattle: (CampaignChapter) -> Unit,
    onOpenClasses: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TrenchBlack, GunmetalDark, TrenchBlack)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        RankHeaderBar(title = "КАМПАНИЯ: БРИТАНИЯ VS ОСМАНЫ", profile = profile)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(GameCatalog.chapters, key = { it.id }) { chapter ->
                val isSelected = chapter.id == selectedChapter.id
                val isCompleted = chapter.index < profile.completedChaptersCount

                Surface(
                    color = if (isSelected) SlatePanel else GunmetalDark.copy(alpha = 0.85f),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) BrassGold else SlateBorder
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_card_${chapter.index}")
                        .clickable { onSelectChapter(chapter) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        TheatreMapBanner(
                            biome = chapter.theatre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(92.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FactionFlagBadge(Faction.GREAT_BRITAIN, Modifier.width(20.dp).height(13.dp))
                                    Text(
                                        text = chapter.dateAndFrontRu,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrassGold
                                    )
                                    FactionFlagBadge(Faction.OTTOMAN_EMPIRE, Modifier.width(20.dp).height(13.dp))
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = chapter.titleRu,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = ParchmentIvory,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isCompleted) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(TacticalGreen.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                        .border(1.dp, TacticalGreen, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Глава пройдена",
                                        tint = TacticalGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ПРОЙДЕНО", style = MaterialTheme.typography.labelSmall, color = ParchmentIvory)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = chapter.storyDispatchRu,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentIvory.copy(alpha = 0.86f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Цель: ${chapter.objectiveTitleRu}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HorizonBlue,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Рек.: ${chapter.recommendedClass.titleRu}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AshGrey
                            )
                        }
                    }
                }
            }
        }

        Surface(
            color = TrenchBlack.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, BrassMuted.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, SlateBorder, RoundedCornerShape(4.dp))
                        .testTag("campaign_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад в меню",
                        tint = ParchmentIvory
                    )
                }

                Ww1CommandButton(
                    text = "КЛАСС",
                    icon = Icons.Filled.Shield,
                    onClick = onOpenClasses,
                    testTag = "campaign_classes_button",
                    modifier = Modifier.weight(0.45f)
                )

                Ww1CommandButton(
                    text = "В БОЙ",
                    icon = Icons.Filled.PlayArrow,
                    isPrimary = true,
                    onClick = { onLaunchBattle(selectedChapter) },
                    testTag = "campaign_launch_button",
                    modifier = Modifier.weight(0.65f)
                )
            }
        }
    }
}

@Composable
private fun TheatreMapBanner(
    biome: TheatreBiome,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val (skyTop, skyBot, groundColor) = when (biome) {
            TheatreBiome.GALLIPOLI_COAST -> Triple(Color(0xFF3A4958), Color(0xFF7D8C96), Color(0xFF3E3B30))
            TheatreBiome.BASRA_OUTSKIRTS -> Triple(Color(0xFF4F4432), Color(0xFF9E845E), Color(0xFF4A3D29))
            TheatreBiome.GAZA_CITY -> Triple(Color(0xFF433A31), Color(0xFF9C7D5A), Color(0xFF3B3126))
            TheatreBiome.JERUSALEM_CITADEL -> Triple(Color(0xFF2D3847), Color(0xFF767F8A), Color(0xFF383430))
        }

        drawRect(brush = Brush.verticalGradient(listOf(skyTop, skyBot)), size = size)

        if (biome == TheatreBiome.GAZA_CITY || biome == TheatreBiome.JERUSALEM_CITADEL) {
            // Whole City Skyline Silhouettes (sandstone blocks, domes, clocktower)
            val bColor = Color(0xFF27211B)
            val bHighlight = Color(0xFF42362B)
            drawRect(bColor, Offset(w * 0.05f, h * 0.42f), Size(w * 0.14f, h * 0.58f))
            drawRect(bHighlight, Offset(w * 0.21f, h * 0.50f), Size(w * 0.12f, h * 0.50f))
            drawRect(bColor, Offset(w * 0.36f, h * 0.36f), Size(w * 0.16f, h * 0.64f))
            // Tall Clocktower / Minaret
            drawRect(bColor, Offset(w * 0.57f, h * 0.18f), Size(w * 0.07f, h * 0.82f))
            drawCircle(bColor, radius = w * 0.036f, center = Offset(w * 0.605f, h * 0.18f))
            drawRect(bHighlight, Offset(w * 0.68f, h * 0.46f), Size(w * 0.14f, h * 0.54f))
            drawRect(bColor, Offset(w * 0.84f, h * 0.38f), Size(w * 0.12f, h * 0.62f))
        } else {
            val ridge = Path().apply {
                moveTo(0f, h * 0.68f)
                lineTo(w * 0.22f, h * 0.50f)
                lineTo(w * 0.48f, h * 0.60f)
                lineTo(w * 0.72f, h * 0.44f)
                lineTo(w, h * 0.62f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(ridge, groundColor)

            if (biome == TheatreBiome.GALLIPOLI_COAST) {
                drawRect(Color(0xFF2C4252), Offset(0f, h * 0.58f), Size(w * 0.55f, h * 0.16f))
                drawRect(Color(0xFF1F2326), Offset(w * 0.20f, h * 0.49f), Size(w * 0.16f, h * 0.09f))
                drawRect(Color(0xFF1A1D20), Offset(w * 0.25f, h * 0.39f), Size(w * 0.018f, h * 0.10f))
                drawRect(Color(0xFF1A1D20), Offset(w * 0.29f, h * 0.39f), Size(w * 0.018f, h * 0.10f))
                drawCircle(Color(0x55222528), radius = h * 0.12f, center = Offset(w * 0.26f, h * 0.26f))
            }
        }
    }
}
