package com.example.animal_tracker.ui.weight

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
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
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSave: (weightKg: Double, measuredAt: String, note: String?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Beige, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Добавить замер", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                Spacer(Modifier.height(16.dp))

                DialogField(weight, { weight = it }, "Вес (кг)")
                Spacer(Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable { showDatePicker = true }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Дата: ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Image(painter = painterResource(R.drawable.vniz), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                DialogField(note, { note = it }, "Заметка")

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена", color = MaterialTheme.colorScheme.surface) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val w = weight.replace(',', '.').toDoubleOrNull()
                            if (w == null || w <= 0.0) {
                                error = "Введите корректный вес"
                            } else {
                                onSave(w, selectedDate.toString(), note.trim().ifBlank { null })
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Готово", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; showDatePicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) { DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)) }
    }
}

@Composable
fun DialogField(value: String, onValueChange: (String) -> Unit, label: String) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        BasicTextField(
            value = value, onValueChange = onValueChange, singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.surface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
    }
}