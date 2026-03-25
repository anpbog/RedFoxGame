package com.redfox.game.data.repository

import com.redfox.game.data.local.datastore.AuthDataStore
import com.redfox.game.domain.model.KycStatus
import com.redfox.game.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий авторизации — локальная реализация через DataStore.
 * Все данные хранятся на устройстве (без обращения к серверу).
 * При подключении бэкенда — заменить на серверную реализацию.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authDataStore: AuthDataStore
) {
    /**
     * Вход: проверяет email+пароль в локальном хранилище.
     * Если данные совпадают — устанавливает флаг isLoggedIn и возвращает User.
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Проверяем, зарегистрирован ли пользователь
            val savedEmail = authDataStore.getSavedEmail()
            if (savedEmail == null) {
                return Result.failure(Exception("Аккаунт не найден. Зарегистрируйтесь"))
            }
            // Проверяем совпадение email и пароля
            if (!authDataStore.verifyCredentials(email, password)) {
                return Result.failure(Exception("Неверный email или пароль"))
            }
            // Устанавливаем флаг авторизации
            authDataStore.setLoggedIn(true)
            Result.success(createLocalUser(email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Регистрация: сохраняет email+пароль в DataStore.
     * Если пользователь с таким email уже зарегистрирован — ошибка.
     */
    suspend fun register(email: String, password: String, passwordConfirmation: String): Result<User> {
        return try {
            // Проверяем, не зарегистрирован ли уже этот email
            if (authDataStore.isRegistered(email)) {
                return Result.failure(Exception("Пользователь с таким email уже зарегистрирован"))
            }
            // Сохраняем данные и автоматически логиним
            authDataStore.saveUser(email, password)
            Result.success(createLocalUser(email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Сброс пароля — в локальной версии информируем, что функция недоступна без сервера.
     */
    suspend fun resetPassword(email: String): Result<String> {
        return try {
            if (!authDataStore.isRegistered(email)) {
                return Result.failure(Exception("Аккаунт с таким email не найден"))
            }
            Result.success("Сброс пароля будет доступен после подключения сервера. Обратитесь в поддержку")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Проверяет, залогинен ли пользователь (для автовхода при запуске).
     */
    suspend fun isLoggedIn(): Boolean {
        return authDataStore.isLoggedIn()
    }

    /**
     * Выход из аккаунта — сбрасывает флаг isLoggedIn.
     * Данные регистрации (email, пароль) сохраняются для повторного входа.
     */
    suspend fun logout() {
        authDataStore.clear()
    }

    /**
     * Возвращает email текущего пользователя.
     */
    suspend fun getCurrentEmail(): String? {
        return authDataStore.getSavedEmail()
    }

    /**
     * Создаёт локальный объект User на основе email.
     * Заполняет демо-значения для полей, которые в будущем придут с сервера.
     */
    private fun createLocalUser(email: String): User {
        val trimmedEmail = email.trim().lowercase()
        val nickname = trimmedEmail.substringBefore("@")
        return User(
            id = 0L,
            email = trimmedEmail,
            nickname = nickname,
            country = "",
            balanceReal = 0.0,
            balanceDemo = 10000.0,
            kycStatus = KycStatus.NONE,
            emailVerified = false
        )
    }
}
