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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.data.PoseItem
import com.aipose.camera.posematch.databinding.FragmentHomeBinding
import com.aipose.camera.posematch.databinding.ItemCategoryHeaderBinding
import com.aipose.camera.posematch.databinding.ItemPoseThumbBinding
import com.aipose.camera.posematch.databinding.ViewCategoryAlbumBinding
import com.aipose.camera.posematch.databinding.ViewHomeHeroBinding
import com.aipose.camera.posematch.ui.screens.assetPathOf
import com.aipose.camera.posematch.ui.screens.decodeAsset
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.util.applySystemBarInsets
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.aipose.camera.posematch.analytics.Analytics

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

class HomeBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View,
    private val onOpenCamera: () -> Unit,
    private val onLaunchGalleryPicker: () -> Unit,
    // Hides/shows the container's bottom nav while the full-screen "Show all" album is open.
    private val onAlbumOpenChanged: (Boolean) -> Unit = {}
) {
    private val binding = FragmentHomeBinding.bind(root)
    private val jobs = mutableListOf<Job>()
    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(root.context)
    private val accent = palette.accent
    private val card = palette.card
    private val glass = palette.glass
    private val gray = palette.textSecondary

    private var showAllCategory: String? = null
    private var albumBinding: ViewCategoryAlbumBinding? = null

    private val dynamic get() = binding.homeDynamic

    fun bind() {
        Analytics.screen(Analytics.Screen.HOME)
        binding.homeTopbar.applySystemBarInsets(top = true)

        binding.homeSearchBar.background = rounded(card, 14f)
        binding.homeGalleryIcon.apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14f * d
                setColor(card)
                setStroke((1f * d).toInt().coerceAtLeast(1), glass)
            }
            setOnClickListener { dismissKeyboard(); onLaunchGalleryPicker() }
        }

        val input = binding.homeSearchInput
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
        binding.homeSearchClear.setOnClickListener {
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
        binding.homeSearchClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

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
                val box = android.widget.FrameLayout(host.requireContext()).apply {
                    background = rounded(card, 16f)
                    val tv = TextView(context).apply {
                        text = host.getString(R.string.home_no_blueprints)
                        setTextColor(gray); textSize = 14f
                    }
                    addView(tv, android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
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
        val hero = ViewHomeHeroBinding.inflate(LayoutInflater.from(host.requireContext()), container, false)
        hero.root.clipToOutline = true
        hero.root.outlineProvider = roundOutline(8f)
        hero.root.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(accent, (accent and 0x00FFFFFF) or (184 shl 24))
        )
        val heroAsset = heroPose?.image?.let(::assetPathOf)
        if (heroAsset != null) {
            hero.heroImage.visibility = View.VISIBLE
            hero.heroScrim.visibility = View.VISIBLE
            hero.heroScrim.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(accent, (accent and 0x00FFFFFF) or (217 shl 24), Color.TRANSPARENT)
            )
            loadAsset(hero.heroImage, heroAsset)
        } else {
            hero.heroImage.visibility = View.GONE
            hero.heroScrim.visibility = View.GONE
        }
        hero.heroCtaIcon.imageTintList = tint(accent)
        hero.heroCtaText.setTextColor(accent)
        hero.heroCtaArrow.imageTintList = tint(accent)
        hero.heroCta.apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f * d
                setColor(Color.WHITE)
            }
            setOnClickListener {
                dismissKeyboard()
                Analytics.click("hero_start_posing", Analytics.Screen.HOME)
                heroPose?.let { viewModel.selectPose(it) }
                onOpenCamera()
            }
        }
        addSpaced(container, hero.root)

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
            val header = ItemCategoryHeaderBinding.inflate(LayoutInflater.from(host.requireContext()), container, false)
            header.catHeaderIcon.apply {
                setImageResource(categoryIconRes(category)); imageTintList = tint(accent)
            }
            header.catHeaderName.text = category
            if (poses.size > 3) {
                header.catHeaderShowAll.visibility = View.VISIBLE
                header.catHeaderShowAllText.setTextColor(accent)
                header.catHeaderShowAllArrow.imageTintList = tint(accent)
                header.catHeaderShowAll.setOnClickListener { dismissKeyboard(); showAlbum(category) }
            } else {
                header.catHeaderShowAll.visibility = View.GONE
            }
            addSpaced(container, header.root)
            addSpaced(container, buildRow(poses.take(3)))
        }
    }

    // A row of up to 3 blueprint thumbnails (weighted columns, 10dp gaps).
    private fun buildRow(poses: List<PoseItem>): LinearLayout {
        val row = LinearLayout(host.requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        val gap = (10f * d).toInt()
        val inflater = LayoutInflater.from(host.requireContext())
        poses.forEachIndexed { i, pose ->
            val thumb = ItemPoseThumbBinding.inflate(inflater, row, false)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            if (i > 0) lp.marginStart = gap
            thumb.root.layoutParams = lp
            bindThumb(thumb, pose)
            row.addView(thumb.root)
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

    private fun bindThumb(thumb: ItemPoseThumbBinding, pose: PoseItem) {
        thumb.root.clipToOutline = true
        thumb.root.outlineProvider = roundOutline(8f)
        thumb.root.background = rounded(0xFF14141A.toInt(), 8f)
        thumb.root.foreground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f * d
            setColor(Color.TRANSPARENT)
            setStroke((0.5f * d).toInt().coerceAtLeast(1), glass)
        }
        loadPose(thumb.poseThumbImage, pose.image)
        thumb.root.setOnClickListener { openPose(pose) }
    }

    private fun openPose(pose: PoseItem) {
        dismissKeyboard()
        viewModel.setSearchQuery("")
        Analytics.click("pose_selected", Analytics.Screen.HOME)
        viewModel.selectPose(pose)
        onOpenCamera()
    }

    // ---- "Show all" album overlay --------------------------------------------------------

    private fun showAlbum(category: String) {
        showAllCategory = category
        onAlbumOpenChanged(true)
        val overlay = binding.homeAlbumOverlay
        overlay.removeAllViews()
        val albumB = ViewCategoryAlbumBinding.inflate(LayoutInflater.from(host.requireContext()), overlay, false)
        albumBinding = albumB
        albumB.root.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )
        // Full-screen album: pad for the status bar (top) and the system nav bar (bottom) so the
        // grid doesn't scroll behind the system icons now that the bottom nav is hidden.
        albumB.root.applySystemBarInsets(top = true, bottom = true)
        albumB.catAlbumTitle.text = category
        albumB.catAlbumBack.setOnClickListener { closeAlbum() }
        overlay.addView(albumB.root)
        overlay.visibility = View.VISIBLE
        rebuildAlbumGrid(viewModel.defaultPoses.value.filter { it.category == category })
        ViewCompat.requestApplyInsets(albumB.root)

        val width = (root.width.takeIf { it > 0 } ?: root.resources.displayMetrics.widthPixels).toFloat()
        overlay.translationX = width
        overlay.animate().translationX(0f).setDuration(300).start()
    }

    private fun rebuildAlbumGrid(poses: List<PoseItem>) {
        val grid = albumBinding?.catAlbumGrid ?: return
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
        val overlay = binding.homeAlbumOverlay
        val width = (root.width.takeIf { it > 0 } ?: root.resources.displayMetrics.widthPixels).toFloat()
        overlay.animate().translationX(width).setDuration(300).withEndAction {
            overlay.visibility = View.GONE
            overlay.removeAllViews()
            overlay.translationX = 0f
            albumBinding = null
        }.start()
        showAllCategory = null
        onAlbumOpenChanged(false)
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
        binding.homeSearchInput.clearFocus()
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
