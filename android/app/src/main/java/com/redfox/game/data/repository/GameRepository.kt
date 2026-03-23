package com.redfox.game.data.repository

import com.redfox.game.data.remote.api.GameApi
import com.redfox.game.data.remote.api.PlaceBetRequest
import com.redfox.game.data.remote.websocket.GameStateSocket
import com.redfox.game.domain.model.BetDirection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий игры — переключение между DEMO (локальный DemoGameService) и REAL (бэкенд).
 */
@Singleton
class GameRepository @Inject constructor(
    private val demoGameService: DemoGameService,
    private val gameStateSocket: GameStateSocket,
    private val gameApi: GameApi
) {
    enum class GameMode { DEMO, REAL }

    private var currentMode = GameMode.DEMO

    fun setMode(mode: GameMode) {
        currentMode = mode
    }

    fun getMode() = currentMode

    fun isDemoMode() = currentMode == GameMode.DEMO

    // Для DEMO — используем DemoGameService
    fun getDemoService() = demoGameService

    // Для REAL — используем GameStateSocket + GameApi
    fun getGameStateSocket() = gameStateSocket
    fun getGameApi() = gameApi

    suspend fun placeBetReal(direction: BetDirection, amount: Double): Result<String> {
        return try {
            val response = gameApi.placeBet(PlaceBetRequest(direction.name, amount))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBalanceReal(): Result<Pair<Double, Double>> {
        return try {
            val response = gameApi.getBalance()
            Result.success(response.balance_real to response.balance_demo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
