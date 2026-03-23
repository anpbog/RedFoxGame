package com.redfox.game.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String
)

data class ResetPasswordRequest(
    val email: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class AuthResponse(
    val user: UserDto,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: Long
)

data class UserDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val country: String,
    @SerializedName("balance_real")
    val balanceReal: Double,
    @SerializedName("balance_demo")
    val balanceDemo: Double,
    @SerializedName("kyc_status")
    val kycStatus: String,
    @SerializedName("email_verified")
    val emailVerified: Boolean
)

data class MessageResponse(
    val message: String
)
