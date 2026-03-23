package com.redfox.game.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

data class ProfileResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val country: String,
    val balance_real: Double,
    val balance_demo: Double,
    val kyc_status: String,
    val email_verified: Boolean,
    val rounds_played: Int,
    val wins: Int,
    val losses: Int,
    val profit: Double
)

data class UpdateProfileRequest(
    val nickname: String?,
    val country: String?
)

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)

interface ProfileApi {

    @GET("api/profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("api/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse

    @PUT("api/profile/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): MessageResponse
}
