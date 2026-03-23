package com.redfox.game.ui.screens.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redfox.game.data.remote.api.CreateWithdrawRequest
import com.redfox.game.data.remote.api.FinanceApi
import com.redfox.game.data.remote.api.TransactionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WithdrawUiState(
    val selectedNetwork: String = "TRC20",
    val address: String = "",
    val amount: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val kycApproved: Boolean = false,
    val history: List<TransactionResponse> = emptyList()
)

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val financeApi: FinanceApi
) : ViewModel() {

    private val _state = MutableStateFlow(WithdrawUiState())
    val state: StateFlow<WithdrawUiState> = _state.asStateFlow()

    fun selectNetwork(network: String) {
        _state.value = _state.value.copy(selectedNetwork = network)
    }

    fun updateAddress(address: String) {
        _state.value = _state.value.copy(address = address)
    }

    fun updateAmount(amount: String) {
        _state.value = _state.value.copy(amount = amount)
    }

    fun submitWithdraw() {
        val s = _state.value
        if (s.address.isBlank() || s.amount.isBlank()) return
        val amountDouble = s.amount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null, success = false)
            try {
                financeApi.createWithdraw(
                    CreateWithdrawRequest(s.selectedNetwork, s.address, amountDouble)
                )
                _state.value = _state.value.copy(isLoading = false, success = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val history = financeApi.getWithdrawHistory()
                _state.value = _state.value.copy(history = history)
            } catch (_: Exception) { }
        }
    }
}
