package com.aipose.camera.posematch.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.content.res.ColorStateList
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.databinding.FragmentSettingsBinding
import com.aipose.camera.posematch.ui.theme.ThemePrefs
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.util.applySystemBarInsets
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity

class SettingsBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View,
    private val requireActivity: FragmentActivity
) {
    private val themes = listOf("Dark", "Light", "Sleek Charcoal", "Cyberpunk Violet")
    private val jobs = mutableListOf<Job>()
    private var themeDialog: Dialog? = null
    private var languageDialog: Dialog? = null

    private val binding = FragmentSettingsBinding.bind(root)
    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(root.context)
    private val accent = palette.accent
    private val card = palette.card
    private val glass = palette.glass

    fun bind() {
        // Status-bar inset on top of the 16dp content padding (edge-to-edge safe on all versions).
        root.applySystemBarInsets(top = true)

        listOf(binding.settingsLanguage, binding.settingsTheme, binding.settingsShare, binding.settingsPrivacy)
            .forEach { it.background = cardBackground() }

        val langValue = binding.settingsLanguageValue
        langValue.setTextColor(accent)

        jobs += host.viewLifecycleOwner.lifecycleScope.launch {
            host.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedLanguage.collect { langValue.text = it }
            }
        }

        // Theme: a single Dark/Light switch (on = dark). Toggling persists the choice and recreates
        // the activity so every screen re-reads the day/night colour resources.
        setupThemeSwitch()

        binding.settingsLanguage.setOnClickListener { showLanguageDialog() }
        binding.settingsShare.setOnClickListener { shareApp(requireActivity) }
        binding.settingsPrivacy.setOnClickListener { openPrivacy() }
    }

    fun unbind() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        themeDialog?.dismiss(); themeDialog = null
        languageDialog?.dismiss(); languageDialog = null
    }

    private fun cardBackground() = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 12f * d
        setColor(card)
    }

    private fun setupThemeSwitch() {
        val sw = binding.settingsThemeSwitch
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val off = palette.textSecondary
        sw.thumbTintList = ColorStateList(states, intArrayOf(accent, off))
        sw.trackTintList = ColorStateList(
            states,
            intArrayOf((accent and 0x00FFFFFF) or (0x80 shl 24), (off and 0x00FFFFFF) or (0x40 shl 24))
        )
        sw.isChecked = ThemePrefs.isDark(root.context)
        sw.setOnCheckedChangeListener { _, dark ->
            if (dark == ThemePrefs.isDark(root.context)) return@setOnCheckedChangeListener
            ThemePrefs.setDark(root.context, dark)
            viewModel.setTheme(if (dark) "Dark" else "Light")
            requireActivity.recreate()
        }
        binding.settingsTheme.setOnClickListener { sw.toggle() }
    }

    fun shareApp(context: Context) {
        val packageName = context.packageName
        val appLink = "https://play.google.com/store/apps/details?id=$packageName"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Download Pose Match Camera: achieve perfect posture alignment live on-device!:\n$appLink"
            )
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share app")
        )
    }

    private fun openPrivacy() {
        runCatching {
            host.startActivity(
                Intent(Intent.ACTION_VIEW,
                    "https://sites.google.com/view/posematchcamera/home".toUri())
            )
        }
    }

    // Language: reuse the XML language screen inside a full-screen dialog covering the bottom nav.
    private fun showLanguageDialog() {
        val ctx = host.requireContext()
        val v = host.layoutInflater.inflate(R.layout.fragment_language, null)
        val dialog = Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(v)
            setCancelable(false)
        }
        languageDialog = dialog
        LanguageBinder(host, viewModel, v) {
            dialog.dismiss()
            (host.activity as? Activity)?.recreate()
        }.bind()
        dialog.show()
    }

    // Theme picker — choose then Apply/Cancel (no instant commit).
    private fun showThemeDialog() {
        val ctx = host.requireContext()
        var pending = viewModel.appTheme.value
        val content = host.layoutInflater.inflate(R.layout.dialog_theme, null) as LinearLayout
        content.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f * d
            setColor(card)
        }

        val list = content.findViewById<LinearLayout>(R.id.theme_list)
        val rowViews = HashMap<String, View>()
        val accent20 = (accent and 0x00FFFFFF) or (51 shl 24)

        fun restyle() {
            for (t in themes) {
                val row = rowViews[t] ?: continue
                val sel = t == pending
                row.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 8f * d
                    setColor(if (sel) accent20 else Color.TRANSPARENT)
                }
                row.findViewById<ImageView>(R.id.theme_check).apply {
                    visibility = if (sel) View.VISIBLE else View.INVISIBLE
                    imageTintList = ColorStateList.valueOf(accent)
                }
            }
        }

        themes.forEachIndexed { i, t ->
            val row = host.layoutInflater.inflate(R.layout.item_theme, list, false)
            row.findViewById<TextView>(R.id.theme_name).text = t
            row.setOnClickListener { pending = t; restyle() }
            val lp = row.layoutParams as LinearLayout.LayoutParams
            if (i < themes.lastIndex) lp.bottomMargin = (8f * d).toInt()
            row.layoutParams = lp
            list.addView(row)
            rowViews[t] = row
        }
        restyle()

        val cancel = content.findViewById<TextView>(R.id.theme_cancel)
        val apply = content.findViewById<TextView>(R.id.theme_apply)
        cancel.background = outlinedBg(8f)
        apply.background = solidBg(accent, 8f)

        val dialog = Dialog(ctx).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(content)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.75f)
        }
        themeDialog = dialog
        cancel.setOnClickListener { dialog.dismiss() }
        apply.setOnClickListener {
            viewModel.setTheme(pending)
            dialog.dismiss()
            Toast.makeText(ctx, ctx.getString(R.string.toast_theme_changed, pending), Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    private fun outlinedBg(radiusDp: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusDp * d
        setColor(Color.TRANSPARENT)
        setStroke((1f * d).toInt().coerceAtLeast(1), glass)
    }

    private fun solidBg(color: Int, radiusDp: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusDp * d
        setColor(color)
    }
}
