package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.example.game.model.Faction
import com.example.ui.theme.BrassMuted
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FactionFlagBadge(
    faction: Faction,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .border(1.dp, BrassMuted.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
    ) {
        val w = size.width
        val h = size.height

        clipRect {
            when (faction) {
                Faction.GREAT_BRITAIN -> {
                    // Authentic Union Jack
                    drawRect(Color(0xFF012169), size = size)
                    // White diagonals
                    drawLine(Color.White, Offset(0f, 0f), Offset(w, h), strokeWidth = h * 0.24f, cap = StrokeCap.Square)
                    drawLine(Color.White, Offset(0f, h), Offset(w, 0f), strokeWidth = h * 0.24f, cap = StrokeCap.Square)
                    // Red St. Patrick diagonals
                    drawLine(Color(0xFFC8102E), Offset(0f, 0f), Offset(w, h), strokeWidth = h * 0.11f, cap = StrokeCap.Square)
                    drawLine(Color(0xFFC8102E), Offset(0f, h), Offset(w, 0f), strokeWidth = h * 0.11f, cap = StrokeCap.Square)
                    // White St. George cross border
                    drawRect(Color.White, Offset(0f, h * 0.34f), Size(w, h * 0.32f))
                    drawRect(Color.White, Offset(w * 0.39f, 0f), Size(w * 0.22f, h))
                    // Red St. George cross
                    drawRect(Color(0xFFC8102E), Offset(0f, h * 0.40f), Size(w, h * 0.20f))
                    drawRect(Color(0xFFC8102E), Offset(w * 0.43f, 0f), Size(w * 0.14f, h))
                }
                Faction.OTTOMAN_EMPIRE -> {
                    // Authentic Ottoman Empire Flag (Crimson field + White Crescent & 5-pointed Star)
                    val ottomanRed = Color(0xFFE30A17)
                    drawRect(ottomanRed, size = size)
                    val cresCenter = Offset(w * 0.40f, h * 0.50f)
                    drawCircle(Color.White, radius = h * 0.30f, center = cresCenter)
                    drawCircle(ottomanRed, radius = h * 0.24f, center = Offset(w * 0.45f, h * 0.50f))

                    // 5-Pointed Star
                    val sx = w * 0.64f
                    val sy = h * 0.50f
                    val outerR = h * 0.13f
                    val innerR = h * 0.052f
                    val starPath = Path()
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) outerR else innerR
                        val angle = (i * 36f - 18f) * (PI.toFloat() / 180f)
                        val px = sx + cos(angle) * r
                        val py = sy + sin(angle) * r
                        if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
                    }
                    starPath.close()
                    drawPath(starPath, Color.White)
                }
            }
        }
    }
}
