package com.redfox.game.data.repository

import com.redfox.game.data.local.datastore.DemoBalanceManager
import com.redfox.game.data.local.db.DemoRoundDao
import com.redfox.game.data.local.db.DemoRoundEntity
import com.redfox.game.data.remote.websocket.BtcPriceSocket
import com.redfox.game.domain.model.Bet
import com.redfox.game.domain.model.BetDirection
import com.redfox.game.domain.model.BotPlayer
import com.redfox.game.domain.model.Round
import com.redfox.game.domain.model.RoundPhase
import com.redfox.game.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class RoundResult(
    val round: Round,
    val playerBet: Bet?,
    val playerPayout: Double,
    val isWin: Boolean
)

@Singleton
class DemoGameService @Inject constructor(
    private val btcPriceSocket: BtcPriceSocket,
    private val balanceManager: DemoBalanceManager,
    private val botGenerator: BotGenerator,
    private val demoRoundDao: DemoRoundDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var roundJob: Job? = null
    private var botJob: Job? = null
    private var roundCounter = 0

    // Абсолютное время окончания текущей фазы (для корректной работы при сворачивании приложения)
    private var phaseEndTime: Long = 0L

    // Состояние текущего раунда
    private val _currentRound = MutableStateFlow(Round())
    val currentRound: StateFlow<Round> = _currentRound.asStateFlow()

    // Таймер (секунды до конца фазы)
    private val _timer = MutableStateFlow(0)
    val timer: StateFlow<Int> = _timer.asStateFlow()

    // Ставка игрока в текущем раунде
    private val _playerBet = MutableStateFlow<Bet?>(null)
    val playerBet: StateFlow<Bet?> = _playerBet.asStateFlow()

    // Боты в текущем раунде
    private val _bots = MutableStateFlow<List<BotPlayer>>(emptyList())
    val bots: StateFlow<List<BotPlayer>> = _bots.asStateFlow()

    // Результат раунда (emit при завершении)
    private val _roundResult = MutableSharedFlow<RoundResult>(replay = 0)
    val roundResult: SharedFlow<RoundResult> = _roundResult.asSharedFlow()

    // История раундов (последние 10)
    private val _roundHistory = MutableStateFlow<List<Round>>(emptyList())
    val roundHistory: StateFlow<List<Round>> = _roundHistory.asStateFlow()

    // Флаг паузы (потеря соединения)
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    // Последнее направление движения цены (для случая startPrice == endPrice)
    private var lastPriceDirection: BetDirection = BetDirection.UP

    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        btcPriceSocket.connect()

        // Подписка на состояние соединения → пауза при обрыве
        scope.launch {
            btcPriceSocket.isConnected.collect { connected ->
                _isPaused.value = !connected
            }
        }

        startNewRound()
    }

    fun stop() {
        isRunning = false
        roundJob?.cancel()
        botJob?.cancel()
        btcPriceSocket.disconnect()
    }

    fun destroy() {
        stop()
        scope.cancel()
    }

    fun placeBet(direction: BetDirection, amount: Double) {
        val round = _currentRound.value
        if (round.phase != RoundPhase.BETTING) return
        if (_playerBet.value != null) return

        val bet = Bet(direction, amount)
        _playerBet.value = bet

        // Обновляем пулы
        val updatedRound = if (direction == BetDirection.UP) {
            round.copy(poolUp = round.poolUp + amount)
        } else {
            round.copy(poolDown = round.poolDown + amount)
        }

        // Пересчитываем payout
        _currentRound.value = recalculatePayouts(updatedRound)

        // Списываем со счёта
        scope.launch {
            balanceManager.subtractFromBalance(amount)
        }
    }

    fun resetBet() {
        // Можно сбросить только в фазе BETTING
        val bet = _playerBet.value ?: return
        val round = _currentRound.value
        if (round.phase != RoundPhase.BETTING) return

        _playerBet.value = null

        val updatedRound = if (bet.direction == BetDirection.UP) {
            round.copy(poolUp = (round.poolUp - bet.amount).coerceAtLeast(0.0))
        } else {
            round.copy(poolDown = (round.poolDown - bet.amount).coerceAtLeast(0.0))
        }

        _currentRound.value = recalculatePayouts(updatedRound)

        // Возвращаем деньги
        scope.launch {
            balanceManager.addToBalance(bet.amount)
        }
    }

    private fun startNewRound() {
        roundJob?.cancel()
        botJob?.cancel()
        roundCounter++

        _playerBet.value = null
        _bots.value = emptyList()
        botGenerator.resetForNewRound()

        _currentRound.value = Round(
            id = roundCounter,
            phase = RoundPhase.BETTING,
            timerSeconds = Constants.BETTING_PHASE_SECONDS
        )
        _timer.value = Constants.BETTING_PHASE_SECONDS

        roundJob = scope.launch {
            // --- ФАЗА BETTING (60 сек) ---
            runBettingPhase()

            // --- ФАЗА ACTIVE (60 сек) ---
            runActivePhase()

            // --- ФАЗА CALCULATING (2 сек) ---
            runCalculatingPhase()

            // --- Новый раунд ---
            if (isRunning) {
                startNewRound()
            }
        }
    }

    private suspend fun runBettingPhase() {
        // Запоминаем абсолютное время окончания фазы
        phaseEndTime = System.currentTimeMillis() + Constants.BETTING_PHASE_SECONDS * 1000L

        // Запускаем генерацию ботов параллельно
        botJob = scope.launch {
            spawnBots()
        }

        // Цикл таймера на основе абсолютного времени (работает корректно при сворачивании)
        while (System.currentTimeMillis() < phaseEndTime) {
            // Пауза при потере соединения — сдвигаем дедлайн
            if (_isPaused.value) {
                val pauseStart = System.currentTimeMillis()
                while (_isPaused.value) {
                    delay(500)
                }
                phaseEndTime += System.currentTimeMillis() - pauseStart
            }

            val secondsLeft = ((phaseEndTime - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
            _timer.value = secondsLeft
            _currentRound.value = _currentRound.value.copy(timerSeconds = secondsLeft)

            // Отслеживаем направление цены для edge case
            trackPriceDirection()

            delay(200) // Обновляем чаще для плавности отображения
        }

        // Гарантируем сброс таймера в 0 при завершении фазы
        _timer.value = 0
        _currentRound.value = _currentRound.value.copy(timerSeconds = 0)

        // Останавливаем генерацию ботов при переходе из BETTING
        botJob?.cancel()
    }

    private suspend fun spawnBots() {
        val totalBots = botGenerator.generateBotCount()
        for (i in 0 until totalBots) {
            val delayMs = botGenerator.generateDelay()
            delay(delayMs)

            // Определяем тренд для бота
            val trend = lastPriceDirection
            val bot = botGenerator.generateBot(trend)

            // Добавляем бота в список
            val currentBots = _bots.value.toMutableList()
            currentBots.add(bot)
            _bots.value = currentBots

            // Обновляем пулы раунда
            val round = _currentRound.value
            val updatedRound = if (bot.bet.direction == BetDirection.UP) {
                round.copy(poolUp = round.poolUp + bot.bet.amount)
            } else {
                round.copy(poolDown = round.poolDown + bot.bet.amount)
            }
            _currentRound.value = recalculatePayouts(updatedRound)
        }
    }

    private suspend fun runActivePhase() {
        // Фиксация startPrice
        val startPrice = btcPriceSocket.latestPrice.value
        _currentRound.value = _currentRound.value.copy(
            phase = RoundPhase.ACTIVE,
            startPrice = startPrice,
            timerSeconds = Constants.ACTIVE_PHASE_SECONDS
        )
        _timer.value = Constants.ACTIVE_PHASE_SECONDS

        // Запоминаем абсолютное время окончания фазы
        phaseEndTime = System.currentTimeMillis() + Constants.ACTIVE_PHASE_SECONDS * 1000L

        // Цикл таймера на основе абсолютного времени (работает корректно при сворачивании)
        while (System.currentTimeMillis() < phaseEndTime) {
            // Пауза при потере соединения — сдвигаем дедлайн
            if (_isPaused.value) {
                val pauseStart = System.currentTimeMillis()
                while (_isPaused.value) {
                    delay(500)
                }
                phaseEndTime += System.currentTimeMillis() - pauseStart
            }

            val secondsLeft = ((phaseEndTime - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
            _timer.value = secondsLeft
            _currentRound.value = _currentRound.value.copy(timerSeconds = secondsLeft)
            trackPriceDirection()
            delay(200) // Обновляем чаще для плавности отображения
        }

        // Гарантируем сброс таймера в 0 при завершении фазы
        _timer.value = 0
        _currentRound.value = _currentRound.value.copy(timerSeconds = 0)
    }

    private suspend fun runCalculatingPhase() {
        val round = _currentRound.value
        val endPrice = btcPriceSocket.latestPrice.value
        val startPrice = round.startPrice ?: endPrice

        // Определение результата
        val result = when {
            endPrice > startPrice -> BetDirection.UP
            endPrice < startPrice -> BetDirection.DOWN
            else -> lastPriceDirection
        }

        val finalRound = round.copy(
            phase = RoundPhase.CALCULATING,
            endPrice = endPrice,
            result = result,
            timerSeconds = 0
        )
        _currentRound.value = finalRound
        _timer.value = 0

        // Расчёт выигрыша игрока
        val playerBet = _playerBet.value
        var playerPayout = 0.0
        var isWin = false

        if (playerBet != null) {
            if (playerBet.direction == result) {
                // Победа
                isWin = true
                val winningPool = if (result == BetDirection.UP) finalRound.poolUp else finalRound.poolDown
                val losingPool = if (result == BetDirection.UP) finalRound.poolDown else finalRound.poolUp
                val winnerPayoutTotal = losingPool * (1.0 - Constants.COMMISSION_PERCENT / 100.0)

                // Доля игрока = (ставка / победивший_пул) * общий_выигрыш
                playerPayout = if (winningPool > 0) {
                    playerBet.amount + (playerBet.amount / winningPool) * winnerPayoutTotal
                } else {
                    playerBet.amount
                }

                scope.launch {
                    balanceManager.addToBalance(playerPayout)
                }
            }
            // Проигрыш: деньги уже списаны при ставке
        }

        // Emit результат
        _roundResult.emit(RoundResult(finalRound, playerBet, playerPayout, isWin))

        // Добавляем в историю (in-memory)
        val history = _roundHistory.value.toMutableList()
        history.add(0, finalRound)
        if (history.size > 10) history.removeLast()
        _roundHistory.value = history

        // Сохраняем в Room
        val savedPayout = when {
            playerBet?.direction == BetDirection.UP -> finalRound.payoutUp
            playerBet?.direction == BetDirection.DOWN -> finalRound.payoutDown
            result == BetDirection.UP -> finalRound.payoutUp
            else -> finalRound.payoutDown
        }
        demoRoundDao.insert(
            DemoRoundEntity(
                startPrice = startPrice,
                endPrice = endPrice,
                result = result.name,
                playerBetDirection = playerBet?.direction?.name,
                playerBetAmount = playerBet?.amount,
                playerPayout = if (isWin) playerPayout else null,
                poolUpTotal = finalRound.poolUp,
                poolDownTotal = finalRound.poolDown,
                payoutMultiplier = savedPayout,
                timestamp = System.currentTimeMillis()
            )
        )

        // Пауза 2 секунды для показа результата
        delay(2000)
    }

    private fun trackPriceDirection() {
        val prices = btcPriceSocket.priceBuffer.value
        if (prices.size >= 2) {
            val prev = prices[prices.size - 2].price
            val curr = prices.last().price
            if (curr > prev) lastPriceDirection = BetDirection.UP
            else if (curr < prev) lastPriceDirection = BetDirection.DOWN
        }
    }

    private fun recalculatePayouts(round: Round): Round {
        val payoutUp = calculatePayout(round.poolUp, round.poolDown)
        val payoutDown = calculatePayout(round.poolDown, round.poolUp)
        return round.copy(payoutUp = payoutUp, payoutDown = payoutDown)
    }

    companion object {
        fun calculatePayout(pool: Double, oppositePool: Double): Double {
            if (pool == 0.0) return Constants.PAYOUT_BASE_RATE
            if (oppositePool == 0.0) return Constants.PAYOUT_BASE_RATE
            val raw = 1.0 + (oppositePool * (1.0 - Constants.COMMISSION_PERCENT / 100.0)) / pool
            return raw.coerceIn(Constants.PAYOUT_MIN, Constants.PAYOUT_MAX)
        }
    }
}
