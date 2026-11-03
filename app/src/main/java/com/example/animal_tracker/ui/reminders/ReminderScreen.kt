package com.example.animal_tracker.ui.reminders

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.animal_tracker.R
import com.example.animal_tracker.data.model.Pet
import com.example.animal_tracker.data.model.Reminder
import com.example.animal_tracker.ui.theme.CalendarDay
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReminderScreen(
    pet: Pet,
    reminders: List<Reminder>,
    onBack: () -> Unit,
    onToggle: (Int, Boolean) -> Unit,
    onAddClick: () -> Unit
) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    val dailyReminders = reminders.filter {
        try { LocalDate.parse(it.remindAt.take(10)) == currentDate } catch (e: Exception) { false }
    }.sortedBy { it.remindAt }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Image(painter = painterResource(R.drawable.left), contentDescription = "Назад", modifier = Modifier.size(24.dp).clickable { currentDate = currentDate.minusDays(1) })
                Text(currentDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Image(painter = painterResource(R.drawable.right), contentDescription = "Вперед", modifier = Modifier.size(24.dp).clickable { currentDate = currentDate.plusDays(1) })
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Напоминания на сегодня", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
            if (dailyReminders.isEmpty()) {
                Text("Нет напоминаний на этот день", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dailyReminders) { rem ->
                        val timeStr = try { OffsetDateTime.parse(rem.remindAt).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (e: Exception) { "--:--" }

                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CalendarDay).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = rem.isDone, onCheckedChange = { onToggle(rem.id!!, it) }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.onSurface))

                            Text("$timeStr | ${rem.type} | ${rem.title}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddClick, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface), modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("+ Добавить напоминание", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
    }
}