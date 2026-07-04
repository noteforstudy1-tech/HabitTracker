package com.habittracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.data.model.HabitCompletion
import com.habittracker.app.ui.theme.AccentCyan
import com.habittracker.app.ui.theme.AccentPurple
import java.time.LocalDate

@Composable
fun YearlyHeatmap(
    completions: List<HabitCompletion>,
    today: LocalDate
) {
    // Generate the last 120 days (approx 17 weeks)
    val days = remember(today) {
        (119 downTo 0).map { today.minusDays(it.toLong()) }
    }

    // Map epoch day to number of completions
    val countsByDay = remember(completions) {
        completions.groupingBy { it.dateEpochDay }.eachCount()
    }

    // Group into columns (weeks). Each column is a list of up to 7 days
    val weeks = remember(days) {
        days.chunked(7)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Activity Heatmap",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Last 120 days",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Day Labels (Mon, Wed, Fri)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp, end = 8.dp)
            ) {
                listOf("M", "W", "F").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(14.dp) // matches box height + spacing logic slightly
                    )
                }
            }

            // Heatmap Grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(weeks.size) { weekIndex ->
                    val weekDays = weeks[weekIndex]
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekDays.forEach { date ->
                            val count = countsByDay[date.toEpochDay()] ?: 0
                            val boxColor = when {
                                count == 0 -> MaterialTheme.colorScheme.surfaceVariant
                                count == 1 -> AccentPurple.copy(alpha = 0.4f)
                                count == 2 -> AccentPurple.copy(alpha = 0.7f)
                                else       -> AccentPurple
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(boxColor)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(6.dp))
            listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                AccentPurple.copy(alpha = 0.4f),
                AccentPurple.copy(alpha = 0.7f),
                AccentPurple
            ).forEach { color ->
                Box(modifier = Modifier.padding(horizontal = 2.dp).size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
