package com.example.animal_tracker.ui.health

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.animal_tracker.R
import com.example.animal_tracker.data.image.rememberRemoteBitmap
import com.example.animal_tracker.data.model.Medication
import com.example.animal_tracker.data.model.Pet
import com.example.animal_tracker.data.model.Vaccination
import com.example.animal_tracker.ui.theme.CalendarDay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HealthScreen(
    pet: Pet,
    vaccinations: List<Vaccination>,
    medications: List<Medication>,
    onBack: () -> Unit,
    onAddVaccinationClick: () -> Unit,
    onAddMedicationClick: () -> Unit,
    onEditVaccinationClick: (Vaccination) -> Unit,
    onFinishMedication: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
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

        Text(
            text = "Здоровье питомца",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.onSurface).padding(4.dp)) {
            listOf("Вакцинация", "Медикаменты").forEachIndexed { index, title ->
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

        if (selectedTab == 0) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                if (vaccinations.isEmpty()) {
                    Text("Нет данных о вакцинациях", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text("Ближайшие вакцинации:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        items(vaccinations) { vac ->
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CalendarDay).padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(vac.vaccineName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                    Image(
                                        painter = painterResource(R.drawable.redakt),
                                        contentDescription = "Изменить",
                                        modifier = Modifier.size(20.dp).clickable { onEditVaccinationClick(vac) }
                                    )
                                }
                                Text("Последняя: ${vac.dateGiven}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                if (vac.nextDueDate != null) {
                                    val daysLeft = try { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(vac.nextDueDate)) } catch (e: Exception) { 0 }
                                    Text("Следующая: ${vac.nextDueDate} (через $daysLeft дней)", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("Ветеринар: ${vac.vetName ?: "—"} | Клиника: ${vac.clinic ?: "—"}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("История вакцинаций:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        items(vaccinations) { vac ->
                            Text("• ${vac.dateGiven} - ${vac.vaccineName}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                if (medications.isEmpty()) {
                    Text("Нет активных курсов", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(medications) { med ->
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CalendarDay).padding(12.dp)) {
                                Text(med.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Дозировка: ${med.dosage} | ${med.timesPerMonth}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                Text("Время приема: ${med.intakeTime.take(5)}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                Text("Курс: с ${med.startDate} по ${med.endDate}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)

                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { onFinishMedication(med.id!!) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.fillMaxWidth().height(32.dp)
                                ) {
                                    Text("Завершить", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { if (selectedTab == 0) onAddVaccinationClick() else onAddMedicationClick() },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (selectedTab == 0) "+ Добавить вакцинацию" else "+ Добавить медикаменты", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
    }
}