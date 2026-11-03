package com.example.animal_tracker.ui.feeding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.animal_tracker.R
import com.example.animal_tracker.data.image.rememberRemoteBitmap
import com.example.animal_tracker.data.model.Food
import com.example.animal_tracker.data.model.Pet
import com.example.animal_tracker.ui.theme.Beige
import com.example.animal_tracker.ui.theme.CalendarDay
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun FoodDirectoryScreen(
    pet: Pet,
    foods: List<Food>,
    onBack: () -> Unit,
    onAddToSchedule: (foodId: Int, time: String, date: String) -> Unit,
    onAddNewFood: (brand: String, name: String, calories: Double) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var scheduleFoodItem by remember { mutableStateOf<Food?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.nazad),
                    contentDescription = "Назад",
                    modifier = Modifier.size(28.dp).clickable { onBack() }
                )
                Spacer(Modifier.width(12.dp))
                val bitmap = rememberRemoteBitmap(pet.photoUrl)
                if (bitmap != null) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(CircleShape))
                } else {
                    Image(painter = painterResource(R.drawable.default_avatar), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(CircleShape))
                }
                Spacer(Modifier.width(12.dp))
                Text(pet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            Text("Справочник", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.onSurface).padding(4.dp)) {
            listOf("Мои корма", "Добавить корм").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { selectedTab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(title, color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            if (selectedTab == 0) {
                if (foods.isEmpty()) {
                    Text("Справочник пуст", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(foods) { itemFood ->
                            Column(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(CalendarDay).padding(12.dp)) {
                                if (itemFood.brand.isNotBlank()) {
                                    Text(itemFood.brand, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(itemFood.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                                Text("${itemFood.caloriesPer100g.toInt()} ккал / 100г", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { scheduleFoodItem = itemFood },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.fillMaxWidth().height(32.dp)
                                ) {
                                    Text("В расписание", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                AddFoodForm(
                    onSave = { brand, name, calories ->
                        onAddNewFood(brand, name, calories)
                        selectedTab = 0
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    scheduleFoodItem?.let { foodObj ->
        AddToScheduleDialog(
            food = foodObj,
            onDismiss = { scheduleFoodItem = null },
            onSave = { date, time ->
                onAddToSchedule(foodObj.id!!, time, date)
                scheduleFoodItem = null
            }
        )
    }
}

@Composable
private fun AddFoodForm(onSave: (String, String, Double) -> Unit) {
    var brand by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FormField(brand, { brand = it }, "Бренд корма (необязательно)")
        FormField(name, { name = it }, "Название корма")
        FormField(calories, { calories = it }, "В 100 граммах (ккал)")

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { brand = ""; name = ""; calories = "" },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CalendarDay, contentColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Очистить", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    val c = calories.replace(',', '.').toDoubleOrNull()
                    if (name.isNotBlank() && c != null) onSave(brand.trim(), name.trim(), c)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Готово", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FormField(value: String, onValueChange: (String) -> Unit, label: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Beige.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToScheduleDialog(
    food: Food,
    onDismiss: () -> Unit,
    onSave: (date: String, time: String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(12, 0)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Beige, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Добавить в расписание", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Дата: ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { showTimePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Время: ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена", color = MaterialTheme.colorScheme.surface) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val dateStr = selectedDate.toString() // "2026-10-24"
                            val timeStr = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")) // "12:00"
                            onSave(dateStr, timeStr)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Готово", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Выбрать", color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Выберите дату", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface) },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    dayContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Выберите время", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("Выбрать", color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}