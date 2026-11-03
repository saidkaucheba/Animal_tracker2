package com.example.animal_tracker.ui.reminders

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.animal_tracker.R
import com.example.animal_tracker.ui.theme.Beige
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    foods: List<String>,
    vaccines: List<String>,
    medications: List<String>,
    onDismiss: () -> Unit,
    onSave: (type: String, title: String, time: String, date: String) -> Unit
) {
    var type by remember { mutableStateOf("Кормление") }
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("12:00") }
    var date by remember { mutableStateOf(LocalDate.now()) }

    var expandedType by remember { mutableStateOf(false) }
    var expandedTitle by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val titlesForType = when(type) {
        "Кормление" -> foods
        "Вакцинация" -> vaccines
        "Медикаменты" -> medications
        "Активность" -> listOf("Прогулка", "Игра", "Обучение")
        "Вес" -> listOf("Взвешивание")
        else -> emptyList()
    }

    LaunchedEffect(type) { title = titlesForType.firstOrNull() ?: "" }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Beige, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                Text("Добавить напоминание", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    DropdownField(type, "Тип напоминания") { expandedType = true }
                    DropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false },
                        modifier = Modifier.fillMaxWidth(0.9f).background(MaterialTheme.colorScheme.surface).clip(RoundedCornerShape(16.dp))
                    ) {
                        listOf("Кормление", "Вакцинация", "Медикаменты", "Активность", "Вес").forEach {
                            DropdownMenuItem(text = { Text(it, color = MaterialTheme.colorScheme.onSurface) }, onClick = { type = it; expandedType = false })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (titlesForType.isNotEmpty() && type != "Вес") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        DropdownField(if(title.isBlank()) "Выберите..." else title, "Название") { expandedTitle = true }
                        DropdownMenu(
                            expanded = expandedTitle,
                            onDismissRequest = { expandedTitle = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(MaterialTheme.colorScheme.surface).heightIn(max = 200.dp).clip(RoundedCornerShape(16.dp))
                        ) {
                            titlesForType.forEach {
                                DropdownMenuItem(text = { Text(it, color = MaterialTheme.colorScheme.onSurface) }, onClick = { title = it; expandedTitle = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                DropdownField(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), "Дата напоминания") { showDatePicker = true }

                Spacer(Modifier.height(8.dp))

                DropdownField(time, "Время") { showTimePicker = true }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена", color = MaterialTheme.colorScheme.surface) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalTitle = if (type == "Вес") "Взвешивание" else title.ifBlank { "Без названия" }
                            onSave(type, finalTitle, time, date.toString())
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Сохранить", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; showDatePicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) { DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)) }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = time.take(2).toIntOrNull() ?: 12, initialMinute = time.takeLast(2).toIntOrNull() ?: 0, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Время напоминания", color = MaterialTheme.colorScheme.onSurface) },
            text = { TimePicker(state = timeState) },
            confirmButton = { TextButton(onClick = { time = "${timeState.hour.toString().padStart(2, '0')}:${timeState.minute.toString().padStart(2, '0')}"; showTimePicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun DropdownField(value: String, label: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("$label: $value", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Image(painter = painterResource(R.drawable.vniz), contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}