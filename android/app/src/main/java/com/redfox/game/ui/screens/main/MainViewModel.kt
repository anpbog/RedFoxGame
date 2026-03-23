package com.redfox.game.ui.screens.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val languageKey = stringPreferencesKey("app_language")

    private val _language = MutableStateFlow("ru")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data.map { prefs ->
                prefs[languageKey] ?: "ru"
            }.collect { lang ->
                _language.value = lang
            }
        }
    }

    fun toggleLanguage() {
        val newLang = if (_language.value == "ru") "en" else "ru"
        _language.value = newLang
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[languageKey] = newLang
            }
        }
    }
}
