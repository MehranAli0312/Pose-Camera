package com.aipose.camera.posematch

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.aipose.camera.posematch.data.AppContainer
import com.aipose.camera.posematch.ui.theme.ThemePrefs
import com.aipose.camera.posematch.ui.viewmodel.MainViewModelFactory

class MainActivity : FragmentActivity() {

    lateinit var appContainer: AppContainer
        private set

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(applyNightMode(newBase, ThemePrefs.isDark(newBase)))
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
        hideNavigationBar()
    }

    /**
     * Hides the system navigation bar (on-screen back/home/recents, or the gesture pill) across
     * every screen — it sat directly on top of the bottom ad slot and made the ads look cramped.
     * The status bar is left alone.
     *
     * BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE means the user can still swipe up from the bottom edge
     * to bring the bar back temporarily; it overlays the content and auto-hides again, so the
     * layout never reflows and the ad slot keeps its size.
     */
    private fun hideNavigationBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    /**
     * Re-hide after any other window takes focus — full-screen ad activities (interstitial,
     * app open), dialogs and the transient bar itself all clear the flag on the way back.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideNavigationBar()
    }

    fun viewModelFactory() = MainViewModelFactory(application, appContainer.repository)
}
