package com.redfox.game.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.redfox.game.data.remote.api.AuthApi
import com.redfox.game.data.remote.dto.LoginRequest
import com.redfox.game.data.remote.dto.RefreshTokenRequest
import com.redfox.game.data.remote.dto.RegisterRequest
import com.redfox.game.data.remote.dto.ResetPasswordRequest
import com.redfox.game.data.remote.dto.UserDto
import com.redfox.game.domain.model.AuthToken
import com.redfox.game.domain.model.KycStatus
import com.redfox.game.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                saveTokens(body.accessToken, body.refreshToken, body.expiresIn)
                Result.success(body.user.toDomain())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Ошибка входа"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, passwordConfirmation: String): Result<User> {
        return try {
            val response = authApi.register(RegisterRequest(email, password, passwordConfirmation))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                saveTokens(body.accessToken, body.refreshToken, body.expiresIn)
                Result.success(body.user.toDomain())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Ошибка регистрации"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<String> {
        return try {
            val response = authApi.resetPassword(ResetPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Ошибка"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(): Result<AuthToken> {
        val savedRefreshToken = getRefreshToken() ?: return Result.failure(Exception("Нет refresh token"))
        return try {
            val response = authApi.refreshToken(RefreshTokenRequest(savedRefreshToken))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                saveTokens(body.accessToken, body.refreshToken, body.expiresIn)
                Result.success(AuthToken(body.accessToken, body.refreshToken, body.expiresIn))
            } else {
                clearTokens()
                Result.failure(Exception("Сессия истекла"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun isLoggedIn(): Boolean = getRefreshToken() != null

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_IN)
            .apply()
    }

    private fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_IN, System.currentTimeMillis() + expiresIn * 1000)
            .apply()
    }

    private fun UserDto.toDomain(): User = User(
        id = id,
        email = email,
        nickname = nickname,
        country = country,
        balanceReal = balanceReal,
        balanceDemo = balanceDemo,
        kycStatus = try { KycStatus.valueOf(kycStatus.uppercase()) } catch (_: Exception) { KycStatus.NONE },
        emailVerified = emailVerified
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_IN = "expires_in"
    }
}
