package com.redfox.game.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redfox.game.data.remote.api.ChangePasswordRequest
import com.redfox.game.data.remote.api.ProfileApi
import com.redfox.game.data.remote.api.UpdateProfileRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val email: String = "",
    val nickname: String = "",
    val country: String = "",
    val balanceReal: Double = 0.0,
    val balanceDemo: Double = 0.0,
    val kycStatus: String = "NONE",
    val emailVerified: Boolean = false,
    val roundsPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val profit: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val passwordChanged: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileApi: ProfileApi
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val profile = profileApi.getProfile()
                _state.value = ProfileUiState(
                    email = profile.email,
                    nickname = profile.nickname,
                    country = profile.country,
                    balanceReal = profile.balance_real,
                    balanceDemo = profile.balance_demo,
                    kycStatus = profile.kyc_status,
                    emailVerified = profile.email_verified,
                    roundsPlayed = profile.rounds_played,
                    wins = profile.wins,
                    losses = profile.losses,
                    profit = profile.profit,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateProfile(nickname: String?, country: String?) {
        viewModelScope.launch {
            try {
                val updated = profileApi.updateProfile(UpdateProfileRequest(nickname, country))
                _state.value = _state.value.copy(
                    nickname = updated.nickname,
                    country = updated.country
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, passwordChanged = false)
            try {
                profileApi.changePassword(ChangePasswordRequest(currentPassword, newPassword))
                _state.value = _state.value.copy(isLoading = false, passwordChanged = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
