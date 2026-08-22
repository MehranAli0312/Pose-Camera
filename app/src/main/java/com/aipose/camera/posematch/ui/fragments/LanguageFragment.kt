package com.aipose.camera.posematch.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aipose.camera.posematch.MainActivity
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.Routes
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel

/**
 * Language selection — first-run flow. Real XML views (migration step 4); the actual UI logic lives
 * in the reusable [LanguageBinder] (also used by the Settings language dialog). Done persists the
 * choice and routes to the main container.
 */
class LanguageFragment : Fragment(R.layout.fragment_language) {

    private val viewModel: MainViewModel by activityViewModels { (requireActivity() as MainActivity).viewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LanguageBinder(this, viewModel, view) { navigateToRoute(Routes.MAIN_CONTAINER) }.bind()
    }
}
