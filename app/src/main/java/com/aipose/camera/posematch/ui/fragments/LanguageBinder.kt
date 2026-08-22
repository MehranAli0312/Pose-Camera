package com.aipose.camera.posematch.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.data.LocaleHelper
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Reusable binding for the language-selection UI ([R.layout.fragment_language]). Used both by
 * [LanguageFragment] (first-run flow) and by the Settings language dialog, so there is a single XML
 * implementation. [onDone] fires after the choice is persisted (navigate, or dismiss + recreate).
 */
class LanguageBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View,
    private val onDone: () -> Unit
) {
    private val languages = listOf(
        "English", "Hindi", "Urdu", "Arabic", "Spanish",
        "Turkish", "Bangla", "French", "Portuguese", "Russian", "Filipino", "German"
    )

    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(viewModel.appTheme.value)
    private val accent = palette.accent
    private val card = palette.card
    private val gray = 0xFF888888.toInt()
    private val cornerPx = 14f * d
    private val strokePx = (1.5f * d).toInt().coerceAtLeast(1)
    private val selectedBg = (accent and 0x00FFFFFF) or (38 shl 24)

    private val rows = ArrayList<RowHolder>()

    private class RowHolder(val lang: String, val root: View, val name: TextView, val radio: RadioButton)

    fun bind() {
        root.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )

        val baseTop = root.paddingTop
        val baseBottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = baseTop + bars.top, bottom = baseBottom + bars.bottom)
            insets
        }

        val done = root.findViewById<View>(R.id.language_confirm_button)
        done.background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(accent) }
        done.setOnClickListener {
            LocaleHelper.persistLanguage(host.requireContext(), viewModel.selectedLanguage.value)
            onDone()
        }

        val list = root.findViewById<LinearLayout>(R.id.language_list)
        list.removeAllViews(); rows.clear()
        val inflater = LayoutInflater.from(host.requireContext())
        val radioTint = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(accent, gray)
        )
        languages.forEachIndexed { i, lang ->
            val row = inflater.inflate(R.layout.item_language, list, false)
            val name = row.findViewById<TextView>(R.id.lang_name)
            val radio = row.findViewById<RadioButton>(R.id.lang_radio)
            name.text = lang
            radio.buttonTintList = radioTint
            row.setOnClickListener { viewModel.setLanguage(lang) }
            val lp = row.layoutParams as LinearLayout.LayoutParams
            if (i < languages.lastIndex) lp.bottomMargin = (10f * d).toInt()
            row.layoutParams = lp
            list.addView(row)
            rows.add(RowHolder(lang, row, name, radio))
        }
        list.addView(View(host.requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (12f * d).toInt())
        })

        host.viewLifecycleOwner.lifecycleScope.launch {
            host.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedLanguage.collect { selected -> applySelection(selected) }
            }
        }
    }

    private fun applySelection(selected: String) {
        for (r in rows) {
            val sel = r.lang == selected
            r.root.background = rowBackground(sel)
            r.name.setTypeface(null, if (sel) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            r.radio.isChecked = sel
        }
    }

    private fun rowBackground(selected: Boolean): RippleDrawable {
        val content = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerPx
            setColor(if (selected) selectedBg else card)
            setStroke(strokePx, if (selected) accent else Color.TRANSPARENT)
        }
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerPx
            setColor(Color.WHITE)
        }
        return RippleDrawable(ColorStateList.valueOf(0x22FFFFFF), content, mask)
    }
}
