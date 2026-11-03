package com.example.animal_tracker.ui.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animal_tracker.R
import com.example.animal_tracker.ui.theme.Beige

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(1) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state.success) {
        if (state.success) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Хвостатый трекер",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Icon(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(96.dp)
            )

            Spacer(Modifier.height(32.dp))

            AuthToggle(
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it }
            )

            Spacer(Modifier.height(24.dp))

            if (selectedTab == 0) {
                RegisterForm(
                    isLoading = state.isLoading,
                    onRegister = { name, email, pass, confirm ->
                        viewModel.signUp(context, name, email, pass, confirm)
                    }
                )
            } else {
                LoginForm(
                    isLoading = state.isLoading,
                    onLogin = { email, pass -> viewModel.signIn(context, email, pass) },
                    onForgot = { email -> viewModel.resetPassword(context, email) }
                )
            }

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    color = Color(0xFFB00020),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun AuthToggle(
    selectedTab: Int,
    onTabChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.onSurface)
            .padding(4.dp)
    ) {
        val offset by animateFloatAsState(
            targetValue = if (selectedTab == 0) 0f else 1f,
            label = "toggle"
        )

        Row(modifier = Modifier.fillMaxSize()) {
            ToggleItem(
                text = "Регистрация",
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { onTabChange(0) }
            )
            ToggleItem(
                text = "Вход",
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { onTabChange(1) }
            )
        }
    }
}

@Composable
private fun ToggleItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun RegisterForm(
    isLoading: Boolean,
    onRegister: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth()) {
        AuthField(value = name, onValueChange = { name = it }, label = "Имя")
        Spacer(Modifier.height(12.dp))
        AuthField(value = email, onValueChange = { email = it }, label = "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        AuthField(value = password, onValueChange = { password = it }, label = "Пароль", isPassword = true)
        Spacer(Modifier.height(12.dp))
        AuthField(value = confirm, onValueChange = { confirm = it }, label = "Повторите пароль", isPassword = true)

        Spacer(Modifier.height(24.dp))

        AuthButton(
            text = if (isLoading) "Загрузка..." else "Зарегистрироваться",
            enabled = !isLoading,
            onClick = { onRegister(name, email, password, confirm) }
        )
    }
}


@Composable
private fun LoginForm(
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onForgot: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth()) {
        AuthField(value = email, onValueChange = { email = it }, label = "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        AuthField(value = password, onValueChange = { password = it }, label = "Пароль", isPassword = true)

        Spacer(Modifier.height(8.dp))

        Spacer(Modifier.height(24.dp))

        AuthButton(
            text = if (isLoading) "Загрузка..." else "Войти",
            enabled = !isLoading,
            onClick = { onLogin(email, password) }
        )
    }
}


@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(2.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            visualTransformation = if (isPassword)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}


@Composable
private fun AuthButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}