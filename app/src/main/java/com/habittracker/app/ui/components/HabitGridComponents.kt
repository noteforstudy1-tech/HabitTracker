package com.habittracker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.app.data.model.Habit
import com.habittracker.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HabitGridHeader(weekDates: List<LocalDate>, today: LocalDate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariantDark)
            .padding(vertical = 6.dp)
    ) {
        // Habit name column header
        Box(
            modifier = Modifier
                .width(140.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Habit", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        }

        // Divider
        Box(Modifier.width(1.dp).height(24.dp).background(BorderColor))

        // Date column headers (horizontal scroll matches the rows below)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            weekDates.forEach { date ->
                val isToday = date == today
                DayHeaderCell(date = date, isToday = isToday)
            }
        }
    }
}

@Composable
fun DayHeaderCell(date: LocalDate, isToday: Boolean) {
    val dayLetter = date.format(DateTimeFormatter.ofPattern("EEE")).take(1)
    val dayNum = date.dayOfMonth.toString()

    Box(
        modifier = Modifier
            .width(48.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayLetter,
                fontSize = 10.sp,
                color = if (isToday) AccentCyan else TextSecondary
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(
                        if (isToday) Modifier.background(AccentPurple, RoundedCornerShape(6.dp))
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayNum,
                    fontSize = 11.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) TextPrimary else TextSecondary
                )
            }
        }
    }
}

@Composable
fun HabitRow(
    habit: Habit,
    weekDates: List<LocalDate>,
    isCompleted: (LocalDate) -> Boolean,
    onToggle: (LocalDate) -> Unit,
    onLongPress: () -> Unit,
    today: LocalDate
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        AccentPurple
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .border(width = 0.5.dp, color = BorderColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pinned habit name
        Row(
            modifier = Modifier
                .width(140.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accentColor, RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = habit.name,
                fontSize = 12.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
        }

        // Vertical divider
        Box(Modifier.width(1.dp).height(44.dp).background(BorderColor))

        // Scrollable checkbox cells
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            weekDates.forEach { date ->
                val done = isCompleted(date)
                val isFuture = date.isAfter(today)
                CheckboxCell(
                    checked = done,
                    isFuture = isFuture,
                    accentColor = accentColor,
                    onClick = { if (!isFuture) onToggle(date) }
                )
            }
        }
    }
}

@Composable
fun CheckboxCell(
    checked: Boolean,
    isFuture: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            checked  -> accentColor.copy(alpha = 0.25f)
            isFuture -> SurfaceVariantDark.copy(alpha = 0.3f)
            else     -> SurfaceDark
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cell_bg"
    )
    val borderCol by animateColorAsState(
        targetValue = when {
            checked  -> accentColor
            isFuture -> BorderColor.copy(alpha = 0.4f)
            else     -> BorderColor
        },
        label = "cell_border"
    )

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(44.dp)
            .padding(5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
