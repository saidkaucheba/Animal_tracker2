package com.example.animal_tracker.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animal_tracker.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun signUp(context: Context, name: String, email: String, password: String, confirm: String) {
        val cleanEmail = email.trim()
        val cleanName = name.trim()

        if (password != confirm) {
            showToast(context, "Пароли не совпадают")
            _state.value = AuthUiState(error = "Пароли не совпадают")
            return
        }
        if (password.length < 6) {
            showToast(context, "Пароль минимум 6 символов")
            _state.value = AuthUiState(error = "Пароль минимум 6 символов")
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = cleanEmail
                    this.password = password
                    this.data = buildJsonObject {
                        put("name", JsonPrimitive(cleanName))
                    }
                }
                _state.value = AuthUiState(success = true)
                showToast(context, "Регистрация успешна!")
            } catch (e: Exception) {
                showToast(context, "Ошибка: ${e.message}")
                _state.value = AuthUiState(error = e.message ?: "Ошибка регистрации")
            }
        }
    }

    fun signIn(context: Context, email: String, password: String) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || password.isBlank()) {
            showToast(context, "Заполните все поля")
            _state.value = AuthUiState(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = cleanEmail
                    this.password = password
                }
                _state.value = AuthUiState(success = true)
                showToast(context, "Успешный вход!")
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("Invalid login") == true -> "Неверный email или пароль"
                    e.message?.contains("Email not confirmed") == true -> "Email не подтвержден"
                    else -> e.message ?: "Ошибка входа"
                }
                showToast(context, errorMsg)
                _state.value = AuthUiState(error = errorMsg)
            }
        }
    }

    fun resetPassword(context: Context, email: String) {
        if (email.isBlank()) {
            showToast(context, "Введите email")
            return
        }

        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.resetPasswordForEmail(email)
                showToast(context, "Письмо отправлено на $email")
                _state.value = AuthUiState(error = "Письмо отправлено на $email")
            } catch (e: Exception) {
                showToast(context, "Ошибка: ${e.message}")
                _state.value = AuthUiState(error = e.message ?: "Ошибка отправки")
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}