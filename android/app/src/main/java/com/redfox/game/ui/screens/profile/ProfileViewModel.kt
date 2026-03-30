package com.redfox.game.ui.screens.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redfox.game.data.local.db.DemoRoundDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Период фильтрации статистики
enum class StatsPeriod { DAY, MONTH, ALL }

// Режим статистики: демо или реальная
enum class StatsMode { DEMO, REAL }

// Данные статистики
data class ProfileStats(
    val totalRounds: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val profit: Double = 0.0,
    val avgBet: Double = 0.0
)

// Состояние экрана профиля
data class ProfileState(
    val email: String = "",
    val period: StatsPeriod = StatsPeriod.ALL,
    val mode: StatsMode = StatsMode.DEMO,
    val stats: ProfileStats = ProfileStats(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val demoRoundDao: DemoRoundDao,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    init {
        loadEmail()
        loadStats()
    }

    // Загрузка email из DataStore
    private fun loadEmail() {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val email = prefs[stringPreferencesKey("auth_email")] ?: ""
                _state.value = _state.value.copy(email = email)
            }
        }
    }

    // Выбор периода фильтрации
    fun setPeriod(period: StatsPeriod) {
        _state.value = _state.value.copy(period = period)
        loadStats()
    }

    // Переключение демо/реал
    fun setMode(mode: StatsMode) {
        _state.value = _state.value.copy(mode = mode)
        loadStats()
    }

    // Загрузка статистики из Room
    fun loadStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val fromTimestamp = when (_state.value.period) {
                StatsPeriod.DAY -> System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                StatsPeriod.MONTH -> System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L
                StatsPeriod.ALL -> 0L
            }

            // Пока только демо-статистика (реальная будет после бэкенда)
            val stats = if (_state.value.mode == StatsMode.DEMO) {
                ProfileStats(
                    totalRounds = demoRoundDao.getRoundsForPeriod(fromTimestamp),
                    wins = demoRoundDao.getWinsForPeriod(fromTimestamp),
                    losses = demoRoundDao.getLossesForPeriod(fromTimestamp),
                    profit = demoRoundDao.getProfitForPeriod(fromTimestamp),
                    avgBet = demoRoundDao.getAvgBetForPeriod(fromTimestamp)
                )
            } else {
                // Реальная статистика — заглушка до бэкенда
                ProfileStats()
            }

            _state.value = _state.value.copy(stats = stats, isLoading = false)
        }
    }
}
