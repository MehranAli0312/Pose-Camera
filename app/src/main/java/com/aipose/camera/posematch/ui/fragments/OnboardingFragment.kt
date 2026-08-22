package com.aipose.camera.posematch.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aipose.camera.posematch.MainActivity
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.Routes
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel

/**
 * Onboarding — 3-page intro converted from Compose to real XML views (migration step 3).
 *
 * Behaviour matches the old [com.aipose.camera.posematch.ui.screens.OnboardingScreen]: a local page
 * index (0..2) drives the illustration/title/description; Skip (pages 0-1) and the final "Let's Go"
 * both mark onboarding complete and route to Language; "Next" advances the page. Theme-driven colours
 * (gradient, glass border, accent dots/button) come from the persisted palette.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val viewModel: MainViewModel by activityViewModels { (requireActivity() as MainActivity).viewModelFactory() }

    private var currentPage = 0
    private var accentArgb = 0
    private val darkGray = 0xFF444444.toInt()

    private val drawableNames = arrayOf(
        "pose_guide_onboard1_1781797560457",
        "pose_guide_onboard2_1781797579198",
        "pose_guide_onboard3_1781797601454"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val d = resources.displayMetrics.density
        val palette = paletteFor(viewModel.appTheme.value)
        accentArgb = palette.accent

        // Vertical gradient background, matching StudioBackgroundGradient.
        view.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )

        // Insets: status bar on the header, nav bar on the bottom panel (statusBarsPadding /
        // navigationBarsPadding in the original), added on top of the 24dp content padding.
        val header = view.findViewById<View>(R.id.onboarding_header)
        val bottom = view.findViewById<View>(R.id.onboarding_bottom)
        val baseHeaderTop = header.paddingTop
        val baseBottom = bottom.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header.updatePadding(top = baseHeaderTop + bars.top)
            bottom.updatePadding(bottom = baseBottom + bars.bottom)
            insets
        }

        // Illustration: clip to 24dp rounded rect + 1dp glass border, then size to 85%h / 2:3.
        val image = view.findViewById<ImageView>(R.id.onboarding_image)
        val cornerPx = 24f * d
        image.clipToOutline = true
        image.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(v: View, outline: Outline) {
                outline.setRoundRect(0, 0, v.width, v.height, cornerPx)
            }
        }
        image.foreground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerPx
            setColor(Color.TRANSPARENT)
            setStroke((1f * d).toInt().coerceAtLeast(1), palette.glass)
        }
        val imageBlock = view.findViewById<View>(R.id.onboarding_image_block)
        val sizeImage = {
            val innerH = imageBlock.height - imageBlock.paddingTop - imageBlock.paddingBottom
            if (innerH > 0) {
                val h = (0.85f * innerH).toInt()
                val w = (h * 2f / 3f).toInt()
                val lp = image.layoutParams
                if (lp.width != w || lp.height != h) {
                    lp.width = w; lp.height = h; image.layoutParams = lp
                }
            }
        }
        imageBlock.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> sizeImage() }
        imageBlock.post { sizeImage() }

        // Next / Let's Go: rounded copper background with ripple, matching the Compose Button.
        val nextButton = view.findViewById<View>(R.id.onboarding_next_button)
        val buttonRadius = 24f * d
        val content = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE; cornerRadius = buttonRadius; setColor(accentArgb)
        }
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE; cornerRadius = buttonRadius; setColor(Color.WHITE)
        }
        nextButton.background = RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), content, mask)

        nextButton.setOnClickListener {
            if (currentPage < 2) {
                currentPage++
                render(view)
            } else {
                viewModel.setOnboardingCompleted()
                navigateToRoute(Routes.LANGUAGE)
            }
        }
        view.findViewById<View>(R.id.onboarding_skip_button).setOnClickListener {
            viewModel.setOnboardingCompleted()
            navigateToRoute(Routes.LANGUAGE)
        }

        render(view)
    }

    /** Applies all per-page content: illustration, title, description, skip, dots, button label. */
    private fun render(view: View) {
        val ctx = requireContext()
        val resId = resources.getIdentifier(drawableNames[currentPage], "drawable", ctx.packageName)
        val image = view.findViewById<ImageView>(R.id.onboarding_image)
        if (resId != 0) {
            image.setImageResource(resId)
        } else {
            image.setImageDrawable(null)
            image.setBackgroundColor(0xFF1F1F26.toInt())
        }

        val titles = intArrayOf(R.string.onboard_title_1, R.string.onboard_title_2, R.string.onboard_title_3)
        val descs = intArrayOf(R.string.onboard_desc_1, R.string.onboard_desc_2, R.string.onboard_desc_3)
        view.findViewById<TextView>(R.id.onboarding_title).setText(titles[currentPage])
        view.findViewById<TextView>(R.id.onboarding_desc).setText(descs[currentPage])

        // Skip hidden (space kept) on the last page.
        view.findViewById<View>(R.id.onboarding_skip_button).visibility =
            if (currentPage < 2) View.VISIBLE else View.INVISIBLE

        val d = resources.displayMetrics.density
        val dots = intArrayOf(R.id.onboarding_dot_0, R.id.onboarding_dot_1, R.id.onboarding_dot_2)
        for (i in dots.indices) {
            val dot = view.findViewById<View>(dots[i])
            val active = currentPage == i
            val lp = dot.layoutParams
            lp.width = ((if (active) 18f else 8f) * d).toInt()
            dot.layoutParams = lp
            dot.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 4f * d
                setColor(if (active) accentArgb else darkGray)
            }
        }

        view.findViewById<TextView>(R.id.onboarding_next_text).setText(
            if (currentPage == 2) R.string.action_lets_go else R.string.action_next
        )
    }
}
