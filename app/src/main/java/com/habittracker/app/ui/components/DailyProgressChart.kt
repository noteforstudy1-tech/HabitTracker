package com.habittracker.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.ui.theme.*

data class BarData(
    val label: String,
    val progress: Float,
    val isToday: Boolean = false
)

@Composable
fun DailyProgressChart(
    bars: List<BarData>,
    modifier: Modifier = Modifier
) {
    // Animate bar heights on composition/change
    val animatedProgress = bars.map { bar ->
        val anim = remember(bar.label, bar.progress) { Animatable(0f) }
        LaunchedEffect(bar.progress) {
            anim.animateTo(
                targetValue = bar.progress,
                animationSpec = tween(durationMillis = 700, easing = EaseOutCubic)
            )
        }
        anim.value
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                val total = bars.sumOf { (it.progress * 100).toInt() }
                val avg = if (bars.isNotEmpty()) total / bars.size else 0
                Text(
                    text = "Weekly Completion: $avg%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            // Graph Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .width(36.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    listOf("100%", "75%", "50%", "25%", "0%").forEach { pct ->
                        Text(
                            text = pct,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Bars Row
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    bars.forEachIndexed { idx, bar ->
                        val progress = animatedProgress[idx]
                        BarColumn(
                            label = bar.label,
                            progress = progress,
                            isToday = bar.isToday,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarColumn(
    label: String,
    progress: Float,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val barColor = if (isToday) AccentPurpleLight else AccentPurple
    val glowColor = if (isToday) AccentCyan else AccentPurple
    val trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (progress > 0.05f) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 8.sp,
                color = if (isToday) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.height(2.dp))
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .weight(1f)
        ) {
            val trackHeight = size.height
            val fillHeight = trackHeight * progress
            val fillTop = trackHeight - fillHeight

            // Draw full track background
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, trackHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Draw progress fill on top
            if (fillHeight > 0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(glowColor, barColor),
                        startY = fillTop,
                        endY = trackHeight
                    ),
                    topLeft = Offset(0f, fillTop),
                    size = Size(size.width, fillHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        if (isToday) {
            Box(
                Modifier
                    .size(4.dp)
                    .background(AccentCyan, RoundedCornerShape(2.dp))
            )
        }
    }
}
