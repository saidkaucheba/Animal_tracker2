package com.example.animal_tracker.ui.main.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.animal_tracker.R
import com.example.animal_tracker.data.image.rememberRemoteBitmap
import com.example.animal_tracker.data.model.Pet
import com.example.animal_tracker.ui.theme.Beige
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetDialog(
    petToEdit: Pet? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, breed: String, birthDay: String, gender: String, species: String, photoUri: Uri?) -> Unit
) {
    var name by remember { mutableStateOf(petToEdit?.name ?: "") }
    var breed by remember { mutableStateOf(petToEdit?.breed ?: "") }
    var gender by remember { mutableStateOf(petToEdit?.gender ?: "male") }
    var species by remember { mutableStateOf(petToEdit?.species ?: "cat") }
    var customSpecies by remember { mutableStateOf(if (petToEdit?.species != null && petToEdit.species !in listOf("cat", "dog", "rodent", "bird", "other")) petToEdit.species else "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var birthDay by remember { mutableStateOf(petToEdit?.birthDay?.let { try { LocalDate.parse(it) } catch(e: Exception){ null } }) }
    var showDatePicker by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedImageUri = uri }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (petToEdit == null) "Новый питомец" else "Редактировать питомца",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(16.dp))

                val previewBitmap = if (selectedImageUri != null) rememberRemoteBitmap(selectedImageUri.toString()) else rememberRemoteBitmap(petToEdit?.photoUrl)
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape).background(Beige.copy(alpha = 0.35f)).clickable {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(bitmap = previewBitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Image(painter = painterResource(R.drawable.default_avatar), contentDescription = "Добавить фото", modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                DialogField(name, { name = it }, "Кличка")
                Spacer(Modifier.height(10.dp))
                DialogField(breed, { breed = it }, "Порода")

                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Beige.copy(alpha = 0.35f)).clickable { showDatePicker = true }.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(
                        text = if (birthDay != null) "Дата рождения: ${birthDay?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}" else "Дата рождения",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text("Вид:", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpeciesChip("Кошка", species == "cat", Modifier.weight(1f)) { species = "cat" }
                    SpeciesChip("Собака", species == "dog", Modifier.weight(1f)) { species = "dog" }
                    SpeciesChip("Другое", species == "other", Modifier.weight(1f)) { species = "other" }
                }
                if (species == "other") {
                    Spacer(Modifier.height(8.dp))
                    DialogField(customSpecies, { customSpecies = it }, "Свой вид")
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Пол:", color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(12.dp))
                    GenderChip("Мальчик", gender == "male") { gender = "male" }
                    Spacer(Modifier.width(8.dp))
                    GenderChip("Девочка", gender == "female") { gender = "female" }
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val finalSpecies = if (species == "other" && customSpecies.isNotBlank()) customSpecies.trim() else species
                                val birthStr = birthDay?.toString() ?: ""
                                onSave(name.trim(), breed.trim(), birthStr, gender, finalSpecies, selectedImageUri)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) { Text("Сохранить") }
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = birthDay?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { birthDay = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; showDatePicker = false }) { Text("ОК", color = MaterialTheme.colorScheme.onSurface) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) { DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)) }
    }
}

@Composable
private fun DialogField(value: String, onValueChange: (String) -> Unit, label: String) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Beige.copy(alpha = 0.35f)).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        BasicTextField(value = value, onValueChange = onValueChange, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

@Composable
private fun GenderChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.onSurface else Beige.copy(alpha = 0.35f)
    val fg = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(bg).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(text, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SpeciesChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.onSurface else Beige.copy(alpha = 0.35f)
    val fg = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(bg).clickable { onClick() }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, color = fg, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
}