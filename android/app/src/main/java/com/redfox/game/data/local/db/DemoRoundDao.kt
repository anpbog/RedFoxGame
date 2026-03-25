package com.redfox.game.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DemoRoundDao {

    @Insert
    suspend fun insert(round: DemoRoundEntity)

    @Query("SELECT * FROM demo_rounds ORDER BY timestamp DESC LIMIT 10")
    fun getLast10(): Flow<List<DemoRoundEntity>>

    @Query("SELECT COUNT(*) FROM demo_rounds WHERE playerBetDirection IS NOT NULL AND playerBetDirection = result")
    suspend fun getWins(): Int

    @Query("SELECT COUNT(*) FROM demo_rounds WHERE playerBetDirection IS NOT NULL AND playerBetDirection != result")
    suspend fun getLosses(): Int

    @Query("SELECT COALESCE(SUM(COALESCE(playerPayout, 0) - playerBetAmount), 0.0) FROM demo_rounds WHERE playerBetDirection IS NOT NULL")
    suspend fun getTotalProfit(): Double

    @Query("SELECT COUNT(*) FROM demo_rounds WHERE playerBetDirection IS NOT NULL")
    suspend fun getTotalRounds(): Int

    // Запросы с фильтрацией по периоду времени (для экрана профиля)

    @Query("SELECT COUNT(*) FROM demo_rounds WHERE timestamp >= :fromTimestamp AND playerBetDirection IS NOT NULL")
    suspend fun getRoundsForPeriod(fromTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM demo_rounds WHERE timestamp >= :fromTimestamp AND playerBetDirection IS NOT NULL AND playerPayout IS NOT NULL AND playerPayout > 0")
    suspend fun getWinsForPeriod(fromTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM demo_rounds WHERE timestamp >= :fromTimestamp AND playerBetDirection IS NOT NULL AND (playerPayout IS NULL OR playerPayout <= 0)")
    suspend fun getLossesForPeriod(fromTimestamp: Long): Int

    @Query("SELECT COALESCE(SUM(COALESCE(playerPayout, 0) - playerBetAmount), 0.0) FROM demo_rounds WHERE timestamp >= :fromTimestamp AND playerBetDirection IS NOT NULL")
    suspend fun getProfitForPeriod(fromTimestamp: Long): Double

    @Query("SELECT COALESCE(AVG(playerBetAmount), 0.0) FROM demo_rounds WHERE timestamp >= :fromTimestamp AND playerBetDirection IS NOT NULL")
    suspend fun getAvgBetForPeriod(fromTimestamp: Long): Double
}
