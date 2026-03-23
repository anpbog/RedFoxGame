package com.redfox.game.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class BalanceResponse(
    val balance_real: Double,
    val balance_demo: Double
)

data class PlaceBetRequest(
    val direction: String,
    val amount: Double
)

data class PlaceBetResponse(
    val message: String
)

data class RoundResponse(
    val id: Long,
    val mode: String,
    val start_price: Double?,
    val end_price: Double?,
    val result: String?,
    val pool_up: Double,
    val pool_down: Double,
    val commission: Double
)

interface GameApi {

    @GET("api/game/balance")
    suspend fun getBalance(): BalanceResponse

    @POST("api/game/bet")
    suspend fun placeBet(@Body request: PlaceBetRequest): PlaceBetResponse

    @GET("api/game/history")
    suspend fun getRoundHistory(): List<RoundResponse>

    @GET("api/game/round/{id}")
    suspend fun getRound(@Path("id") id: Long): RoundResponse
}
