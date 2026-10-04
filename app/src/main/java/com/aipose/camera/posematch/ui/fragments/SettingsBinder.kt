package com.aipose.camera.posematch.ui.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.aipose.camera.posematch.BuildConfig
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.admob_ads.skipNextAppOpen
import com.aipose.camera.posematch.databinding.FragmentSettingsBinding
import com.aipose.camera.posematch.ui.theme.ThemePrefs
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.util.applySystemBarInsets
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import androidx.core.graphics.drawable.toDrawable
import com.aipose.camera.posematch.analytics.Analytics

class SettingsBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View,
    private val requireActivity: FragmentActivity,
    private val onExitRequest: () -> Unit
) {
    private val jobs = mutableListOf<Job>()

    private val binding = FragmentSettingsBinding.bind(root)
    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(root.context)
    private val accent = palette.accent
    private val card = palette.card
    private val textPrimary = palette.textPrimary

    fun bind() {
        Analytics.screen(Analytics.Screen.SETTINGS)
        root.applySystemBarInsets(top = true)

        binding.settingsBack.setOnClickListener {
            requireActivity.onBackPressedDispatcher.onBackPressed()
        }

        setupThemeButtons()
        setupVersion()
        setupClickListeners()

        // Apply card backgrounds
        binding.cardTheme.background = cardBackground()
        binding.cardMore.background = cardBackground()
    }

    fun unbind() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private fun cardBackground() = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 12f * d
        setColor(card)
    }

    private fun setupThemeButtons() {
        val isDark = ThemePrefs.isDark(root.context)
        updateThemeButtonStates(isDark)

        binding.btnThemeLight.setOnClickListener {
            if (ThemePrefs.isDark(root.context)) {
                applyTheme(false)
            }
        }
        binding.btnThemeDark.setOnClickListener {
            if (!ThemePrefs.isDark(root.context)) {
                applyTheme(true)
            }
        }
    }

    private fun applyTheme(dark: Boolean) {
        ThemePrefs.setDark(root.context, dark)
        viewModel.setTheme(if (dark) "Dark" else "Light")
        requireActivity.recreate()
    }

    private fun updateThemeButtonStates(isDark: Boolean) {
        val radius = 8f * d
        
        // Light button
        binding.btnThemeLight.background = GradientDrawable().apply {
            cornerRadius = radius
            if (!isDark) {
                setColor(accent)
            } else {
                setColor(0xFF2A2A2E.toInt())
            }
        }
        binding.btnThemeLight.setTextColor(if (!isDark) Color.WHITE else textPrimary)

        // Dark button
        binding.btnThemeDark.background = GradientDrawable().apply {
            cornerRadius = radius
            if (isDark) {
                setColor(accent)
            } else {
                setColor(0xFFE8E9ED.toInt())
            }
        }
        binding.btnThemeDark.setTextColor(if (isDark) Color.WHITE else textPrimary)
    }

    private fun setupVersion() {
        binding.tvVersionValue.text = BuildConfig.VERSION_NAME
    }

    private fun setupClickListeners() {
        binding.itemRate.setOnClickListener { Analytics.click("rate_us", Analytics.Screen.SETTINGS); showRateUsDialog() }
        binding.itemPrivacy.setOnClickListener { Analytics.click("privacy_policy", Analytics.Screen.SETTINGS); openPrivacy() }
        binding.itemShare.setOnClickListener { Analytics.click("share_app", Analytics.Screen.SETTINGS); shareApp(requireActivity) }
        binding.itemExit.setOnClickListener { Analytics.click("exit_app", Analytics.Screen.SETTINGS); onExitRequest() }
    }

    private fun showRateUsDialog() {
        val dialog = Dialog(requireActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_rate_us)
        
        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(android.view.Gravity.CENTER)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
        }
        
        val stars = listOf(
            dialog.findViewById(R.id.star1),
            dialog.findViewById(R.id.star2),
            dialog.findViewById(R.id.star3),
            dialog.findViewById(R.id.star4),
            dialog.findViewById<ImageView>(R.id.star5)
        )
        val submitBtn = dialog.findViewById<TextView>(R.id.btn_rate_submit)
        val closeBtn = dialog.findViewById<ImageView>(R.id.rate_close)
        val illustration = dialog.findViewById<ImageView>(R.id.rate_illustration)
        val starsContainer = dialog.findViewById<View>(R.id.stars_container)
        
        // Use mipmap launcher icon
        illustration.setImageResource(R.drawable.il_rate_us)
        
        // Ensure visibility by setting tint explicitly
        closeBtn.setColorFilter(palette.textSecondary)
        
        var selectedRating = 0

        // Reset to border state initially
        stars.forEach { star ->
            star.setImageResource(R.drawable.ic_star_border)
            star.setColorFilter(palette.iconMuted)
        }

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStars(stars, selectedRating)
                
                star.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150)
                    .withEndAction {
                        star.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                    }.start()
            }
        }

        closeBtn.setOnClickListener { dialog.dismiss() }

        submitBtn.setOnClickListener {
            if (selectedRating == 0) {
                // Add a small shake animation to indicate rating is needed
                starsContainer.animate().translationX(15f).setDuration(50).withEndAction {
                    starsContainer.animate().translationX(-15f).setDuration(50).withEndAction {
                        starsContainer.animate().translationX(0f).setDuration(50).start()
                    }.start()
                }.start()
                return@setOnClickListener
            }
            
            dialog.dismiss()
            if (selectedRating <= 3) {
                sendFeedbackEmail()
            } else {
                openPlayStore()
            }
        }

        dialog.show()
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        val gold = 0xFFFFD700.toInt() // Premium Gold
        val muted = palette.iconMuted
        
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(R.drawable.ic_star)
                star.setColorFilter(gold)
            } else {
                star.setImageResource(R.drawable.ic_star_border)
                star.setColorFilter(muted)
            }
        }
    }

    private fun sendFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("slife0667@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "App Feedback: Pose Match Camera")
        }
        runCatching {
            skipNextAppOpen = true   // user is leaving the app -> don't show App Open on return
            requireActivity.startActivity(intent)
        }
    }

    private fun openPlayStore() {
        val packageName = requireActivity.packageName
        val uri = "market://details?id=$packageName".toUri()
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        runCatching {
            skipNextAppOpen = true   // leaving the app -> don't show App Open on return
            requireActivity.startActivity(goToMarket)
        }.onFailure {
            skipNextAppOpen = true
            requireActivity.startActivity(Intent(Intent.ACTION_VIEW,
                "http://play.google.com/store/apps/details?id=$packageName".toUri()))
        }
    }

    private fun shareApp(context: Context) {
        val appLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Download Pose Match Camera: achieve perfect posture alignment live on-device!:\n$appLink")
        }
        skipNextAppOpen = true   // share sheet leaves the app -> don't show App Open on return
        context.startActivity(Intent.createChooser(shareIntent, "Share app"))
    }

    private fun openPrivacy() {
        runCatching {
            skipNextAppOpen = true   // browser leaves the app -> don't show App Open on return
            host.startActivity(Intent(Intent.ACTION_VIEW, "https://sites.google.com/view/posematchcamera/home".toUri()))
        }
    }
}
