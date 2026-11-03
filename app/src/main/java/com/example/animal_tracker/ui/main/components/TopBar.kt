package com.example.animal_tracker.ui.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.animal_tracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    avatarUrl: String?,
    onMenuClick: () -> Unit,
    showMenu: Boolean = true
) {
    TopAppBar(
        title = {
            Text(
                text = "Хвостатый трекер",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            if (showMenu) {
                IconButton(onClick = onMenuClick) {
                    Image(
                        painter = painterResource(R.drawable.menu),
                        contentDescription = "Меню",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        actions = {
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}