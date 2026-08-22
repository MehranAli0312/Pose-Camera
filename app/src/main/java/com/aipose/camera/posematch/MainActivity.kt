package com.aipose.camera.posematch

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.aipose.camera.posematch.data.AppContainer
import com.aipose.camera.posematch.data.LocaleHelper
import com.aipose.camera.posematch.ui.viewmodel.MainViewModelFactory

/**
 * Single-Activity host. The whole app runs inside a Navigation-Component graph
 * ([R.navigation.nav_graph]); each destination is a Fragment (see ui/fragments). During the
 * Compose→XML migration those fragments host the existing composables via ComposeView.
 */
class MainActivity : FragmentActivity() {

    lateinit var appContainer: AppContainer
        private set

    // Apply the persisted per-app language before any UI is inflated.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
    }

    /** Shared ViewModel factory used by every fragment (Activity-scoped, single instance). */
    fun viewModelFactory() = MainViewModelFactory(application, appContainer.repository)
}
