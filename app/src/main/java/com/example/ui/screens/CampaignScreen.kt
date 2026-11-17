package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.data.PlayerProfileState
import com.example.game.model.CampaignChapter
import com.example.game.model.GameCatalog
import com.example.game.model.TheatreBiome
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
            .background(
                Brush.verticalGradient(
                    listOf(TrenchBlack, GunmetalDark, TrenchBlack)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        RankHeaderBar(title = "СЮЖЕТНАЯ КАМПАНИЯ", profile = profile)

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
                        // Tactical Theatre Mini-Artwork Banner
                        TheatreMapBanner(
                            biome = chapter.theatre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(86.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.dateAndFrontRu,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrassGold
                                )
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
                                    Text(
                                        text = "ПРОЙДЕНО",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ParchmentIvory
                                    )
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

        // Bottom Tactical Launch Bar
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
            TheatreBiome.MARNE_TRENCHES -> Triple(Color(0xFF283036), Color(0xFF5D5342), Color(0xFF2B241A))
            TheatreBiome.VERDUN_CRATERS -> Triple(Color(0xFF231D1B), Color(0xFF6B3E28), Color(0xFF211B18))
            TheatreBiome.COASTAL_LANDING -> Triple(Color(0xFF3A4958), Color(0xFF7D8C96), Color(0xFF3E3B30))
            TheatreBiome.ALPINE_FORTRESS -> Triple(Color(0xFF2B3847), Color(0xFF6A7D8F), Color(0xFF363A3E))
        }

        drawRect(
            brush = Brush.verticalGradient(listOf(skyTop, skyBot)),
            size = size
        )

        // Distant silhouette hills / cliffs / troopship
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

        if (biome == TheatreBiome.COASTAL_LANDING) {
            // Sea horizon & troopship silhouette with smoke
            drawRect(
                color = Color(0xFF2C4252),
                topLeft = Offset(0f, h * 0.58f),
                size = androidx.compose.ui.geometry.Size(w * 0.55f, h * 0.16f)
            )
            // Ship hull + funnels
            drawRect(Color(0xFF1F2326), Offset(w * 0.20f, h * 0.49f), androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.09f))
            drawRect(Color(0xFF1A1D20), Offset(w * 0.25f, h * 0.39f), androidx.compose.ui.geometry.Size(w * 0.018f, h * 0.10f))
            drawRect(Color(0xFF1A1D20), Offset(w * 0.29f, h * 0.39f), androidx.compose.ui.geometry.Size(w * 0.018f, h * 0.10f))
            drawCircle(Color(0x55222528), radius = h * 0.12f, center = Offset(w * 0.26f, h * 0.26f))
            drawCircle(Color(0x33222528), radius = h * 0.16f, center = Offset(w * 0.30f, h * 0.14f))
        }

        // Foreground trench posts & barbed wire zig-zag
        for (i in 0..5) {
            val px = w * (0.08f + i * 0.17f)
            drawLine(
                color = Color(0xFF17130F),
                start = Offset(px, h * 0.65f),
                end = Offset(px + 4f, h * 0.96f),
                strokeWidth = 3.5f
            )
        }
        drawLine(
            color = Color(0x88C69C53),
            start = Offset(0f, h * 0.78f),
            end = Offset(w, h * 0.74f),
            strokeWidth = 1.5f
        )
    }
}
