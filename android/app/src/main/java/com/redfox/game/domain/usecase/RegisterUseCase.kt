package com.redfox.game.domain.usecase

import android.util.Patterns
import com.redfox.game.data.repository.AuthRepository
import com.redfox.game.domain.model.User
import javax.inject.Inject

/**
 * UseCase для регистрации нового пользователя.
 * Валидация полей + вызов репозитория (локальное сохранение в DataStore).
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, passwordConfirmation: String): Result<User> {
        if (email.isBlank()) return Result.failure(Exception("Email не может быть пустым"))
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Некорректный email"))
        }
        if (password.length < 8) return Result.failure(Exception("Пароль минимум 8 символов"))
        if (password != passwordConfirmation) return Result.failure(Exception("Пароли не совпадают"))
        return authRepository.register(email.trim(), password, passwordConfirmation)
    }
}
