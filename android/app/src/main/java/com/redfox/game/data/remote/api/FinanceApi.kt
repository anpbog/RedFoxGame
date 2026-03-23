package com.redfox.game.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class CreateDepositRequest(val network: String)

data class DepositResponse(
    val id: Long,
    val address: String,
    val network: String,
    val status: String
)

data class CreateWithdrawRequest(
    val network: String,
    val address: String,
    val amount: Double
)

data class TransactionResponse(
    val id: Long,
    val type: String,
    val amount: Double,
    val currency: String,
    val network: String,
    val status: String,
    val address: String?
)

data class MessageResponse(val message: String)

interface FinanceApi {

    @POST("api/deposit/create")
    suspend fun createDeposit(@Body request: CreateDepositRequest): DepositResponse

    @GET("api/deposit/status/{id}")
    suspend fun getDepositStatus(@Path("id") id: Long): TransactionResponse

    @GET("api/deposit/history")
    suspend fun getDepositHistory(): List<TransactionResponse>

    @POST("api/withdraw/create")
    suspend fun createWithdraw(@Body request: CreateWithdrawRequest): MessageResponse

    @GET("api/withdraw/history")
    suspend fun getWithdrawHistory(): List<TransactionResponse>
}
