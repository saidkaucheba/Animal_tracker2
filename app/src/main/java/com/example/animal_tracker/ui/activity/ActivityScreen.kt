package com.example.animal_tracker.ui.activity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.animal_tracker.R
import com.example.animal_tracker.data.image.rememberRemoteBitmap
import com.example.animal_tracker.data.model.ActivityLog
import com.example.animal_tracker.data.model.Pet
import com.example.animal_tracker.ui.theme.Cream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ActivityScreen(
    pet: Pet,
    activities: List<ActivityLog>,
    onBack: () -> Unit,
    onAddClick: () -> Unit
) {
    val oneWeekAgo = LocalDate.now().minusDays(7).atStartOfDay()
    val weeklyActivities = activities.filter {
        try {
            LocalDate.parse(it.startedAt.take(10)).atStartOfDay() >= oneWeekAgo
        } catch (e: Exception) { false }
    }

    val totalWalks = weeklyActivities.count { it.type == "walk" }
    val totalTime = weeklyActivities.sumOf { it.durationMin ?: 0 }
    val totalDistance = weeklyActivities.sumOf { it.distanceKm ?: 0.0 }
    val totalActivities = weeklyActivities.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Активность питомца", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Cream)
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Сводка за неделю", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBlock(title = "Всего прогулок", value = totalWalks.toString(), modifier = Modifier.weight(1f))
            StatBlock(title = "Время (мин)", value = totalTime.toString(), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBlock(title = "Дистанция (км)", value = String.format("%.1f", totalDistance), modifier = Modifier.weight(1f))
            StatBlock(title = "Всего активностей", value = totalActivities.toString(), modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        ActivityChart(activities = activities)

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("+ Добавить активность", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatBlock(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ActivityChart(activities: List<ActivityLog>) {
    val context = LocalContext.current

    val forumTypeface = remember {
        androidx.core.content.res.ResourcesCompat.getFont(context, R.font.forum_regular)
    }

    val now = LocalDate.now()
    val currentMonth = now.monthValue
    val currentYear = now.year
    val monthName = now.format(DateTimeFormatter.ofPattern("LLLL", Locale("ru"))).replaceFirstChar { it.uppercase() }
    val daysInMonth = now.lengthOfMonth()

    val weeks = listOf(1..7, 8..14, 15..21, 22..daysInMonth)
    val weeklyCounts = weeks.map { week ->
        activities.count { log ->
            try {
                val date = LocalDate.parse(log.startedAt.take(10))
                date.monthValue == currentMonth && date.year == currentYear && date.dayOfMonth in week
            } catch (e: Exception) { false }
        }
    }

    val maxCount = 30

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "График активностей за месяц ($monthName)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#3D2B1F")
                        textSize = 36f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = forumTypeface
                    }
                    val axisPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#3D2B1F")
                        textSize = 32f
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                        typeface = forumTypeface
                    }

                    val leftPadding = 70.dp.toPx()
                    val bottomPadding = 50.dp.toPx()
                    val topPadding = 30.dp.toPx()

                    val chartWidth = size.width - leftPadding - 30.dp.toPx()
                    val chartHeight = size.height - bottomPadding - topPadding

                    // Оси
                    drawLine(Color.DarkGray, Offset(leftPadding, topPadding), Offset(leftPadding, size.height - bottomPadding), strokeWidth = 4f)
                    drawLine(Color.DarkGray, Offset(leftPadding, size.height - bottomPadding), Offset(size.width, size.height - bottomPadding), strokeWidth = 4f)

                    val steps = listOf(0, 10, 20, 30)
                    for (value in steps) {
                        val y = size.height - bottomPadding - (value.toFloat() / maxCount) * chartHeight

                        if (value > 0) {
                            drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(leftPadding, y), Offset(size.width, y), strokeWidth = 2f)
                        }

                        drawContext.canvas.nativeCanvas.drawText("$value", leftPadding - 10.dp.toPx(), y + 10.dp.toPx(), axisPaint)
                    }

                    drawContext.canvas.nativeCanvas.drawText("Кол-во", leftPadding - 10.dp.toPx(), topPadding - 10.dp.toPx(), axisPaint)

                    val barWidth = 50.dp.toPx()
                    val spacing = chartWidth / 4

                    weeklyCounts.forEachIndexed { index, count ->
                        val x = leftPadding + (index * spacing) + (spacing / 2)
                        val safeCount = if (count > maxCount) maxCount else count
                        val barHeight = (safeCount.toFloat() / maxCount) * chartHeight
                        val yTop = size.height - bottomPadding - barHeight

                        drawRect(
                            color = Color(0xFF3D2B1F),
                            topLeft = Offset(x - barWidth / 2, yTop),
                            size = Size(barWidth, barHeight)
                        )

                        if (count > 0) {
                            drawContext.canvas.nativeCanvas.drawText("$count", x, yTop - 15.dp.toPx(), textPaint)
                        }
                        drawContext.canvas.nativeCanvas.drawText("${index + 1}", x, size.height - 10.dp.toPx(), textPaint)
                    }
                }

                Text(
                    text = "Недели",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 10.dp, start = 0.dp)
                )
            }
        }
    }
}