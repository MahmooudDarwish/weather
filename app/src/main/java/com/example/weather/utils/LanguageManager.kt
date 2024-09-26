import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class LanguageManager(private val sharedPreferences: SharedPreferences) {

    private val _languageFlow = MutableSharedFlow<String>(replay = 1)
    val languageFlow = _languageFlow.asSharedFlow()

    init {
        _languageFlow.tryEmit(getSavedLanguage())
    }

    fun setLanguage(languageCode: String) {
        sharedPreferences.edit().putString("language_code", languageCode).apply()
        _languageFlow.tryEmit(languageCode)
    }

    fun getSavedLanguage(): String {
        return sharedPreferences.getString("language_code", "en") ?: "en"
    }
}
