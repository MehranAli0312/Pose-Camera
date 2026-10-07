package com.aipose.camera.posematch.ui.models

import androidx.annotation.DrawableRes
import com.aipose.camera.posematch.R
import java.util.Locale

data class LanguageItem(
    val name: String,
    val code: String,
    @param:DrawableRes @get:DrawableRes val flag: Int
)

private const val DEFAULT_LANGUAGE_CODE = "en"

val allLanguageItems = listOf(
    LanguageItem("English", "en", R.drawable.ic_lang_english),
    LanguageItem("العربية", "ar", R.drawable.ic_lang_arabic),
    LanguageItem("中国人", "zh-CN", R.drawable.ic_lang_chinese_s),
    LanguageItem("Français", "fr", R.drawable.ic_lang_french),
    LanguageItem("Dansk", "da", R.drawable.ic_lang_danish),
    LanguageItem("Deutsch", "de", R.drawable.ic_lang_german),
    LanguageItem("हिंदी", "hi", R.drawable.ic_lang_hindi),
    LanguageItem("Indonesia", "in", R.drawable.ic_lang_indonesian),
    LanguageItem("Italiana", "it", R.drawable.ic_lang_italian),
    LanguageItem("日本語", "ja", R.drawable.ic_lang_japanese),
    LanguageItem("한국인", "ko", R.drawable.ic_lang_korean),
    LanguageItem("Nederlands", "nl", R.drawable.ic_lang_dutch),
    LanguageItem("ελληνικά", "el", R.drawable.ic_lang_greek),
    LanguageItem("Polski", "pl", R.drawable.ic_lang_polish),
    LanguageItem("Português", "pt", R.drawable.ic_lang_portuguese),
    LanguageItem("Русский", "ru", R.drawable.ic_lang_russian),
    LanguageItem("Española", "es", R.drawable.ic_lang_spanish),
    LanguageItem("Svenska", "sv", R.drawable.ic_lang_swedish),
    LanguageItem("ไทย", "th", R.drawable.ic_lang_thai),
    LanguageItem("Türkçe", "tr", R.drawable.ic_lang_turkish),
    LanguageItem("українська", "uk", R.drawable.ic_lang_ukrainian),
    LanguageItem("Tiếng Việt", "vi", R.drawable.ic_lang_vietnamese)
)

fun resolveDefaultLanguageCode(locale: Locale = Locale.getDefault()): String {
    val languageCode = locale.language.lowercase(Locale.ROOT)
    return allLanguageItems.find { it.code.equals(languageCode, ignoreCase = true) }?.code
        ?: DEFAULT_LANGUAGE_CODE
}
