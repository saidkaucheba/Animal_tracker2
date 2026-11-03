package com.example.animal_tracker.ui.health

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.animal_tracker.R
import com.example.animal_tracker.data.model.Vaccination
import com.example.animal_tracker.ui.theme.Beige
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaccinationDialog(
    vaccinationToEdit: Vaccination? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, dateGiven: String, nextDate: String?, vet: String?, clinic: String?) -> Unit,
    onArchive: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(vaccinationToEdit?.vaccineName ?: "") }
    var vet by remember { mutableStateOf(vaccinationToEdit?.vetName ?: "") }
    var clinic by remember { mutableStateOf(vaccinationToEdit?.clinic ?: "") }

    var dateGiven by remember { mutableStateOf(vaccinationToEdit?.dateGiven?.let { LocalDate.parse(it) } ?: LocalDate.now()) }
    var nextDate by remember { mutableStateOf(vaccinationToEdit?.nextDueDate?.let { LocalDate.parse(it) }) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showNextDatePicker by remember { mutableStateOf(false) }

    val isChanged = vaccinationToEdit == null || (
            name != (vaccinationToEdit.vaccineName) ||
                    vet != (vaccinationToEdit.vetName ?: "") ||
                    clinic != (vaccinationToEdit.clinic ?: "") ||
                    dateGiven.toString() != vaccinationToEdit.dateGiven ||
                    nextDate?.toString() != vaccinationToEdit.nextDueDate
            )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Beige, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (vaccinationToEdit == null) "Добавить вакцинацию" else "Редактировать вакцинацию", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                Spacer(Modifier.height(16.dp))

                DialogField(name, { name = it }, "Название вакцинации")
                Spacer(Modifier.height(8.dp))
                DatePickerButton(dateGiven.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), "Дата введения") { showDatePicker = true }
                Spacer(Modifier.height(8.dp))
                DatePickerButton(nextDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Не выбрано", "Дата следующей прививки") { showNextDatePicker = true }
                Spacer(Modifier.height(8.dp))
                DialogField(vet, { vet = it }, "Ветеринар")
                Spacer(Modifier.height(8.dp))
                DialogField(clinic, { clinic = it }, "Клиника")

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (vaccinationToEdit != null && !isChanged && onArchive != null) {
                        Button(
                            onClick = onArchive,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020), contentColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text("Удалить", fontWeight = FontWeight.Bold) }
                    } else {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp)) { Text("Отмена", color = MaterialTheme.colorScheme.surface) }
                    }

                    Button(
                        onClick = {
                            onSave(name, dateGiven.toString(), nextDate?.toString(), vet.trim().ifBlank { null }, clinic.trim().ifBlank { null })
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Сохранить", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateGiven.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { dateGiven = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; showDatePicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) { DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)) }
    }
    if (showNextDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = nextDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showNextDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { nextDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; showNextDatePicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) { DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, dosage: String, times: String, time: String, start: String, end: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var times by remember { mutableStateOf("Каждый день") }
    var customTimes by remember { mutableStateOf("") }
    var isCustomTimes by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var expandedTimes by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf("12:00") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Beige, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Добавить медикаменты", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                Spacer(Modifier.height(16.dp))

                DialogField(name, { name = it }, "Название медикаментов")
                Spacer(Modifier.height(8.dp))
                DialogField(dosage, { dosage = it }, "Дозировка")

                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable { expandedTimes = true }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isCustomTimes) "Свое количество" else "Каждый день", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Image(painter = painterResource(R.drawable.vniz), contentDescription = null, modifier = Modifier.size(24.dp))
                        }
                    }
                    DropdownMenu(expanded = expandedTimes, onDismissRequest = { expandedTimes = false }, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                        DropdownMenuItem(text = { Text("Каждый день", color = MaterialTheme.colorScheme.onSurface) }, onClick = { isCustomTimes = false; expandedTimes = false })
                        DropdownMenuItem(text = { Text("Свое количество", color = MaterialTheme.colorScheme.onSurface) }, onClick = { isCustomTimes = true; expandedTimes = false })
                    }
                }

                if (isCustomTimes) {
                    Spacer(Modifier.height(8.dp))
                    DialogField(customTimes, { customTimes = it; errorText = null }, "Сколько раз в месяц (1-31)")
                }

                if (errorText != null) {
                    Text(errorText!!, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(Modifier.height(8.dp))
                DatePickerButton(time, "Время приема") { showTimePicker = true }

                Spacer(Modifier.height(8.dp))
                DatePickerButton(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), "Дата начала") { showStartPicker = true }

                Spacer(Modifier.height(8.dp))
                DatePickerButton(endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), "Дата конца") { showEndPicker = true }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена", color = MaterialTheme.colorScheme.surface) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isCustomTimes) {
                                val num = customTimes.toIntOrNull()
                                if (num == null || num < 1 || num > 31) {
                                    errorText = "Введите число от 1 до 31"
                                    return@Button
                                }
                            }

                            val finalTimes = if (isCustomTimes) customTimes else "Каждый день"
                            onSave(name, dosage, finalTimes, time, startDate.toString(), endDate.toString())
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Готово", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { startDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; showStartPicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) { DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)) }
    }
    if (showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { endDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; showEndPicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) { DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)) }
    }
    if (showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = 12, initialMinute = 0, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Время приема", color = MaterialTheme.colorScheme.onSurface) },
            text = { TimePicker(state = timeState) },
            confirmButton = { TextButton(onClick = { time = "${timeState.hour.toString().padStart(2, '0')}:${timeState.minute.toString().padStart(2, '0')}"; showTimePicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
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

@Composable
fun DatePickerButton(value: String, label: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("$label: $value", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Image(painter = painterResource(R.drawable.vniz), contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}