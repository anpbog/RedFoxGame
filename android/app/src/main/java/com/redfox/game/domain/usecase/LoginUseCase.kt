package com.redfox.game.domain.usecase

import android.util.Patterns
import com.redfox.game.data.repository.AuthRepository
import com.redfox.game.domain.model.User
import javax.inject.Inject

/**
 * UseCase для входа в аккаунт.
 * Валидация полей + вызов репозитория (локальная авторизация через DataStore).
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) return Result.failure(Exception("Email не может быть пустым"))
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return Result.failure(Exception("Некорректный формат email"))
        if (password.length < 8) return Result.failure(Exception("Пароль минимум 8 символов"))
        return authRepository.login(email.trim(), password)
    }
}
