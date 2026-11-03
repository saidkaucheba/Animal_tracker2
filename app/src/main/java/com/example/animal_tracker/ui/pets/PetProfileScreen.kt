package com.example.animal_tracker.ui.pets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.animal_tracker.R
import com.example.animal_tracker.data.image.rememberRemoteBitmap
import com.example.animal_tracker.data.model.Pet
import java.time.LocalDate
import java.time.Period

@Composable
fun PetProfileScreen(
    pet: Pet,
    lastWeightKg: Double?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onWeightClick: () -> Unit,
    onActivityClick: () -> Unit,
    onFeedingClick: () -> Unit,
    onHealthClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(R.drawable.nazad),
                    contentDescription = "Назад",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBack() }
                )
                Image(
                    painter = painterResource(R.drawable.redakt),
                    contentDescription = "Редактировать",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onEdit() }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val bitmap = rememberRemoteBitmap(pet.photoUrl)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = pet.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.default_avatar),
                        contentDescription = pet.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))

                InfoRow(label = "Порода",  value = pet.breed ?: "—")
                InfoRow(label = "Возраст", value = calculateAge(pet.birthDay))
                InfoRow(label = "Вес",     value = lastWeightKg?.let { "%.1f кг".format(it) } ?: "—")
                InfoRow(label = "Пол",     value = genderLabel(pet.gender))
                InfoRow(label = "Вид",     value = speciesLabel(pet.species))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(title = "Вес", modifier = Modifier.weight(1f).aspectRatio(1f), onClick = onWeightClick)
            ActionCard(title = "Активность", modifier = Modifier.weight(1f).aspectRatio(1f), onClick = onActivityClick)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(title = "Кормление", modifier = Modifier.weight(1f).aspectRatio(1f), onClick = onFeedingClick)
            ActionCard(title = "Здоровье", modifier = Modifier.weight(1f).aspectRatio(1f), onClick = onHealthClick)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

private fun calculateAge(birthDay: String?): String {
    if (birthDay.isNullOrBlank()) return "—"
    return try {
        val birth = LocalDate.parse(birthDay)
        val p = Period.between(birth, LocalDate.now())
        when {
            p.years > 0 -> "${p.years} г. ${p.months} мес."
            p.months > 0 -> "${p.months} мес."
            else -> "${p.days} дн."
        }
    } catch (e: Exception) { "—" }
}

private fun genderLabel(gender: String?): String = when (gender?.lowercase()) {
    "male"   -> "Мальчик"
    "female" -> "Девочка"
    else     -> "—"
}

private fun speciesLabel(species: String?): String = when (species?.lowercase()) {
    "cat"     -> "Кошка"
    "dog"     -> "Собака"
    "rodent"  -> "Грызун"
    "bird"    -> "Птица"
    "reptile" -> "Рептилия"
    "fish"    -> "Рыба"
    "other"   -> "Другое"
    else      -> species ?: "Другое"
}