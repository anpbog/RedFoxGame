package com.redfox.game.domain.usecase

import android.util.Patterns
import com.redfox.game.data.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase для сброса пароля.
 * Валидация email + вызов репозитория.
 * В локальной версии — заглушка (без сервера сброс невозможен).
 */
class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<String> {
        if (email.isBlank()) return Result.failure(Exception("Email не может быть пустым"))
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Некорректный email"))
        }
        return authRepository.resetPassword(email.trim())
    }
}
