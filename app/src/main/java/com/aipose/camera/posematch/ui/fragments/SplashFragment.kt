package com.aipose.camera.posematch.ui.fragments

import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.PathInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.aipose.camera.posematch.MainActivity
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.Routes
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash — first screen converted from Compose to real XML views (migration step 2).
 *
 * Behaviour is unchanged from the old [com.aipose.camera.posematch.ui.screens.SplashScreen]:
 * after a 300 ms beat the logo/title fade + scale in over 1.5 s, the screen holds, then routes to
 * Onboarding (first launch) or the main container (returning user). The gradient background and the
 * logo's accent border are taken from the persisted theme's palette so the look matches every theme.
 */
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val viewModel: MainViewModel by activityViewModels { (requireActivity() as MainActivity).viewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val density = resources.displayMetrics.density
        val palette = paletteFor(viewModel.appTheme.value)

        // Vertical gradient background (bgTop -> bgBottom), matching StudioBackgroundGradient.
        view.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )

        // Logo: clip to a 28dp rounded rect and overlay a 1.5dp accent (copper) border.
        val cornerPx = 28f * density
        val logo = view.findViewById<ImageView>(R.id.splash_logo)
        logo.clipToOutline = true
        logo.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(v: View, outline: Outline) {
                outline.setRoundRect(0, 0, v.width, v.height, cornerPx)
            }
        }
        logo.foreground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerPx
            setColor(Color.TRANSPARENT)
            setStroke((1.5f * density).toInt().coerceAtLeast(1), palette.accent)
        }

        // Enter animation + timed navigation (same 300ms delay / 1500ms fade+scale / 2200ms hold).
        val content = view.findViewById<View>(R.id.splash_content)
        content.alpha = 0f
        content.scaleX = 0.85f
        content.scaleY = 0.85f

        viewLifecycleOwner.lifecycleScope.launch {
            delay(300)
            content.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1500)
                .setInterpolator(PathInterpolator(0f, 0f, 0.2f, 1f)) // LinearOutSlowIn
                .start()
            delay(2200)
            val target = if (viewModel.onboardingCompleted.value) Routes.MAIN_CONTAINER else Routes.ONBOARDING
            navigateToRoute(target)
        }
    }
}
