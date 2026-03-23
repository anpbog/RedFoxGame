package com.redfox.game.domain.usecase

import com.redfox.game.data.repository.AuthRepository
import com.redfox.game.domain.model.User
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) return Result.failure(Exception("Email не может быть пустым"))
        if (password.length < 8) return Result.failure(Exception("Пароль минимум 8 символов"))
        return authRepository.login(email.trim(), password)
    }
}
