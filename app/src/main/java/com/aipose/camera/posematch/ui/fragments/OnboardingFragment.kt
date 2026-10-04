package com.aipose.camera.posematch.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.aipose.camera.posematch.MainActivity
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.databinding.FragmentOnboardingBinding
import com.aipose.camera.posematch.databinding.ItemOnboardingBinding
import com.aipose.camera.posematch.ui.screens.Routes
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import com.aipose.camera.posematch.analytics.Analytics

/**
 * Onboarding — 3-page intro using ViewPager2 for swiping, matching the reference design.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val viewModel: MainViewModel by activityViewModels { (requireActivity() as MainActivity).viewModelFactory() }

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    // Design-specific accent colors for each page
    private val onboardingPages = listOf(
        OnboardingPage(R.string.onboard_title_1, R.string.onboard_desc_1, R.drawable.il_onboard_step1, 0xFF3B82F6.toInt()),
        OnboardingPage(R.string.onboard_title_2, R.string.onboard_desc_2, R.drawable.il_onboard_step2, 0xFF8B5CF6.toInt()),
        OnboardingPage(R.string.onboard_title_3, R.string.onboard_desc_3, R.drawable.il_onboard_step3, 0xFF10B981.toInt())
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Analytics.screen(Analytics.Screen.ONBOARDING)
        _binding = FragmentOnboardingBinding.bind(view)

        // Set static dark background for consistency with the design reference
        view.setBackgroundColor(0xFF0C0C0F.toInt())

        // Setup ViewPager2
        val adapter = OnboardingAdapter(onboardingPages)
        binding.onboardingPager.adapter = adapter
        binding.onboardingPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                render(position)
            }
        })

        binding.onboardingNextButton.setOnClickListener {
            val current = binding.onboardingPager.currentItem
            if (current < onboardingPages.size - 1) {
                binding.onboardingPager.currentItem = current + 1
            } else {
                viewModel.setOnboardingCompleted()
                navigateToRoute(Routes.MAIN_CONTAINER)
            }
        }

        binding.onboardingSkipButton.setOnClickListener {
            viewModel.setOnboardingCompleted()
            navigateToRoute(Routes.MAIN_CONTAINER)
        }

        render(0)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun render(position: Int) {
        val page = onboardingPages[position]
        val accent = page.accentColor
        val d = resources.displayMetrics.density

        // Update Dots
        val dots = listOf(binding.onboardingDot0, binding.onboardingDot1, binding.onboardingDot2)
        val inactiveColor = 0xFF333333.toInt()
        for (i in dots.indices) {
            val dot = dots[i]
            val active = position == i
            val lp = dot.layoutParams
            lp.width = (8f * d).toInt()
            dot.layoutParams = lp
            dot.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (active) accent else inactiveColor)
            }
        }

        // Update Next button
        val buttonRadius = 12f * d
        val content = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE; cornerRadius = buttonRadius; setColor(accent)
        }
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE; cornerRadius = buttonRadius; setColor(Color.WHITE)
        }
        binding.onboardingNextButton.background = RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), content, mask)
        binding.onboardingNextText.setText(
            if (position == onboardingPages.size - 1) R.string.action_get_started else R.string.action_next
        )
    }

    private inner class OnboardingAdapter(private val pages: List<OnboardingPage>) :
        RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemOnboardingBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val page = pages[position]

            holder.binding.itemTitle.setText(page.titleRes)
            holder.binding.itemDesc.setText(page.descRes)
            holder.binding.itemImage.setImageResource(page.imageRes)

            // Background circle logic
            holder.binding.itemBgCircle.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(page.accentColor)
            }
            holder.binding.itemBgCircle.alpha = 0.12f
        }

        override fun getItemCount() = pages.size
    }

    data class OnboardingPage(val titleRes: Int, val descRes: Int, val imageRes: Int, val accentColor: Int)
}
