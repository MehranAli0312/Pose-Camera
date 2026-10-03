package com.aipose.camera.posematch.ui.vm

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.usecase.LanguageUseCase
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.util.Locale

class LanguageViewModel(private val languageUseCase: LanguageUseCase) : ViewModel() {

    var currentLanguageCode by mutableStateOf("")
    var isLanguageLoaded by mutableStateOf(false)

    init {
        getLanguageCode()
    }

    fun changeLanguage(languageCode: String) {
        currentLanguageCode = languageCode

        viewModelScope.launch {
            languageUseCase.setLanguageCode(languageCode)
        }
    }

    fun applyLanguage(languageCode: String) {
        val requested = Locale.forLanguageTag(languageCode).withLatinDigits()
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val active = (if (appLocales.isEmpty) LocaleListCompat.getAdjustedDefault() else appLocales)[0]

        val isAlreadyActive = active != null &&
            active.language == requested.language &&
            (requested.country.isEmpty() || active.country == requested.country) &&
            active.getUnicodeLocaleType(NUMBERING_SYSTEM_KEY) ==
            requested.getUnicodeLocaleType(NUMBERING_SYSTEM_KEY)
        if (isAlreadyActive) return

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(requested))
    }

    private fun Locale.withLatinDigits(): Locale {
        if (DecimalFormatSymbols.getInstance(this).zeroDigit == '0') return this
        return Locale.Builder()
            .setLocale(this)
            .setUnicodeLocaleKeyword(NUMBERING_SYSTEM_KEY, LATIN_DIGITS)
            .build()
    }

    fun getLanguageCode() {
        viewModelScope.launch {
            languageUseCase.getLanguageCode().collect { savedLanguageCode ->
                currentLanguageCode = savedLanguageCode
                isLanguageLoaded = true
            }
        }
    }

    private companion object {
        const val NUMBERING_SYSTEM_KEY = "nu"
        const val LATIN_DIGITS = "latn"
    }
}
