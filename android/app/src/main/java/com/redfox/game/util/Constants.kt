package com.redfox.game.util

object Constants {
    // URL бэкенда (будет заменён на реальный)
    const val BASE_URL = "https://api.redfoxgame.com/"

    // Binance WebSocket для цены BTC
    const val BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/btcusdt@trade"

    // Игровые константы
    const val BETTING_PHASE_SECONDS = 60
    const val ACTIVE_PHASE_SECONDS = 60
    const val CALCULATING_PHASE_SECONDS = 2
    const val COMMISSION_PERCENT = 15.0
    const val DEMO_MAX_BALANCE = 50000.0
    const val MIN_BET = 5.0
    const val MAX_BET = 5000.0
    const val DEMO_START_BALANCE = 5000.0

    // Payout
    const val PAYOUT_BASE_RATE = 1.0
    const val PAYOUT_MIN = 1.0
    const val PAYOUT_MAX = 6.0
}
