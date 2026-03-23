package com.redfox.game.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "demo_rounds")
data class DemoRoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startPrice: Double,
    val endPrice: Double,
    val result: String,
    val playerBetDirection: String?,
    val playerBetAmount: Double?,
    val playerPayout: Double?,
    val poolUpTotal: Double,
    val poolDownTotal: Double,
    val payoutMultiplier: Double,
    val timestamp: Long
)
