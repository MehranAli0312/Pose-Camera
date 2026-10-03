package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.domain.usecase.ThemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeUseCase: ThemeUseCase) : ViewModel() {

    private val _themeOption = MutableStateFlow(AppThemeOption.Dark)
    val themeOption = _themeOption.asStateFlow()

    private var isObserving = false

    fun loadTheme() {
        if (isObserving) return
        isObserving = true
        viewModelScope.launch {
            themeUseCase.getThemeOption().collect { option -> _themeOption.value = option }
        }
    }

    fun updateTheme(option: AppThemeOption) {
        if (_themeOption.value == option) return
        _themeOption.value = option
        viewModelScope.launch { themeUseCase.setThemeOption(option) }
    }
}
