package com.aipose.camera.posematch.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.res.ColorStateList
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Binds the real-XML Settings tab ([R.layout.fragment_settings]) to the shared [MainViewModel],
 * reproducing the old Compose SettingsScreen: four cards (Language / Theme / Share / Privacy), a
 * full-screen Language dialog and an Apply/Cancel Theme picker. Colours come from the theme palette.
 */
class SettingsBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View
) {
    private val themes = listOf("Dark", "Light", "Sleek Charcoal", "Cyberpunk Violet")
    private val jobs = mutableListOf<Job>()
    private var themeDialog: Dialog? = null
    private var languageDialog: Dialog? = null

    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(viewModel.appTheme.value)
    private val accent = palette.accent
    private val card = palette.card
    private val glass = palette.glass

    fun bind() {
        // statusBarsPadding on top of the 16dp content padding.
        val baseTop = root.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updatePadding(top = baseTop + top)
            insets
        }

        val cardIds = intArrayOf(R.id.settings_language, R.id.settings_theme, R.id.settings_share, R.id.settings_privacy)
        for (id in cardIds) root.findViewById<View>(id).background = cardBackground()

        val langValue = root.findViewById<TextView>(R.id.settings_language_value)
        val themeValue = root.findViewById<TextView>(R.id.settings_theme_value)
        langValue.setTextColor(accent)
        themeValue.setTextColor(accent)

        jobs += host.viewLifecycleOwner.lifecycleScope.launch {
            host.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedLanguage.collect { langValue.text = it }
            }
        }
        jobs += host.viewLifecycleOwner.lifecycleScope.launch {
            host.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appTheme.collect { themeValue.text = it }
            }
        }

        root.findViewById<View>(R.id.settings_language).setOnClickListener { showLanguageDialog() }
        root.findViewById<View>(R.id.settings_theme).setOnClickListener { showThemeDialog() }
        root.findViewById<View>(R.id.settings_share).setOnClickListener { shareApp() }
        root.findViewById<View>(R.id.settings_privacy).setOnClickListener { openPrivacy() }
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

    private fun shareApp() {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Download Pose Match Camera: achieve perfect posture alignment live on-device!"
            )
        }
        host.startActivity(Intent.createChooser(intent, "Share App"))
    }

    private fun openPrivacy() {
        runCatching {
            host.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/posematchcamera/home"))
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
