package com.example.animal_tracker.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.animal_tracker.R
import com.example.animal_tracker.data.image.rememberRemoteBitmap
import com.example.animal_tracker.ui.theme.Forum

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    avatarUrl: String?,
    onAvatarChange: (Uri) -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var editName by remember { mutableStateOf(name) }
    var editEmail by remember { mutableStateOf(email) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(name) { editName = name }
    LaunchedEffect(email) { editEmail = email }

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedAvatarUri = uri
            onAvatarChange(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = rememberRemoteBitmap(selectedAvatarUri?.toString() ?: avatarUrl)

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = isEditing) {
                        avatarLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Аватар",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.default_avatar),
                        contentDescription = "Аватар",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!isEditing) {
                    Image(
                        painter = painterResource(R.drawable.redakt),
                        contentDescription = "Изменить",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isEditing = true }
                    )
                }
            }

            Text(text = "Имя", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))

            if (isEditing) {
                BasicTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = name.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            Text(text = "Email", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))

            if (isEditing) {
                BasicTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = email.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))

            if (isEditing) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            editName = name
                            editEmail = email
                            isEditing = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Отмена", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            onSaveProfile(editName.trim(), editEmail.trim())
                            isEditing = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Сохранить", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Button(
                onClick = { showLogoutDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB00020),
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Выйти из профиля", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Выйти из аккаунта?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontFamily = Forum) },
            text = { Text(text = "Вы уверены? Придётся залогиниться заново.", color = MaterialTheme.colorScheme.onSurface, fontFamily = Forum) },
            confirmButton = { TextButton(onClick = { showLogoutDialog = false; onLogout() }) { Text("Выйти", color = Color(0xFFB00020), fontFamily = Forum) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface, fontFamily = Forum) } },
            containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)
        )
    }
}