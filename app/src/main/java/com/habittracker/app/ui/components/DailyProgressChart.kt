package com.habittracker.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.ui.theme.*
import java.time.LocalDate

data class BarData(
    val label: String,     // "Mon"
    val progress: Float,   // 0..1
    val isToday: Boolean = false
)

@Composable
fun DailyProgressChart(
    bars: List<BarData>,
    modifier: Modifier = Modifier
) {
    // Animate bar heights on first composition
    val animatedProgress = bars.map { bar ->
        val anim = remember(bar.label, bar.progress) {
            Animatable(0f)
        }
        LaunchedEffect(bar.progress) {
            anim.animateTo(
                targetValue = bar.progress,
                animationSpec = tween(durationMillis = 700, easing = EaseOutCubic)
            )
        }
        anim.value
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Progress",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            val total = bars.sumOf { (it.progress * 100).toInt() }
            val avg = if (bars.isNotEmpty()) total / bars.size else 0
            Text(
                text = "Week Avg: $avg%",
                fontSize = 12.sp,
                color = AccentCyan
            )
        }

        Spacer(Modifier.height(16.dp))

        // Y-axis labels + bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            // Y-axis
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf("100%", "75%", "50%", "25%", "0%").forEach { label ->
                    Text(label, fontSize = 9.sp, color = TextHint)
                }
            }

            Spacer(Modifier.width(8.dp))

            // Bars
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEachIndexed { index, bar ->
                    val progress = animatedProgress[index]
                    BarColumn(
                        label = bar.label,
                        progress = progress,
                        isToday = bar.isToday,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Horizontal divider line
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderColor)
        )
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

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Percentage label above bar
        if (progress > 0.05f) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 8.sp,
                color = if (isToday) AccentCyan else TextSecondary,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.height(2.dp))
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .weight(1f)
        ) {
            val barHeight = size.height * progress
            val barTop = size.height - barHeight
            val gradient = Brush.verticalGradient(
                colors = listOf(glowColor, barColor),
                startY = barTop,
                endY = size.height
            )
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(0f, barTop),
                size = Size(size.width, barHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Empty bar track
            if (progress < 1f) {
                drawRoundRect(
                    color = BorderColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, barTop),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isToday) TextPrimary else TextSecondary,
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
