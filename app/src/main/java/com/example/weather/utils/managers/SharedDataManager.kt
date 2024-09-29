package com.example.weather.utils.managers

import com.example.weather.utils.enums.Language
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object SharedDataManager {

    private val _languageFlow = MutableSharedFlow<Language>(replay = 1)
    val languageFlow: SharedFlow<Language> = _languageFlow

    suspend fun emitLanguage(language: Language) {
        _languageFlow.emit(language)
    }

    private val _currentLocationFlow = MutableSharedFlow<Pair<Double, Double>?>(replay = 1)
    val currentLocationFlow: SharedFlow<Pair<Double,Double>?> = _currentLocationFlow

    suspend fun emitLocation(location: Pair<Double,Double>?) {
        _currentLocationFlow.emit(location)
    }
}
