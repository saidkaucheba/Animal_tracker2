package com.example.animal_tracker.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.animal_tracker.data.SupabaseClient
import com.example.animal_tracker.ui.auth.AuthScreen
import com.example.animal_tracker.ui.main.MainScreen
import com.example.animal_tracker.ui.theme.AnimalTrackerTheme
import com.example.animal_tracker.ui.theme.DarkBrown
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        setContent {
            AnimalTrackerTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val navController = rememberNavController()
    val auth = SupabaseClient.client.auth
    val sessionStatus by auth.sessionStatus.collectAsStateWithLifecycle()

    when (sessionStatus) {
        is SessionStatus.Authenticated -> {
            MainScreen(navController = navController)
        }
        is SessionStatus.NotAuthenticated -> {
            AuthScreen(onAuthSuccess = {})
        }
        is SessionStatus.Initializing -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkBrown)
            }
        }
        else -> {
            AuthScreen(onAuthSuccess = {})
        }
    }
}