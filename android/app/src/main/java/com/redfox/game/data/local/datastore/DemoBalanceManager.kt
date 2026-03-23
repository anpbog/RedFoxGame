package com.redfox.game.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.redfox.game.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoBalanceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val balanceKey = doublePreferencesKey("demo_balance")

    val balance: Flow<Double> = dataStore.data.map { prefs ->
        prefs[balanceKey] ?: Constants.DEMO_START_BALANCE
    }

    suspend fun getBalance(): Double {
        var result = Constants.DEMO_START_BALANCE
        dataStore.data.collect { prefs ->
            result = prefs[balanceKey] ?: Constants.DEMO_START_BALANCE
        }
        return result
    }

    suspend fun updateBalance(newBalance: Double) {
        dataStore.edit { prefs ->
            prefs[balanceKey] = newBalance
        }
    }

    suspend fun addToBalance(amount: Double) {
        dataStore.edit { prefs ->
            val current = prefs[balanceKey] ?: Constants.DEMO_START_BALANCE
            prefs[balanceKey] = current + amount
        }
    }

    suspend fun subtractFromBalance(amount: Double) {
        dataStore.edit { prefs ->
            val current = prefs[balanceKey] ?: Constants.DEMO_START_BALANCE
            prefs[balanceKey] = (current - amount).coerceAtLeast(0.0)
        }
    }

    suspend fun resetBalance() {
        dataStore.edit { prefs ->
            prefs[balanceKey] = Constants.DEMO_START_BALANCE
        }
    }
}
