package com.aipose.camera.posematch.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aipose.camera.posematch.MainActivity
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.databinding.FragmentOnboardingBinding
import com.aipose.camera.posematch.ui.screens.Routes
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.util.applySystemBarInsets
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel

/**
 * Onboarding — 3-page intro (real XML views). A local page index (0..2) drives the
 * illustration/title/description; Skip (pages 0-1) and the final "Let's Go" mark onboarding complete
 * and route to Language; "Next" advances the page. View Binding is used throughout.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val viewModel: MainViewModel by activityViewModels { (requireActivity() as MainActivity).viewModelFactory() }

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentOnboardingBinding.bind(view)

        val d = resources.displayMetrics.density
        val palette = paletteFor(view.context)
        accentArgb = palette.accent

        view.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )

        binding.onboardingHeader.applySystemBarInsets(top = true)
        binding.onboardingBottom.applySystemBarInsets(bottom = true)

        // Illustration: clip to 24dp rounded rect + 1dp glass border, then size to 85%h / 2:3.
        val image = binding.onboardingImage
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
        val imageBlock = binding.onboardingImageBlock
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

        // Next / Let's Go: rounded copper background with ripple.
        val buttonRadius = 24f * d
        val content = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE; cornerRadius = buttonRadius; setColor(accentArgb)
        }
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE; cornerRadius = buttonRadius; setColor(Color.WHITE)
        }
        binding.onboardingNextButton.background = RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), content, mask)

        binding.onboardingNextButton.setOnClickListener {
            if (currentPage < 2) {
                currentPage++
                render()
            } else {
                viewModel.setOnboardingCompleted()
                navigateToRoute(Routes.LANGUAGE)
            }
        }
        binding.onboardingSkipButton.setOnClickListener {
            viewModel.setOnboardingCompleted()
            navigateToRoute(Routes.LANGUAGE)
        }

        render()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /** Applies all per-page content: illustration, title, description, skip, dots, button label. */
    private fun render() {
        val ctx = requireContext()
        val resId = resources.getIdentifier(drawableNames[currentPage], "drawable", ctx.packageName)
        if (resId != 0) {
            binding.onboardingImage.setImageResource(resId)
        } else {
            binding.onboardingImage.setImageDrawable(null)
            binding.onboardingImage.setBackgroundColor(0xFF1F1F26.toInt())
        }

        val titles = intArrayOf(R.string.onboard_title_1, R.string.onboard_title_2, R.string.onboard_title_3)
        val descs = intArrayOf(R.string.onboard_desc_1, R.string.onboard_desc_2, R.string.onboard_desc_3)
        binding.onboardingTitle.setText(titles[currentPage])
        binding.onboardingDesc.setText(descs[currentPage])

        binding.onboardingSkipButton.visibility =
            if (currentPage < 2) View.VISIBLE else View.INVISIBLE

        val d = resources.displayMetrics.density
        val dots = listOf(binding.onboardingDot0, binding.onboardingDot1, binding.onboardingDot2)
        for (i in dots.indices) {
            val dot = dots[i]
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

        binding.onboardingNextText.setText(
            if (currentPage == 2) R.string.action_lets_go else R.string.action_next
        )
    }
}
