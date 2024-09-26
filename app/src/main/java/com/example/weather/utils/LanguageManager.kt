package com.example.weather.utils

import com.example.weather.utils.enums.Language
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object SharedDataManager {

    private val _languageFlow = MutableSharedFlow<Language>(replay = 1)
    val languageFlow: SharedFlow<Language> = _languageFlow

    suspend fun emitLanguage(language: Language) {
        _languageFlow.emit(language)
    }
}
