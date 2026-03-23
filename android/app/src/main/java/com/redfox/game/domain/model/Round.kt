package com.redfox.game.domain.model

enum class RoundPhase {
    BETTING,
    ACTIVE,
    CALCULATING
}

enum class BetDirection {
    UP,
    DOWN
}

data class Round(
    val id: Int = 0,
    val phase: RoundPhase = RoundPhase.BETTING,
    val startPrice: Double? = null,
    val endPrice: Double? = null,
    val result: BetDirection? = null,
    val poolUp: Double = 0.0,
    val poolDown: Double = 0.0,
    val payoutUp: Double = 1.0,
    val payoutDown: Double = 1.0,
    val timerSeconds: Int = 0
)

data class Bet(
    val direction: BetDirection,
    val amount: Double
)

data class BotPlayer(
    val name: String,
    val countryFlag: String,
    val bet: Bet
)
