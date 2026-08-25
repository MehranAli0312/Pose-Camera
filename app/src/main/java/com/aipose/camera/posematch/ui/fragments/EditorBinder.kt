package com.aipose.camera.posematch.ui.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import coil.imageLoader
import coil.request.ImageRequest
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.databinding.FragmentEditorBinding
import com.aipose.camera.posematch.databinding.ItemAdjustToolBinding
import com.aipose.camera.posematch.databinding.ItemFilterChipBinding
import com.aipose.camera.posematch.domain.PhotoFilters
import com.aipose.camera.posematch.ui.screens.AdjustTool
import com.aipose.camera.posematch.ui.screens.EditResult
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.util.SELECTION_BLUE
import com.aipose.camera.posematch.ui.util.applySystemBarInsets
import com.aipose.camera.posematch.ui.widget.CenterSeekBar
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import java.io.File

/**
 * Drives the real-XML post-capture editor ([R.layout.fragment_editor]) — a faithful port of
 * PhotoEditScreen. Same self-contained state (selected filter, edit tab, active tool, adjustment
 * sliders, rotation, crop aspect); the live preview uses a [ColorMatrixColorFilter] and the Save
 * result hands back the composed matrix + geometry via [EditResult].
 */
class EditorBinder(
    private val ctx: Context,
    private val viewModel: MainViewModel,
    private val root: View,
    private val photoPath: String,
    private val autoFilterMatrix: FloatArray?,
    private val onDiscard: () -> Unit,
    private val onDone: (EditResult) -> Unit
) {
    private val binding = FragmentEditorBinding.bind(root)
    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(root.context)
    private val accent = palette.accent
    private val card = palette.card
    private val gray = palette.textSecondary
    private val inflater = LayoutInflater.from(ctx)

    private var selectedFilterId = PhotoFilters.ID_AUTO
    private var editMode = 0 // 0 = Filters, 1 = Adjust
    private var activeTool = AdjustTool.Exposure
    private var rotationDeg = 0
    private var cropAspect: Float? = null
    private val adjustments = HashMap<AdjustTool, Float>()

    private val preview get() = binding.editorPreview
    private val previewBox get() = binding.editorPreviewBox

    fun bind() {
        // Full-screen overlay: inset the whole editor from the status + navigation bars.
        root.applySystemBarInsets(top = true, bottom = true)

        // Static styling.
        binding.editorSave.background = rounded(accent, 10f)
        binding.editorTabs.background = rounded(card, 10f)
        binding.editorRevert.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(card)
        }
        preview.clipToOutline = true
        preview.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(v: View, outline: Outline) {
                outline.setRoundRect(0, 0, v.width, v.height, 10f * d)
            }
        }

        // Load the captured frame.
        ctx.imageLoader.enqueue(
            ImageRequest.Builder(ctx).data(File(photoPath)).crossfade(true).target(preview).build()
        )

        binding.editorDiscard.setOnClickListener { onDiscard() }
        binding.editorSave.setOnClickListener {
            onDone(EditResult(finalMatrix(), rotationDeg, cropAspect))
        }
        binding.editorRevert.setOnClickListener {
            selectedFilterId = PhotoFilters.ID_ORIGINAL
            adjustments.clear(); rotationDeg = 0; cropAspect = null
            restyleChips(); if (editMode == 1) { restyleRail(); buildToolControl() }
            updatePreview()
        }
        binding.editorTabFilters.setOnClickListener { setMode(0) }
        binding.editorTabAdjust.setOnClickListener { setMode(1) }

        buildFilterStrip()
        setMode(0)
        previewBox.post { updatePreview() }
    }

    // ---- Compute -------------------------------------------------------------------------

    private fun adj(tool: AdjustTool) = adjustments[tool] ?: 0f

    private fun finalMatrix(): FloatArray? {
        val filter = PhotoFilters.matrixFor(selectedFilterId, autoFilterMatrix)
        val adjust = PhotoFilters.adjustmentMatrix(
            exposure = adj(AdjustTool.Exposure),
            brightness = adj(AdjustTool.Brightness),
            contrast = adj(AdjustTool.Contrast),
            saturation = adj(AdjustTool.Saturation),
            warmth = adj(AdjustTool.Warmth),
            tint = adj(AdjustTool.Tint),
            hue = adj(AdjustTool.Hue),
            fade = adj(AdjustTool.Fade)
        )
        return PhotoFilters.compose(filter, adjust)
    }

    private fun updatePreview() {
        val m = finalMatrix()
        preview.colorFilter = m?.let { ColorMatrixColorFilter(ColorMatrix(it.copyOf())) }
        preview.rotation = rotationDeg.toFloat()
        applyCropSizing()
    }

    private fun applyCropSizing() {
        val box = previewBox
        val pad = box.paddingLeft
        val contentW = box.width - box.paddingLeft - box.paddingRight
        val contentH = box.height - box.paddingTop - box.paddingBottom
        val lp = preview.layoutParams as FrameLayout.LayoutParams
        val aspect = cropAspect
        if (aspect == null || contentW <= 0 || contentH <= 0) {
            lp.width = FrameLayout.LayoutParams.MATCH_PARENT
            lp.height = FrameLayout.LayoutParams.MATCH_PARENT
            preview.scaleType = ImageView.ScaleType.FIT_CENTER
        } else {
            var w = contentW
            var h = (w / aspect).toInt()
            if (h > contentH) { h = contentH; w = (h * aspect).toInt() }
            lp.width = w; lp.height = h
            preview.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        lp.gravity = Gravity.CENTER
        preview.layoutParams = lp
    }

    // ---- Tabs ----------------------------------------------------------------------------

    private fun setMode(mode: Int) {
        editMode = mode
        binding.editorFilterScroll.visibility = if (mode == 0) View.VISIBLE else View.GONE
        binding.editorAdjustPanel.visibility = if (mode == 1) View.VISIBLE else View.GONE
        styleTab(binding.editorTabFilters, mode == 0)
        styleTab(binding.editorTabAdjust, mode == 1)
        if (mode == 1) { buildToolRail(); restyleRail(); buildToolControl() }
    }

    private fun styleTab(tab: TextView, selected: Boolean) {
        tab.apply {
            background = if (selected) rounded(accent, 10f) else null
            setTextColor(if (selected) Color.WHITE else gray)
            setTypeface(null, if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }
    }

    // ---- Filter strip --------------------------------------------------------------------

    private val chipViews = ArrayList<Pair<String, ItemFilterChipBinding>>()

    private fun buildFilterStrip() {
        val strip = binding.editorFilterStrip
        strip.removeAllViews(); chipViews.clear()
        val gap = (12f * d).toInt()
        PhotoFilters.strip.forEachIndexed { i, filter ->
            val chip = ItemFilterChipBinding.inflate(inflater, strip, false)
            (chip.root.layoutParams as LinearLayout.LayoutParams).marginStart = if (i > 0) gap else 0
            val img = chip.chipImage
            img.clipToOutline = true
            img.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(v: View, outline: Outline) { outline.setRoundRect(0, 0, v.width, v.height, 10f * d) }
            }
            ctx.imageLoader.enqueue(ImageRequest.Builder(ctx).data(File(photoPath)).size(120).target(img).build())
            val chipMatrix = PhotoFilters.matrixFor(filter.id, autoFilterMatrix)
            img.colorFilter = chipMatrix?.let { ColorMatrixColorFilter(ColorMatrix(it.copyOf())) }
            if (filter.id == PhotoFilters.ID_AUTO) {
                chip.chipAutoDot.apply {
                    visibility = View.VISIBLE
                    background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(0xFF10B981.toInt()) }
                }
            }
            chip.chipLabel.text = filter.label
            chip.root.setOnClickListener { selectedFilterId = filter.id; restyleChips(); updatePreview() }
            strip.addView(chip.root)
            chipViews.add(filter.id to chip)
        }
        restyleChips()
    }

    private fun restyleChips() {
        for ((id, chip) in chipViews) {
            val selected = id == selectedFilterId
            // 1dp blue selection stroke drawn OVER the preview image (foreground), glass otherwise.
            chip.chipBox.foreground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10f * d
                setColor(Color.TRANSPARENT)
                setStroke((1f * d).toInt().coerceAtLeast(1), if (selected) SELECTION_BLUE else palette.glass)
            }
            chip.chipLabel.apply {
                setTextColor(if (selected) SELECTION_BLUE else gray)
                setTypeface(null, if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            }
        }
    }

    // ---- Adjust rail + control -----------------------------------------------------------

    private val toolViews = ArrayList<Pair<AdjustTool, ItemAdjustToolBinding>>()

    private fun buildToolRail() {
        val rail = binding.editorToolRail
        rail.removeAllViews(); toolViews.clear()
        val gap = (4f * d).toInt()
        AdjustTool.entries.forEachIndexed { i, tool ->
            val item = ItemAdjustToolBinding.inflate(inflater, rail, false)
            (item.root.layoutParams as LinearLayout.LayoutParams).marginStart = if (i > 0) gap else 0
            item.toolIcon.setImageResource(toolIcon(tool))
            item.toolLabel.text = tool.label
            item.root.setOnClickListener { activeTool = tool; restyleRail(); buildToolControl() }
            rail.addView(item.root)
            toolViews.add(tool to item)
        }
    }

    private fun restyleRail() {
        for ((tool, item) in toolViews) {
            val active = tool == activeTool
            val touched = when (tool) {
                AdjustTool.Rotate -> rotationDeg != 0
                AdjustTool.Crop -> cropAspect != null
                else -> adj(tool) != 0f
            }
            item.toolRoot.background = if (active) GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10f * d
                setColor((accent and 0x00FFFFFF) or (38 shl 24))
            } else null
            item.toolIcon.imageTintList = tint(if (active) accent else if (touched) Color.WHITE else gray)
            item.toolDot.apply {
                visibility = if (touched) View.VISIBLE else View.GONE
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(accent) }
            }
            item.toolLabel.apply {
                setTextColor(if (active) accent else gray)
                setTypeface(null, if (active) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun buildToolControl() {
        val holder = binding.editorToolControl
        holder.removeAllViews()
        when (activeTool) {
            AdjustTool.Rotate -> {
                val row = horizontalRow()
                row.addView(rotateButton(R.drawable.ic_rotate_left, "90° Left", 270), weightLp())
                row.addView(rotateButton(R.drawable.ic_rotate_right, "90° Right", 90), weightLp((6f * d).toInt()))
                holder.addView(row)
            }
            AdjustTool.Crop -> {
                val scroll = android.widget.HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false }
                val row = horizontalRow()
                row.setPadding((16f * d).toInt(), 0, (16f * d).toInt(), 0)
                listOf("Free" to null, "1:1" to 1f, "4:5" to 0.8f, "16:9" to (16f / 9f), "9:16" to (9f / 16f))
                    .forEachIndexed { i, (label, ratio) ->
                        val sel = cropAspect == ratio
                        val chip = TextView(ctx).apply {
                            text = label
                            textSize = 12f
                            setTextColor(if (sel) Color.WHITE else gray)
                            setPadding((14f * d).toInt(), (9f * d).toInt(), (14f * d).toInt(), (9f * d).toInt())
                            background = rounded(if (sel) accent else card, 10f)
                            setOnClickListener { cropAspect = ratio; restyleRail(); buildToolControl(); updatePreview() }
                        }
                        val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        if (i > 0) lp.marginStart = (8f * d).toInt()
                        row.addView(chip, lp)
                    }
                scroll.addView(row)
                holder.addView(scroll)
            }
            else -> {
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding((16f * d).toInt(), (4f * d).toInt(), (16f * d).toInt(), 0)
                }
                val valueText = TextView(ctx).apply {
                    setTextColor(Color.WHITE); textSize = 12f
                    gravity = Gravity.END
                    text = (adj(activeTool) * 100).toInt().toString()
                }
                // Center-origin slider: middle = 0, right = +, left = − (fills from the centre).
                val slider = CenterSeekBar(ctx).apply {
                    accentColor = accent
                    value = adj(activeTool)
                    onValueChanged = { v ->
                        adjustments[activeTool] = v
                        valueText.text = (v * 100).toInt().toString()
                        restyleRail(); updatePreview()
                    }
                }
                row.addView(slider, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                row.addView(valueText, LinearLayout.LayoutParams((36f * d).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
                    .apply { marginStart = (10f * d).toInt() })
                holder.addView(row)
            }
        }
    }

    private fun rotateButton(iconRes: Int, label: String, delta: Int): View {
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(card, 10f)
            setPadding(0, (11f * d).toInt(), 0, (11f * d).toInt())
            setOnClickListener { rotationDeg = (rotationDeg + delta) % 360; restyleRail(); updatePreview() }
        }
        row.addView(ImageView(ctx).apply {
            setImageResource(iconRes); imageTintList = tint(Color.WHITE)
        }, LinearLayout.LayoutParams((18f * d).toInt(), (18f * d).toInt()))
        row.addView(TextView(ctx).apply {
            text = label; setTextColor(Color.WHITE); textSize = 12f
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            .apply { marginStart = (6f * d).toInt() })
        return row
    }

    private fun horizontalRow() = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding((16f * d).toInt(), 0, (16f * d).toInt(), 0)
    }

    private fun weightLp(startMargin: Int = 0) =
        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = startMargin }

    private fun toolIcon(tool: AdjustTool) = when (tool) {
        AdjustTool.Exposure -> R.drawable.ic_exposure
        AdjustTool.Brightness -> R.drawable.ic_wb_sunny
        AdjustTool.Contrast -> R.drawable.ic_contrast
        AdjustTool.Saturation -> R.drawable.ic_water_drop
        AdjustTool.Warmth -> R.drawable.ic_thermostat
        AdjustTool.Tint -> R.drawable.ic_colorize
        AdjustTool.Hue -> R.drawable.ic_palette
        AdjustTool.Fade -> R.drawable.ic_gradient
        AdjustTool.Rotate -> R.drawable.ic_rotate_right
        AdjustTool.Crop -> R.drawable.ic_crop
    }

    private fun tint(color: Int) = android.content.res.ColorStateList.valueOf(color)

    private fun rounded(color: Int, radiusDp: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusDp * d
        setColor(color)
    }
}
