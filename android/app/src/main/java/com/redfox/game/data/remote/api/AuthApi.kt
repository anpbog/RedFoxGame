package com.redfox.game.data.remote.api

import com.redfox.game.data.remote.dto.AuthResponse
import com.redfox.game.data.remote.dto.LoginRequest
import com.redfox.game.data.remote.dto.MessageResponse
import com.redfox.game.data.remote.dto.RefreshTokenRequest
import com.redfox.game.data.remote.dto.RegisterRequest
import com.redfox.game.data.remote.dto.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>
}
