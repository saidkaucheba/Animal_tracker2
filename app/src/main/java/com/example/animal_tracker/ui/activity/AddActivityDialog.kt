package com.example.animal_tracker.ui.activity

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

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, duration: Int, distance: Double?, repetitions: Int?, note: String?) -> Unit
) {
    var selectedType by remember { mutableStateOf("walk") }
    var expanded by remember { mutableStateOf(false) }

    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var repetitions by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Beige, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Добавить активность", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { expanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val typeName = when(selectedType) {
                                "walk" -> "Прогулка"
                                "play" -> "Игра с игрушками"
                                else -> "Обучение командам"
                            }
                            Text(typeName, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)

                            Image(
                                painter = painterResource(R.drawable.vniz),
                                contentDescription = "Выбрать",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        DropdownMenuItem(text = { Text("Прогулка", color = MaterialTheme.colorScheme.onSurface) }, onClick = { selectedType = "walk"; expanded = false })
                        DropdownMenuItem(text = { Text("Игра с игрушками", color = MaterialTheme.colorScheme.onSurface) }, onClick = { selectedType = "play"; expanded = false })
                        DropdownMenuItem(text = { Text("Обучение командам", color = MaterialTheme.colorScheme.onSurface) }, onClick = { selectedType = "training"; expanded = false })
                    }
                }

                Spacer(Modifier.height(12.dp))

                DialogField(duration, { duration = it }, "Длительность (мин)")

                if (selectedType == "walk") {
                    Spacer(Modifier.height(8.dp))
                    DialogField(distance, { distance = it }, "Дистанция (км)")
                } else if (selectedType == "training") {
                    Spacer(Modifier.height(8.dp))
                    DialogField(repetitions, { repetitions = it }, "Количество раз")
                }

                Spacer(Modifier.height(8.dp))
                DialogField(note, { note = it }, "Заметка")

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена", color = MaterialTheme.colorScheme.surface) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val dur = duration.toIntOrNull() ?: 0
                            val dist = distance.replace(',', '.').toDoubleOrNull()
                            val reps = repetitions.toIntOrNull()

                            onSave(selectedType, dur, dist, reps, note.trim().ifBlank { null })
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
}

@Composable
private fun DialogField(value: String, onValueChange: (String) -> Unit, label: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)).padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.surface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
    }
}