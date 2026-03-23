package com.redfox.game.ui.screens.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redfox.game.data.remote.api.CreateDepositRequest
import com.redfox.game.data.remote.api.FinanceApi
import com.redfox.game.data.remote.api.TransactionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DepositUiState(
    val selectedNetwork: String = "TRC20",
    val address: String = "",
    val status: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val history: List<TransactionResponse> = emptyList()
)

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val financeApi: FinanceApi
) : ViewModel() {

    private val _state = MutableStateFlow(DepositUiState())
    val state: StateFlow<DepositUiState> = _state.asStateFlow()

    fun selectNetwork(network: String) {
        _state.value = _state.value.copy(selectedNetwork = network)
        createDeposit(network)
    }

    fun createDeposit(network: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = financeApi.createDeposit(CreateDepositRequest(network))
                _state.value = _state.value.copy(
                    address = response.address,
                    status = response.status,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val history = financeApi.getDepositHistory()
                _state.value = _state.value.copy(history = history)
            } catch (_: Exception) { }
        }
    }

    fun checkStatus(depositId: Long) {
        viewModelScope.launch {
            try {
                val tx = financeApi.getDepositStatus(depositId)
                _state.value = _state.value.copy(status = tx.status)
            } catch (_: Exception) { }
        }
    }
}
