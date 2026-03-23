package com.redfox.game.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redfox.game.data.local.datastore.DemoBalanceManager
import com.redfox.game.data.remote.websocket.BtcPriceSocket
import com.redfox.game.data.remote.websocket.BtcTrade
import com.redfox.game.data.repository.BotGenerator
import com.redfox.game.data.repository.DemoGameService
import com.redfox.game.data.repository.RoundResult
import com.redfox.game.domain.model.Bet
import com.redfox.game.domain.model.BetDirection
import com.redfox.game.domain.model.BotPlayer
import com.redfox.game.domain.model.Round
import com.redfox.game.domain.model.RoundPhase
import com.redfox.game.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val btcPriceSocket: BtcPriceSocket,
    private val demoGameService: DemoGameService,
    private val balanceManager: DemoBalanceManager
) : ViewModel() {

    // Цена BTC (буфер точек для графика)
    val priceBuffer: StateFlow<List<BtcTrade>> = btcPriceSocket.priceBuffer

    // Соединение с Binance
    val isConnected: StateFlow<Boolean> = btcPriceSocket.isConnected

    // Текущий раунд
    val currentRound: StateFlow<Round> = demoGameService.currentRound

    // Таймер
    val timer: StateFlow<Int> = demoGameService.timer

    // Ставка игрока
    val playerBet: StateFlow<Bet?> = demoGameService.playerBet

    // Боты
    val bots: StateFlow<List<BotPlayer>> = demoGameService.bots

    // История раундов
    val roundHistory: StateFlow<List<Round>> = demoGameService.roundHistory

    // Пауза
    val isPaused: StateFlow<Boolean> = demoGameService.isPaused

    // Баланс
    val balance: StateFlow<Double> = balanceManager.balance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Constants.DEMO_START_BALANCE)

    // Сумма ставки (регулятор)
    private val _betAmount = MutableStateFlow(Constants.MIN_BET)
    val betAmount: StateFlow<Double> = _betAmount.asStateFlow()

    // Результат раунда (для оверлея)
    private val _lastResult = MutableStateFlow<RoundResult?>(null)
    val lastResult: StateFlow<RoundResult?> = _lastResult.asStateFlow()

    // Показывать ли оверлей результата
    private val _showResultOverlay = MutableStateFlow(false)
    val showResultOverlay: StateFlow<Boolean> = _showResultOverlay.asStateFlow()

    init {
        demoGameService.start()

        // Подписка на результаты раундов
        viewModelScope.launch {
            demoGameService.roundResult.collect { result ->
                if (result.playerBet != null) {
                    _lastResult.value = result
                    _showResultOverlay.value = true
                }
            }
        }
    }

    fun placeBet(direction: BetDirection) {
        demoGameService.placeBet(direction, _betAmount.value)
    }

    fun resetBet() {
        demoGameService.resetBet()
    }

    fun increaseBet() {
        val newAmount = (_betAmount.value + Constants.MIN_BET).coerceAtMost(balance.value)
        _betAmount.value = newAmount
    }

    fun decreaseBet() {
        val newAmount = (_betAmount.value - Constants.MIN_BET).coerceAtLeast(Constants.MIN_BET)
        _betAmount.value = newAmount
    }

    fun setMinBet() {
        _betAmount.value = Constants.MIN_BET
    }

    fun setMaxBet() {
        _betAmount.value = balance.value.coerceAtLeast(Constants.MIN_BET)
    }

    fun setBetAmount(amount: Double) {
        _betAmount.value = amount.coerceIn(Constants.MIN_BET, balance.value)
    }

    fun dismissResultOverlay() {
        _showResultOverlay.value = false
    }

    fun addDemoBalance(amount: Double) {
        viewModelScope.launch {
            balanceManager.addToBalance(amount)
        }
    }

    override fun onCleared() {
        super.onCleared()
        demoGameService.stop()
    }
}
