package com.aipose.camera.posematch.ui.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.data.PoseItem
import com.aipose.camera.posematch.ui.screens.assetPathOf
import com.aipose.camera.posematch.ui.screens.decodeAsset
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val poseBitmapCache = LruCache<String, Bitmap>(80)

private val CategoryOrder = listOf(
    "Viral", "Couple", "Sunset", "Dark", "Mirror",
    "Beach", "Cafe", "Family", "Nature", "Waterfall"
)

private fun categoryIconRes(cat: String): Int = when (cat) {
    "Viral" -> R.drawable.ic_whatshot
    "Couple" -> R.drawable.ic_favorite
    "Sunset" -> R.drawable.ic_wb_sunny
    "Dark" -> R.drawable.ic_dark_mode
    "Mirror" -> R.drawable.ic_flip
    "Beach" -> R.drawable.ic_beach
    "Cafe" -> R.drawable.ic_coffee
    "Family" -> R.drawable.ic_groups
    "Nature" -> R.drawable.ic_park
    "Waterfall" -> R.drawable.ic_waves
    else -> R.drawable.ic_collections
}

/**
 * Binds the real-XML Home tab ([R.layout.fragment_home]) to the shared [MainViewModel], reproducing
 * HomeScreen: brand + gallery top bar, a search box, and either a flat search-results grid or the
 * browse view (hero card + guidance caption + per-trend rows of 3 with a "Show all" album overlay).
 * Bundled-asset images are decoded straight from AssetManager (via [decodeAsset]) as in the original.
 */
class HomeBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View,
    private val onOpenCamera: () -> Unit,
    private val onLaunchGalleryPicker: () -> Unit
) {
    private val jobs = mutableListOf<Job>()
    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(viewModel.appTheme.value)
    private val accent = palette.accent
    private val card = palette.card
    private val glass = palette.glass
    private val gray = 0xFF888888.toInt()

    private var showAllCategory: String? = null

    private val dynamic get() = root.findViewById<LinearLayout>(R.id.home_dynamic)
    private val albumOverlay get() = root.findViewById<FrameLayout>(R.id.home_album_overlay)

    fun bind() {
        val topbar = root.findViewById<View>(R.id.home_topbar)
        val baseTop = topbar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(topbar) { v, insets ->
            v.updatePadding(top = baseTop + insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            insets
        }

        root.findViewById<View>(R.id.home_search_bar).background = rounded(card, 14f)
        root.findViewById<View>(R.id.home_gallery_icon).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14f * d
                setColor(card)
                setStroke((1f * d).toInt().coerceAtLeast(1), glass)
            }
            setOnClickListener { dismissKeyboard(); onLaunchGalleryPicker() }
        }

        val input = root.findViewById<EditText>(R.id.home_search_input)
        val clear = root.findViewById<ImageView>(R.id.home_search_clear)
        input.setText(viewModel.searchQuery.value)
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                if (text != viewModel.searchQuery.value) viewModel.setSearchQuery(text)
            }
        })
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { dismissKeyboard(); true } else false
        }
        clear.setOnClickListener {
            input.setText("")
            viewModel.setSearchQuery("")
            dismissKeyboard()
        }

        for (flow in listOf(viewModel.searchQuery, viewModel.defaultPoses)) {
            jobs += host.viewLifecycleOwner.lifecycleScope.launch {
                host.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    flow.collect { render() }
                }
            }
        }
    }

    fun unbind() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        viewModel.setSearchQuery("")
    }

    /** @return true if a back press was consumed (the album overlay was open). */
    fun onBack(): Boolean = if (showAllCategory != null) { closeAlbum(); true } else false

    // ---- Rendering -----------------------------------------------------------------------

    private fun render() {
        val query = viewModel.searchQuery.value
        val allPoses = viewModel.defaultPoses.value
        root.findViewById<View>(R.id.home_search_clear).visibility =
            if (query.isNotEmpty()) View.VISIBLE else View.GONE

        val container = dynamic
        container.removeAllViews()

        if (query.isNotBlank()) {
            val results = allPoses.filter { pose ->
                pose.title.contains(query, true) ||
                    pose.description.contains(query, true) ||
                    pose.category.contains(query, true) ||
                    pose.tags.any { it.contains(query, true) }
            }
            if (results.isEmpty()) {
                val box = FrameLayout(host.requireContext()).apply {
                    background = rounded(card, 16f)
                    val tv = TextView(context).apply {
                        text = host.getString(R.string.home_no_blueprints)
                        setTextColor(gray); textSize = 14f
                    }
                    addView(tv, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply { gravity = android.view.Gravity.CENTER })
                }
                addSpaced(container, box, (140f * d).toInt())
            } else {
                results.chunked(3).forEach { addSpaced(container, buildRow(it)) }
            }
        } else {
            buildBrowse(container, allPoses)
        }

        showAllCategory?.let { cat ->
            rebuildAlbumGrid(viewModel.defaultPoses.value.filter { it.category == cat })
        }
    }

    private fun buildBrowse(container: LinearLayout, allPoses: List<PoseItem>) {
        // Hero card.
        val heroPose = allPoses.firstOrNull { it.category == "Couple" } ?: allPoses.firstOrNull()
        val hero = LayoutInflater.from(host.requireContext()).inflate(R.layout.view_home_hero, container, false)
        hero.clipToOutline = true
        hero.outlineProvider = roundOutline(8f)
        hero.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(accent, (accent and 0x00FFFFFF) or (184 shl 24))
        )
        val heroImage = hero.findViewById<ImageView>(R.id.hero_image)
        val heroScrim = hero.findViewById<View>(R.id.hero_scrim)
        val heroAsset = heroPose?.image?.let(::assetPathOf)
        if (heroAsset != null) {
            heroImage.visibility = View.VISIBLE
            heroScrim.visibility = View.VISIBLE
            heroScrim.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(accent, (accent and 0x00FFFFFF) or (217 shl 24), Color.TRANSPARENT)
            )
            loadAsset(heroImage, heroAsset)
        } else {
            heroImage.visibility = View.GONE
            heroScrim.visibility = View.GONE
        }
        hero.findViewById<ImageView>(R.id.hero_cta_icon).imageTintList = tint(accent)
        hero.findViewById<TextView>(R.id.hero_cta_text).setTextColor(accent)
        hero.findViewById<ImageView>(R.id.hero_cta_arrow).imageTintList = tint(accent)
        hero.findViewById<View>(R.id.hero_cta).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f * d
                setColor(Color.WHITE)
            }
            setOnClickListener {
                dismissKeyboard()
                heroPose?.let { viewModel.selectPose(it) }
                onOpenCamera()
            }
        }
        addSpaced(container, hero)

        // Guidance caption.
        val hint = TextView(host.requireContext()).apply {
            text = host.getString(R.string.home_tap_hint)
            setTextColor(gray); textSize = 12f
        }
        addSpaced(container, hint)

        // Per-trend sections in a stable order (present categories first).
        val present = allPoses.map { it.category }.toSet()
        val ordered = CategoryOrder.filter { it in present } + present.filter { it !in CategoryOrder }
        val grouped = allPoses.groupBy { it.category }
        for (category in ordered) {
            val poses = grouped[category].orEmpty()
            if (poses.isEmpty()) continue
            val header = LayoutInflater.from(host.requireContext())
                .inflate(R.layout.item_category_header, container, false)
            header.findViewById<ImageView>(R.id.cat_header_icon).apply {
                setImageResource(categoryIconRes(category)); imageTintList = tint(accent)
            }
            header.findViewById<TextView>(R.id.cat_header_name).text = category
            val showAll = header.findViewById<LinearLayout>(R.id.cat_header_show_all)
            if (poses.size > 3) {
                showAll.visibility = View.VISIBLE
                header.findViewById<TextView>(R.id.cat_header_show_all_text).setTextColor(accent)
                header.findViewById<ImageView>(R.id.cat_header_show_all_arrow).imageTintList = tint(accent)
                showAll.setOnClickListener { dismissKeyboard(); showAlbum(category) }
            } else {
                showAll.visibility = View.GONE
            }
            addSpaced(container, header)
            addSpaced(container, buildRow(poses.take(3)))
        }
    }

    // A row of up to 3 blueprint thumbnails (weighted columns, 10dp gaps).
    private fun buildRow(poses: List<PoseItem>): LinearLayout {
        val row = LinearLayout(host.requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        val gap = (10f * d).toInt()
        val inflater = LayoutInflater.from(host.requireContext())
        poses.forEachIndexed { i, pose ->
            val thumb = inflater.inflate(R.layout.item_pose_thumb, row, false)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            if (i > 0) lp.marginStart = gap
            thumb.layoutParams = lp
            bindThumb(thumb, pose)
            row.addView(thumb)
        }
        repeat(3 - poses.size) { i ->
            val spacer = View(host.requireContext())
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            if (poses.isNotEmpty() || i > 0) lp.marginStart = gap
            spacer.layoutParams = lp
            row.addView(spacer)
        }
        return row
    }

    private fun bindThumb(thumb: View, pose: PoseItem) {
        thumb.clipToOutline = true
        thumb.outlineProvider = roundOutline(8f)
        thumb.background = rounded(0xFF14141A.toInt(), 8f)
        thumb.foreground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f * d
            setColor(Color.TRANSPARENT)
            setStroke((0.5f * d).toInt().coerceAtLeast(1), glass)
        }
        loadPose(thumb.findViewById(R.id.pose_thumb_image), pose.image)
        thumb.findViewById<TextView>(R.id.pose_thumb_title).apply {
            text = pose.title
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.TRANSPARENT, 0xBF000000.toInt())
            )
        }
        thumb.setOnClickListener { openPose(pose) }
    }

    private fun openPose(pose: PoseItem) {
        dismissKeyboard()
        viewModel.setSearchQuery("")
        viewModel.selectPose(pose)
        onOpenCamera()
    }

    // ---- "Show all" album overlay --------------------------------------------------------

    private fun showAlbum(category: String) {
        showAllCategory = category
        val overlay = albumOverlay
        overlay.removeAllViews()
        val v = LayoutInflater.from(host.requireContext()).inflate(R.layout.view_category_album, overlay, false)
        v.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )
        val baseTop = v.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(v) { view, insets ->
            view.updatePadding(top = baseTop + insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            insets
        }
        v.findViewById<TextView>(R.id.cat_album_title).text = category
        v.findViewById<View>(R.id.cat_album_back).setOnClickListener { closeAlbum() }
        overlay.addView(v)
        overlay.visibility = View.VISIBLE
        rebuildAlbumGrid(viewModel.defaultPoses.value.filter { it.category == category })
        ViewCompat.requestApplyInsets(v)

        val width = (root.width.takeIf { it > 0 } ?: root.resources.displayMetrics.widthPixels).toFloat()
        overlay.translationX = width
        overlay.animate().translationX(0f).setDuration(300).start()
    }

    private fun rebuildAlbumGrid(poses: List<PoseItem>) {
        val grid = albumOverlay.findViewById<LinearLayout>(R.id.cat_album_grid) ?: return
        grid.removeAllViews()
        poses.chunked(3).forEachIndexed { idx, rowPoses ->
            val row = buildRow(rowPoses)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = if (idx > 0) (10f * d).toInt() else 0
            grid.addView(row, lp)
        }
    }

    private fun closeAlbum() {
        val overlay = albumOverlay
        val width = (root.width.takeIf { it > 0 } ?: root.resources.displayMetrics.widthPixels).toFloat()
        overlay.animate().translationX(width).setDuration(300).withEndAction {
            overlay.visibility = View.GONE
            overlay.removeAllViews()
            overlay.translationX = 0f
        }.start()
        showAllCategory = null
    }

    // ---- Image loading -------------------------------------------------------------------

    private fun loadPose(image: ImageView, imageStr: String) {
        image.setImageDrawable(null)
        if (imageStr.isEmpty()) return
        val assetPath = assetPathOf(imageStr)
        when {
            assetPath != null -> loadAsset(image, assetPath)
            imageStr.startsWith("http") || imageStr.contains("/") ->
                image.context.imageLoader.enqueue(
                    ImageRequest.Builder(image.context).data(imageStr).crossfade(true).target(image).build()
                )
            else -> {
                val id = image.resources.getIdentifier(imageStr, "drawable", host.requireContext().packageName)
                if (id != 0) image.setImageResource(id)
            }
        }
    }

    private fun loadAsset(image: ImageView, assetPath: String) {
        poseBitmapCache.get(assetPath)?.let { image.setImageBitmap(it); return }
        image.tag = assetPath
        jobs += host.viewLifecycleOwner.lifecycleScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                decodeAsset(host.requireContext(), assetPath)?.also { poseBitmapCache.put(assetPath, it) }
            }
            if (bmp != null && image.tag == assetPath) image.setImageBitmap(bmp)
        }
    }

    // ---- Helpers -------------------------------------------------------------------------

    private fun addSpaced(container: LinearLayout, view: View, height: Int? = null) {
        val existing = view.layoutParams as? LinearLayout.LayoutParams
        val lp = existing ?: LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.width = LinearLayout.LayoutParams.MATCH_PARENT
        // Keep an inflated view's own height (e.g. the 128dp hero) unless an explicit height is given.
        if (height != null) lp.height = height
        if (container.childCount > 0) lp.topMargin = (12f * d).toInt()
        container.addView(view, lp)
    }

    private fun dismissKeyboard() {
        val imm = host.requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(root.windowToken, 0)
        root.findViewById<EditText>(R.id.home_search_input)?.clearFocus()
    }

    private fun tint(color: Int) = android.content.res.ColorStateList.valueOf(color)

    private fun rounded(color: Int, radiusDp: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusDp * d
        setColor(color)
    }

    private fun roundOutline(radiusDp: Float) = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radiusDp * d)
        }
    }
}
