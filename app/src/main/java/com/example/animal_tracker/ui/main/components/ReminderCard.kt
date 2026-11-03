package com.example.animal_tracker.ui.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.animal_tracker.R
import com.example.animal_tracker.data.model.Reminder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ReminderCard(
    reminders: List<Reminder>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {

        Column(Modifier.fillMaxWidth()) {

            Text(
                text = "Ближайшие напоминания",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(12.dp))

            if (reminders.isEmpty()) {
                Text(
                    text = "Пока нет напоминаний",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                reminders.forEach { reminder ->
                    ReminderRow(reminder)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(reminder: Reminder) {
    val timeStr = try {
        java.time.OffsetDateTime.parse(reminder.remindAt).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) { "--:--" }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(iconResForType(reminder.type)),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "$timeStr · ${reminder.title}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun iconResForType(type: String): Int = when (type.lowercase()) {
    "кормление" -> R.drawable.kormlenie
    "вакцинация" -> R.drawable.vaccination
    "медикаменты" -> R.drawable.medikamenti
    "активность" -> R.drawable.outdoor
    "вес" -> R.drawable.weight
    else -> R.drawable.default_avatar
}

private fun typeLabel(type: String): String = when (type.lowercase()) {
    "vaccination" -> "Вакцинация"
    "medication"  -> "Медикамент"
    "feeding"     -> "Кормление"
    "weight"      -> "Взвешивание"
    "activity"    -> "Активность"
    else          -> "Напоминание"
}

private fun formatTime(iso: String): String = try {
    OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("HH:mm"))
} catch (e: Exception) {
    iso.take(5)
}