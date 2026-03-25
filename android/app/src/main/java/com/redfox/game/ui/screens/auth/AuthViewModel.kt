package com.redfox.game.ui.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redfox.game.data.repository.AuthRepository
import com.redfox.game.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Состояние UI экранов авторизации.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val resetPasswordMessage: String? = null,
    val isAutoLoginChecked: Boolean = false
)

/**
 * ViewModel для экранов авторизации (Login, Register, ResetPassword).
 * Работает через AuthRepository с локальным хранением в DataStore.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Проверка автовхода при запуске приложения.
     * Если пользователь залогинен (есть сохранённый email) — пропускаем экран логина.
     */
    fun checkAutoLogin() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            _uiState.value = _uiState.value.copy(
                isLoggedIn = loggedIn,
                isAutoLoginChecked = true
            )
        }
    }

    /**
     * Вход по email + пароль.
     * Валидация полей → проверка в DataStore → переход на главный экран.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Валидация на стороне клиента
            if (email.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Email не может быть пустым")
                return@launch
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                _uiState.value = _uiState.value.copy(error = "Некорректный формат email")
                return@launch
            }
            if (password.length < 8) {
                _uiState.value = _uiState.value.copy(error = "Пароль минимум 8 символов")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(email.trim(), password)
            _uiState.value = result.fold(
                onSuccess = { user ->
                    _uiState.value.copy(isLoading = false, user = user, isLoggedIn = true)
                },
                onFailure = { e ->
                    _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    /**
     * Регистрация нового пользователя.
     * Валидация полей → сохранение в DataStore → автовход.
     */
    fun register(email: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            // Валидация на стороне клиента
            if (email.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Email не может быть пустым")
                return@launch
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                _uiState.value = _uiState.value.copy(error = "Некорректный формат email")
                return@launch
            }
            if (password.length < 8) {
                _uiState.value = _uiState.value.copy(error = "Пароль минимум 8 символов")
                return@launch
            }
            if (password != passwordConfirmation) {
                _uiState.value = _uiState.value.copy(error = "Пароли не совпадают")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.register(email.trim(), password, passwordConfirmation)
            _uiState.value = result.fold(
                onSuccess = { user ->
                    _uiState.value.copy(isLoading = false, user = user, isLoggedIn = true)
                },
                onFailure = { e ->
                    _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    /**
     * Сброс пароля (в локальной версии — заглушка).
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Email не может быть пустым")
                return@launch
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                _uiState.value = _uiState.value.copy(error = "Некорректный формат email")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null, resetPasswordMessage = null)
            val result = authRepository.resetPassword(email.trim())
            _uiState.value = result.fold(
                onSuccess = { message ->
                    _uiState.value.copy(isLoading = false, resetPasswordMessage = message)
                },
                onFailure = { e ->
                    _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    /**
     * Выход из аккаунта.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState(isAutoLoginChecked = true)
        }
    }

    /**
     * Очистка ошибки.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
