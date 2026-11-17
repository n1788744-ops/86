package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game.data.PlayerProfileState
import com.example.ui.theme.AshGrey
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassMuted
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlatePanel
import com.example.ui.theme.TrenchBlack

@Composable
fun RankHeaderBar(
    title: String,
    profile: PlayerProfileState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TrenchBlack.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, BrassMuted.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Filled.MilitaryTech,
                    contentDescription = "Ранг бойца",
                    tint = BrassGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Уровень ${profile.level}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ParchmentIvory
                    )
                    LinearProgressIndicator(
                        progress = {
                            (profile.xp.toFloat() / profile.xpForNextLevel.toFloat()).coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .width(72.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = BrassGold,
                        trackColor = SlatePanel
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ParchmentIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(SlatePanel.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .border(1.dp, BrassMuted.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${profile.stars}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrassGold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Звёзды доблести",
                    tint = BrassGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun Ww1CommandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = false,
    testTag: String = ""
) {
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(listOf(BrassMuted, BrassGold, BrassMuted))
    } else {
        Brush.horizontalGradient(listOf(GunmetalDark.copy(alpha = 0.92f), SlatePanel.copy(alpha = 0.92f)))
    }
    val borderColor = if (isPrimary) BrassGold else SlateBorder
    val contentColor = if (isPrimary) TrenchBlack else ParchmentIvory

    Box(
        modifier = modifier
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .clip(RoundedCornerShape(4.dp))
            .background(bgBrush)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (isPrimary) TrenchBlack else BrassGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatMeterRow(
    label: String,
    value: Int,
    accentColor: Color = BrassGold,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = AshGrey
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentIvory
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = accentColor,
            trackColor = TrenchBlack
        )
    }
}
