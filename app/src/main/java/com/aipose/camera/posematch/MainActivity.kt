package com.aipose.camera.posematch

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.aipose.camera.posematch.data.AppContainer
import com.aipose.camera.posematch.data.LocaleHelper
import com.aipose.camera.posematch.ui.theme.ThemePrefs
import com.aipose.camera.posematch.ui.viewmodel.MainViewModelFactory

class MainActivity : FragmentActivity() {

    lateinit var appContainer: AppContainer
        private set

    // Apply the persisted language AND the Dark/Light night-mode before any UI is inflated, so
    // @color day/night resources resolve to the chosen theme on every API level.
    override fun attachBaseContext(newBase: Context) {
        val localized = LocaleHelper.wrap(newBase)
        super.attachBaseContext(applyNightMode(localized, ThemePrefs.isDark(newBase)))
    }

    private fun applyNightMode(context: Context, dark: Boolean): Context {
        val config = Configuration(context.resources.configuration)
        val mode = if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or mode
        return context.createConfigurationContext(config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)
        // Edge-to-edge with transparent bars; bar-icon contrast follows the chosen theme.
        val dark = ThemePrefs.isDark(this)
        val barStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
        else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        setContentView(R.layout.activity_main)
    }

    fun viewModelFactory() = MainViewModelFactory(application, appContainer.repository)
}
