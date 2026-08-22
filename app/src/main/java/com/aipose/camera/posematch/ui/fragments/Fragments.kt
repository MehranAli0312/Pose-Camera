package com.aipose.camera.posematch.ui.fragments

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.Routes

// ---- Shared helpers ----------------------------------------------------------------------
// Splash, Onboarding, Language and the Main container are now real XML fragments in their own files.

/** Maps the screens' route strings onto Fragment destinations, popping the current screen. */
internal fun Fragment.navigateToRoute(route: String) {
    val nav = findNavController()
    val dest = when (route) {
        Routes.SPLASH -> R.id.splashFragment
        Routes.ONBOARDING -> R.id.onboardingFragment
        Routes.LANGUAGE -> R.id.languageFragment
        Routes.MAIN_CONTAINER -> R.id.mainFragment
        else -> return
    }
    val current = nav.currentDestination?.id
    nav.navigate(dest, null, navOptions {
        if (current != null) popUpTo(current) { inclusive = true }
    })
}
