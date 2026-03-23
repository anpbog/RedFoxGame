package com.redfox.game.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redfox.game.data.repository.AuthRepository
import com.redfox.game.domain.model.User
import com.redfox.game.domain.usecase.LoginUseCase
import com.redfox.game.domain.usecase.RegisterUseCase
import com.redfox.game.domain.usecase.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val resetPasswordMessage: String? = null,
    val isAutoLoginChecked: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun checkAutoLogin() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                val result = authRepository.refreshToken()
                _uiState.value = if (result.isSuccess) {
                    _uiState.value.copy(isLoggedIn = true, isAutoLoginChecked = true)
                } else {
                    _uiState.value.copy(isAutoLoginChecked = true)
                }
            } else {
                _uiState.value = _uiState.value.copy(isAutoLoginChecked = true)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = loginUseCase(email, password)
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

    fun register(email: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = registerUseCase(email, password, passwordConfirmation)
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

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, resetPasswordMessage = null)
            val result = resetPasswordUseCase(email)
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
