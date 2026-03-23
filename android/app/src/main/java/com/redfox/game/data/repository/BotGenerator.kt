package com.redfox.game.data.repository

import com.redfox.game.domain.model.Bet
import com.redfox.game.domain.model.BetDirection
import com.redfox.game.domain.model.BotPlayer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class BotGenerator @Inject constructor() {

    private val names = listOf(
        "Alex", "CryptoKing", "Luna_Trader", "SatoshiFan", "BitMaster",
        "TradeHawk", "NightOwl", "BullRunner", "DiamondHands", "MoonShot",
        "WhaleAlert", "CoinSniper", "BlockSmith", "HashRate", "DeFiPro",
        "AltSeason", "GreenCandle", "RedAlert", "PumpKing", "TokenVault",
        "ChainLink", "GasPrice", "LiqPool", "StakeKing", "YieldFarm",
        "NFT_Lord", "MetaTrader", "FlashBot", "ArbiPro", "SwapMaster"
    )

    private val flags = listOf(
        "\uD83C\uDDF7\uD83C\uDDFA", // RU
        "\uD83C\uDDFA\uD83C\uDDF8", // US
        "\uD83C\uDDEC\uD83C\uDDE7", // GB
        "\uD83C\uDDE9\uD83C\uDDEA", // DE
        "\uD83C\uDDEF\uD83C\uDDF5", // JP
        "\uD83C\uDDE7\uD83C\uDDF7", // BR
        "\uD83C\uDDF0\uD83C\uDDF7", // KR
        "\uD83C\uDDFA\uD83C\uDDE6", // UA
        "\uD83C\uDDF9\uD83C\uDDF7", // TR
        "\uD83C\uDDEE\uD83C\uDDF3"  // IN
    )

    // Использованные имена в текущем раунде (чтобы не повторять)
    private val usedNames = mutableSetOf<String>()

    fun resetForNewRound() {
        usedNames.clear()
    }

    /**
     * Генерирует одного бота с учётом текущего тренда.
     * @param trendDirection текущий тренд цены (UP/DOWN)
     * @return BotPlayer со случайным именем, флагом и ставкой
     */
    fun generateBot(trendDirection: BetDirection): BotPlayer {
        // Выбираем неиспользованное имя
        val availableNames = names.filter { it !in usedNames }
        val name = if (availableNames.isNotEmpty()) {
            availableNames.random().also { usedNames.add(it) }
        } else {
            // Все имена использованы — генерируем с суффиксом
            val baseName = names.random()
            "${baseName}_${Random.nextInt(100, 999)}"
        }

        val flag = flags.random()

        // 60% в сторону тренда, 40% против
        val direction = if (Random.nextDouble() < 0.6) trendDirection else {
            if (trendDirection == BetDirection.UP) BetDirection.DOWN else BetDirection.UP
        }

        // Сумма ставки: $5–$200, с шагом $5
        val amount = (Random.nextInt(1, 41) * 5).toDouble()

        return BotPlayer(
            name = name,
            countryFlag = flag,
            bet = Bet(direction, amount)
        )
    }

    /**
     * Генерирует количество ботов для раунда (2–8).
     */
    fun generateBotCount(): Int = Random.nextInt(2, 9)

    /**
     * Генерирует задержку между появлением ботов (5–15 сек).
     */
    fun generateDelay(): Long = Random.nextLong(5000, 15001)
}
