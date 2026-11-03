package com.example.animal_tracker.ui.feeding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import com.example.animal_tracker.data.model.FeedingLog
import com.example.animal_tracker.data.model.Food
import com.example.animal_tracker.data.model.Pet
import com.example.animal_tracker.ui.theme.Beige
import com.example.animal_tracker.ui.theme.CalendarDay
import com.example.animal_tracker.ui.theme.Cream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScreen(
    pet: Pet, foods: List<Food>, feedingLogs: List<FeedingLog>, onBack: () -> Unit,
    onDateChange: (String) -> Unit, onUpdateFeeding: (id: Int, amountG: Double, time: String, date: String) -> Unit,
    onDeleteFeeding: (id: Int, date: String) -> Unit, onOpenDirectory: () -> Unit
) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var editLog by remember { mutableStateOf<FeedingLog?>(null) }

    LaunchedEffect(currentDate) { onDateChange(currentDate.toString()) }

    fun parseTime(isoString: String): String {
        return try {
            val offsetDateTime = java.time.OffsetDateTime.parse(isoString)
            val localTime = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())
            localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            "--:--"
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(R.drawable.nazad), contentDescription = "Назад", modifier = Modifier.size(28.dp).clickable { onBack() })
                Spacer(Modifier.width(12.dp))
                val bitmap = rememberRemoteBitmap(pet.photoUrl)
                if (bitmap != null) Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(CircleShape))
                else Image(painter = painterResource(R.drawable.default_avatar), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(CircleShape))
                Spacer(Modifier.width(12.dp))
                Text(pet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            Text("Питание питомца", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Cream)
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Image(painter = painterResource(R.drawable.left), contentDescription = "Назад", modifier = Modifier.size(24.dp).clickable { currentDate = currentDate.minusDays(1) })
                Text(currentDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Image(painter = painterResource(R.drawable.right), contentDescription = "Вперед", modifier = Modifier.size(24.dp).clickable { currentDate = currentDate.plusDays(1) })
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
            if (feedingLogs.isEmpty()) {
                Text("Нет записей за этот день", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Center))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    feedingLogs.forEach { log ->
                        val food = foods.find { it.id == log.foodId }
                        val kcal = ((food?.caloriesPer100g ?: 0.0) * log.amountG / 100.0).toInt()
                        val time = parseTime(log.fedAt)

                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CalendarDay).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(time, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Box(modifier = Modifier.padding(horizontal = 8.dp).width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
                            Column(modifier = Modifier.weight(1f)) {
                                if (!food?.brand.isNullOrBlank()) Text(food.brand, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(food?.name ?: "—", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Box(modifier = Modifier.padding(horizontal = 8.dp).width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
                            Text("${log.amountG.toInt()} г\n$kcal ккал", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(60.dp))
                            Image(painter = painterResource(R.drawable.redakt), contentDescription = "Ред.", modifier = Modifier.size(20.dp).clickable { editLog = log })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onOpenDirectory, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface), modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Справочник кормов", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
    }

    editLog?.let { logItem ->
        val food = foods.find { it.id == logItem.foodId }
        var amount by remember { mutableStateOf(logItem.amountG.toInt().toString()) }
        var time by remember { mutableStateOf(parseTime(logItem.fedAt)) }
        var showTimePicker by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { editLog = null }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Beige, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Редактирование кормления", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                    Spacer(Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).clickable { showTimePicker = true }.padding(16.dp)) {
                        Text("Время: $time", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(8.dp))
                    DialogField(food?.brand ?: "—", {}, "Бренд", readOnly = true)
                    Spacer(Modifier.height(8.dp))
                    DialogField(food?.name ?: "—", {}, "Название", readOnly = true)
                    Spacer(Modifier.height(8.dp))
                    DialogField(amount, { amount = it }, "Количество (г)")

                    Spacer(Modifier.height(20.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onDeleteFeeding(logItem.id!!, currentDate.toString()); editLog = null },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020), contentColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text("Удалить", fontWeight = FontWeight.Bold) }

                        Button(
                            onClick = {
                                val a = amount.toDoubleOrNull()
                                if (a != null) {
                                    onUpdateFeeding(logItem.id!!, a, time, currentDate.toString())
                                    editLog = null
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text("Сохранить", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        if (showTimePicker) {
            val timeState = rememberTimePickerState(initialHour = time.take(2).toIntOrNull() ?: 12, initialMinute = time.takeLast(2).toIntOrNull() ?: 0, is24Hour = true)
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Время кормления", color = MaterialTheme.colorScheme.onSurface) },
                text = { TimePicker(state = timeState) },
                confirmButton = { TextButton(onClick = { time = "${timeState.hour.toString().padStart(2, '0')}:${timeState.minute.toString().padStart(2, '0')}"; showTimePicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) } },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun DialogField(value: String, onValueChange: (String) -> Unit, label: String, readOnly: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        BasicTextField(value = value, onValueChange = onValueChange, singleLine = true, readOnly = readOnly, textStyle = MaterialTheme.typography.bodyLarge.copy(color = if(readOnly) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface), cursorBrush = SolidColor(MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
    }
}