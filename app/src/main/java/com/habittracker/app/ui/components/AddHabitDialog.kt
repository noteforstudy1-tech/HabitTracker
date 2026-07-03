package com.habittracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.habittracker.app.data.model.Habit
import com.habittracker.app.ui.theme.*

@Composable
fun HabitDialog(
    habitToEdit: Habit? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String, frequencyType: String, customDays: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var habitName by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var selectedColor by remember { mutableStateOf(habitToEdit?.colorHex ?: HabitAccentColors[0]) }
    var frequencyType by remember { mutableStateOf(habitToEdit?.frequencyType ?: "DAILY") }

    // Init custom days list
    val selectedDays = remember {
        val initialList = habitToEdit?.customDays?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: listOf(1, 2, 3, 4, 5, 6, 7)
        mutableStateListOf<Int>().apply { addAll(initialList) }
    }

    val weekDaysMap = listOf(
        1 to "M",
        2 to "T",
        3 to "W",
        4 to "T",
        5 to "F",
        6 to "S",
        7 to "S"
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (habitToEdit == null) "New Habit" else "Edit Habit",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (habitToEdit != null && onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Habit",
                                tint = AccentRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Name input
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                placeholder = {
                    Text(
                        text = "e.g. Read 30 min",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (habitName.isNotBlank()) {
                        val daysString = if (frequencyType == "DAILY") "1,2,3,4,5,6,7" else selectedDays.sorted().joinToString(",")
                        onConfirm(habitName, selectedColor, frequencyType, daysString)
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = AccentPurple
                )
            )

            Spacer(Modifier.height(16.dp))

            // Frequency Selector
            Text(
                text = "Frequency",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Daily option
                FilterChip(
                    selected = frequencyType == "DAILY",
                    onClick = { frequencyType = "DAILY" },
                    label = { Text("Daily") }
                )
                // Specific days option
                FilterChip(
                    selected = frequencyType == "SPECIFIC_DAYS",
                    onClick = { frequencyType = "SPECIFIC_DAYS" },
                    label = { Text("Specific Days") }
                )
            }

            // Render day selector if CUSTOM / SPECIFIC_DAYS is selected
            if (frequencyType == "SPECIFIC_DAYS") {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDaysMap.forEach { (dayInt, label) ->
                        val isSelected = selectedDays.contains(dayInt)
                        val activeColor = try { Color(android.graphics.Color.parseColor(selectedColor)) } catch (e: Exception) { AccentPurple }
                        val chipBg = if (isSelected) activeColor.copy(alpha = 0.3f) else Color.Transparent
                        val chipBorder = if (isSelected) activeColor else MaterialTheme.colorScheme.outline

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(chipBg)
                                .border(width = 1.dp, color = chipBorder, shape = RoundedCornerShape(6.dp))
                                .clickable {
                                    if (isSelected) {
                                        if (selectedDays.size > 1) { // Require at least 1 day selected
                                            selectedDays.remove(dayInt)
                                        }
                                    } else {
                                        selectedDays.add(dayInt)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Color picker label
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            // 8-color grid selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HabitAccentColors.take(8).forEach { hex ->
                    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { AccentPurple }
                    val isSelected = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .clickable { selectedColor = hex }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            val daysString = if (frequencyType == "DAILY") "1,2,3,4,5,6,7" else selectedDays.sorted().joinToString(",")
                            onConfirm(habitName, selectedColor, frequencyType, daysString)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = habitName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = if (habitToEdit == null) "Create" else "Save")
                }
            }
        }
    }
}
