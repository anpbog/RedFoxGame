package com.redfox.game.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Локальное хранилище данных авторизации через DataStore.
 * Хранит email, хэш пароля (SHA-256) и флаг авторизации.
 * Используется как временное решение до подключения бэкенда.
 */
@Singleton
class AuthDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_EMAIL = stringPreferencesKey("auth_email")
        private val KEY_PASSWORD_HASH = stringPreferencesKey("auth_password_hash")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("auth_is_logged_in")
    }

    /**
     * Сохраняет данные регистрации: email и хэш пароля.
     * Также устанавливает флаг isLoggedIn = true (автовход после регистрации).
     */
    suspend fun saveUser(email: String, password: String) {
        dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email.trim().lowercase()
            prefs[KEY_PASSWORD_HASH] = hashPassword(password)
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    /**
     * Проверяет совпадение email и пароля с сохранёнными данными.
     * Возвращает true, если пользователь зарегистрирован и данные верные.
     */
    suspend fun verifyCredentials(email: String, password: String): Boolean {
        val prefs = dataStore.data.first()
        val savedEmail = prefs[KEY_EMAIL] ?: return false
        val savedHash = prefs[KEY_PASSWORD_HASH] ?: return false
        return savedEmail == email.trim().lowercase() && savedHash == hashPassword(password)
    }

    /**
     * Устанавливает флаг авторизации (isLoggedIn).
     */
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = isLoggedIn
        }
    }

    /**
     * Возвращает true, если пользователь залогинен.
     */
    suspend fun isLoggedIn(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[KEY_IS_LOGGED_IN] == true
    }

    /**
     * Возвращает сохранённый email или null, если пользователь не зарегистрирован.
     */
    suspend fun getSavedEmail(): String? {
        val prefs = dataStore.data.first()
        return prefs[KEY_EMAIL]
    }

    /**
     * Проверяет, зарегистрирован ли пользователь с данным email.
     */
    suspend fun isRegistered(email: String): Boolean {
        val prefs = dataStore.data.first()
        val savedEmail = prefs[KEY_EMAIL] ?: return false
        return savedEmail == email.trim().lowercase()
    }

    /**
     * Очистка флага авторизации (logout).
     * Email и хэш пароля НЕ удаляем — чтобы пользователь мог залогиниться снова.
     */
    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_IS_LOGGED_IN)
        }
    }

    /**
     * Полная очистка всех данных (для сброса пароля/удаления аккаунта).
     */
    suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_PASSWORD_HASH)
            prefs.remove(KEY_IS_LOGGED_IN)
        }
    }

    /**
     * Обновляет пароль для зарегистрированного пользователя.
     */
    suspend fun updatePassword(newPassword: String) {
        dataStore.edit { prefs ->
            prefs[KEY_PASSWORD_HASH] = hashPassword(newPassword)
        }
    }

    /**
     * Хэширование пароля через SHA-256.
     * Простая реализация для демо-версии (без bcrypt/scrypt).
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
