package com.aipose.camera.posematch.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
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
import com.aipose.camera.posematch.data.CapturedPhoto
import com.aipose.camera.posematch.databinding.DialogDeleteBinding
import com.aipose.camera.posematch.databinding.FragmentCollectionsBinding
import com.aipose.camera.posematch.databinding.ItemCollectionHeaderBinding
import com.aipose.camera.posematch.databinding.ItemCollectionThumbBinding
import com.aipose.camera.posematch.databinding.ItemDetailRowBinding
import com.aipose.camera.posematch.databinding.ViewLocationAlbumBinding
import com.aipose.camera.posematch.databinding.ViewPhotoDetailBinding
import com.aipose.camera.posematch.admob_ads.ScreenBottomAd
import com.aipose.camera.posematch.admob_ads.backInter
import com.aipose.camera.posematch.admob_ads.canShowAds
import com.aipose.camera.posematch.admob_ads.forwardInter
import com.aipose.camera.posematch.admob_ads.remote.NATIVE_DETAIL
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig
import com.aipose.camera.posematch.admob_ads.showInterThen
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.util.applySystemBarInsets
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import com.aipose.camera.posematch.ui.viewmodel.formatHistoryDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.aipose.camera.posematch.analytics.Analytics

/**
 * Binds the real-XML Collections tab ([R.layout.fragment_collections]) to the shared [MainViewModel],
 * reproducing HistoryScreen: a search box, a list grouped by location (≤6 per group + "See all"), a
 * full-location album overlay and a full-screen photo-detail overlay with share/delete. Behaviour and
 * flows (capturedHistory / filteredHistory / historyQuery) match the original.
 */
class CollectionsBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View,
    // Hides/shows the container's bottom nav while the full-screen photo preview is open.
    private val onDetailOpenChanged: (Boolean) -> Unit = {}
) {
    private val binding = FragmentCollectionsBinding.bind(root)
    private var albumBinding: ViewLocationAlbumBinding? = null
    private var detailBinding: ViewPhotoDetailBinding? = null
    private val jobs = mutableListOf<Job>()
    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(root.context)
    private val accent = palette.accent
    private val card = palette.card
    private val glass = palette.glass
    private val gray = palette.textSecondary
    private val scoreGreen = 0xFF81C784.toInt()
    private val scoreOrange = 0xFFFFB74D.toInt()

    private var albumLocation: String? = null
    private var selectedPhotoId: Long? = null
    private var deleteDialog: Dialog? = null

    /** Bottom ad on the full-screen photo preview; released whenever the preview closes. */
    private var detailAdSlot: ScreenBottomAd.Slot? = null

    private val main get() = binding.collectionsMain
    private val list get() = binding.collectionsList
    private val scroll get() = binding.collectionsScroll
    private val empty get() = binding.collectionsEmpty
    private val albumOverlay get() = binding.collectionsAlbumOverlay
    private val detailOverlay get() = binding.collectionsDetailOverlay

    fun bind() {
        Analytics.screen(Analytics.Screen.COLLECTIONS)
        // Status-bar inset on top of the content (edge-to-edge safe on all versions).
        main.applySystemBarInsets(top = true)

        binding.collectionsSearchBar.background = rounded(card, 14f)

        val input = binding.collectionsSearchInput
        val clear = binding.collectionsSearchClear
        input.setText(viewModel.historyQuery.value)
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                if (text != viewModel.historyQuery.value) viewModel.setHistoryQuery(text)
            }
        })
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { dismissKeyboard(); true } else false
        }
        clear.setOnClickListener {
            input.setText("")
            viewModel.setHistoryQuery("")
            dismissKeyboard()
        }

        // Observe the history flows; any change re-renders (matches Compose recomposition).
        for (flow in listOf(viewModel.filteredHistory, viewModel.capturedHistory, viewModel.historyQuery)) {
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
        deleteDialog?.dismiss(); deleteDialog = null
        detailAdSlot?.release(); detailAdSlot = null
        // Reset search when leaving (the Compose onDispose did the same).
        viewModel.setHistoryQuery("")
    }

    /** @return true if a back press was consumed (an open overlay was closed). */
    fun onBack(): Boolean = when {
        selectedPhotoId != null -> {
            host.showInterThen(backInter(RemoteConfig.interDetailBack)) { closeDetail() }
            true
        }
        albumLocation != null -> { closeAlbum(); true }
        else -> false
    }

    // ---- Rendering -----------------------------------------------------------------------

    private fun render() {
        val all = viewModel.capturedHistory.value
        val filtered = viewModel.filteredHistory.value
        val query = viewModel.historyQuery.value

        binding.collectionsSubtitle.text = host.getString(R.string.history_subtitle, all.size)
        binding.collectionsSearchClear.visibility =
            if (query.isNotEmpty()) View.VISIBLE else View.GONE

        if (filtered.isEmpty()) {
            scroll.visibility = View.GONE
            empty.visibility = View.VISIBLE
            binding.collectionsEmptyIcon
                .setImageResource(if (query.isEmpty()) R.drawable.ic_schedule else R.drawable.ic_search)
            binding.collectionsEmptyText.text =
                if (query.isEmpty()) host.getString(R.string.history_empty)
                else host.getString(R.string.history_no_match, query)
            binding.collectionsEmptySub.visibility =
                if (query.isEmpty()) View.VISIBLE else View.GONE
        } else {
            empty.visibility = View.GONE
            scroll.visibility = View.VISIBLE
            buildGroups(filtered)
        }

        // Keep open overlays in sync with DB updates.
        albumLocation?.let { loc -> rebuildAlbumGrid(filtered.filter { it.locationName == loc }) }
        selectedPhotoId?.let { id ->
            val photo = all.firstOrNull { it.id == id }
            if (photo == null) closeDetail() else bindDetail(photo)
        }
    }

    private fun buildGroups(photos: List<CapturedPhoto>) {
        val container = list
        container.removeAllViews()
        val inflater = LayoutInflater.from(host.requireContext())
        val grouped = photos.groupBy { it.locationName }
        var first = true
        for ((location, groupPhotos) in grouped) {
            val headerB = ItemCollectionHeaderBinding.inflate(inflater, container, false)
            headerB.headerLocation.text = location
            headerB.headerCount.apply {
                text = groupPhotos.size.toString()
                setTextColor(accent)
                background = rounded((accent and 0x00FFFFFF) or (46 shl 24), 8f)
            }
            if (groupPhotos.size > 6) {
                headerB.headerSeeAll.visibility = View.VISIBLE
                headerB.headerSeeAllText.setTextColor(accent)
                headerB.headerSeeAllArrow.imageTintList = android.content.res.ColorStateList.valueOf(accent)
                headerB.headerSeeAll.setOnClickListener { dismissKeyboard(); showAlbum(location) }
            } else {
                headerB.headerSeeAll.visibility = View.GONE
            }
            (headerB.root.layoutParams as LinearLayout.LayoutParams).topMargin = if (first) 0 else (18f * d).toInt()
            container.addView(headerB.root)

            val grid = LinearLayout(host.requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = (8f * d).toInt()
                layoutParams = lp
            }
            buildGrid(grid, groupPhotos.take(6))
            container.addView(grid)
            first = false
        }
    }

    // 3-column chunked grid of square thumbnails.
    private fun buildGrid(container: LinearLayout, photos: List<CapturedPhoto>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(host.requireContext())
        val gap = (8f * d).toInt()
        photos.chunked(3).forEachIndexed { rowIdx, rowPhotos ->
            val row = LinearLayout(host.requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (rowIdx > 0) lp.topMargin = gap
                layoutParams = lp
            }
            rowPhotos.forEachIndexed { i, photo ->
                val thumbB = ItemCollectionThumbBinding.inflate(inflater, row, false)
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                if (i > 0) lp.marginStart = gap
                thumbB.root.layoutParams = lp
                bindThumb(thumbB, photo)
                row.addView(thumbB.root)
            }
            // Fill the remaining columns so widths stay equal.
            repeat(3 - rowPhotos.size) { i ->
                val spacer = View(host.requireContext())
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                if (rowPhotos.isNotEmpty() || i > 0) lp.marginStart = gap
                spacer.layoutParams = lp
                row.addView(spacer)
            }
            container.addView(row)
        }
    }

    private fun bindThumb(thumb: ItemCollectionThumbBinding, photo: CapturedPhoto) {
        thumb.root.clipToOutline = true
        thumb.root.outlineProvider = roundOutline(14f)
        thumb.root.background = rounded(card, 14f)
        thumb.root.foreground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 14f * d
            setColor(Color.TRANSPARENT)
            setStroke((0.5f * d).toInt().coerceAtLeast(1), glass)
        }

        loadPhoto(thumb.thumbImage, thumb.thumbBroken, photo.imagePath)

        thumb.thumbScore.apply {
            text = "${photo.matchScore}%"
            setTextColor(if (photo.matchScore >= 80) scoreGreen else scoreOrange)
            background = rounded(0x8C000000.toInt(), 8f)
        }
        thumb.thumbFavorite.apply {
            visibility = if (photo.isFavorite) View.VISIBLE else View.GONE
            imageTintList = android.content.res.ColorStateList.valueOf(accent)
        }
        thumb.thumbDate.apply {
            text = SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault()).format(Date(photo.dateTimestamp))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.TRANSPARENT, 0x99000000.toInt())
            )
        }
        thumb.root.setOnClickListener {
            dismissKeyboard()
            Analytics.click("collection_photo", Analytics.Screen.COLLECTIONS)
            host.showInterThen(forwardInter(RemoteConfig.interCollectionDetail)) { showDetail(photo) }
        }
    }

    // ---- Album overlay -------------------------------------------------------------------

    private fun showAlbum(location: String) {
        albumLocation = location
        val overlay = albumOverlay
        overlay.removeAllViews()
        val albumB = ViewLocationAlbumBinding.inflate(LayoutInflater.from(host.requireContext()), overlay, false)
        albumBinding = albumB
        albumB.root.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )
        albumB.root.applySystemBarInsets(top = true)
        albumB.albumTitle.text = location
        albumB.albumBack.setOnClickListener { closeAlbum() }
        overlay.addView(albumB.root)
        overlay.visibility = View.VISIBLE
        rebuildAlbumGrid(viewModel.filteredHistory.value.filter { it.locationName == location })
        ViewCompat.requestApplyInsets(albumB.root)

        val width = (root.width.takeIf { it > 0 } ?: root.resources.displayMetrics.widthPixels).toFloat()
        overlay.translationX = width
        overlay.animate().translationX(0f).setDuration(300).start()
    }

    private fun rebuildAlbumGrid(photos: List<CapturedPhoto>) {
        val albumB = albumBinding ?: return
        albumB.albumCount.text = "${photos.size} ${host.getString(R.string.frames)}"
        buildGrid(albumB.albumGrid, photos)
    }

    private fun closeAlbum() {
        val overlay = albumOverlay
        val width = (root.width.takeIf { it > 0 } ?: root.resources.displayMetrics.widthPixels).toFloat()
        overlay.animate().translationX(width).setDuration(300).withEndAction {
            overlay.visibility = View.GONE
            overlay.removeAllViews()
            overlay.translationX = 0f
            albumBinding = null
        }.start()
        albumLocation = null
    }

    // ---- Detail overlay ------------------------------------------------------------------

    private fun showDetail(photo: CapturedPhoto) {
        selectedPhotoId = photo.id
        onDetailOpenChanged(true)
        val overlay = detailOverlay
        overlay.removeAllViews()
        val detailB = ViewPhotoDetailBinding.inflate(LayoutInflater.from(host.requireContext()), overlay, false)
        detailBinding = detailB
        detailB.root.applySystemBarInsets(top = true, bottom = true)
        // Theme-aware gradient background + a subtly bordered card (works in dark and light).
        detailB.root.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(palette.bgTop, palette.bgBottom)
        )
        detailB.detailCard.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f * d
            setColor(card)
            setStroke((1f * d).toInt().coerceAtLeast(1), glass)
        }
        detailB.detailImage.apply {
            clipToOutline = true
            outlineProvider = roundOutline(16f)
        }
        detailB.detailShare.background = rounded(accent, 12f)
        detailB.detailDelete.background = outlined(12f)
        detailB.detailBack.setOnClickListener {
            host.showInterThen(backInter(RemoteConfig.interDetailBack)) { closeDetail() }
        }
        overlay.addView(detailB.root)
        overlay.visibility = View.VISIBLE
        ViewCompat.requestApplyInsets(detailB.root)
        bindDetail(photo)
        renderDetailAd(detailB)

        overlay.alpha = 0f
        overlay.scaleX = 0.94f
        overlay.scaleY = 0.94f
        overlay.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220).start()
    }

    private fun bindDetail(photo: CapturedPhoto) {
        val detailB = detailBinding ?: return
        detailB.detailTitle.text = photo.title
        loadPhoto(detailB.detailImage, detailB.detailBroken, photo.imagePath)

        detailB.detailRows.removeAllViews()
        addDetailRow(detailB.detailRows, R.drawable.ic_location_on, host.getString(R.string.detail_location), photo.locationName, palette.textPrimary)
        addDetailRow(detailB.detailRows, R.drawable.ic_schedule, host.getString(R.string.detail_captured), formatHistoryDate(photo.dateTimestamp), palette.textPrimary)
        addDetailRow(detailB.detailRows, R.drawable.ic_analytics, host.getString(R.string.detail_match_score), "${photo.matchScore}%",
            if (photo.matchScore >= 80) scoreGreen else scoreOrange)
        addDetailRow(detailB.detailRows, R.drawable.ic_category, host.getString(R.string.detail_category), photo.category, palette.textPrimary)

        detailB.detailShare.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${photo.title} • ${photo.locationName} • Match ${photo.matchScore}% — via Pose Match Camera"
                )
            }
            host.startActivity(Intent.createChooser(share, host.getString(R.string.share_frame)))
        }
        detailB.detailDelete.setOnClickListener { showDeleteDialog(photo) }
    }

    private fun addDetailRow(container: LinearLayout, iconRes: Int, label: String, value: String, valueColor: Int) {
        val rowB = ItemDetailRowBinding.inflate(LayoutInflater.from(host.requireContext()), container, false)
        rowB.rowIcon.setImageResource(iconRes)
        rowB.rowLabel.text = label
        rowB.rowValue.apply {
            text = value
            setTextColor(valueColor)
        }
        (rowB.root.layoutParams as? LinearLayout.LayoutParams)?.topMargin =
            if (container.childCount > 0) (10f * d).toInt() else 0
        container.addView(rowB.root)
    }

    /** Photo-preview bottom ad — a browsing screen with room below the metadata card. */
    private fun renderDetailAd(detailB: ViewPhotoDetailBinding) {
        detailAdSlot?.release()
        detailAdSlot = null
        detailB.detailAdHolder.removeAllViews()
        detailB.detailAdHolder.visibility = View.GONE
        detailB.detailAdText.visibility = View.GONE
        if (!host.requireContext().canShowAds(true)) return

        detailAdSlot = ScreenBottomAd.render(
            activity = host.requireActivity(),
            owner = host.viewLifecycleOwner,
            holder = detailB.detailAdHolder,
            label = detailB.detailAdText,
            mode = RemoteConfig.nativeDetail,
            placement = NATIVE_DETAIL,
            nativeId = host.getString(R.string.NativeAll),
            bannerId = host.getString(R.string.Banner_Ad),
            tag = "ScreenNative-Detail"
        )
    }

    private fun closeDetail() {
        selectedPhotoId = null
        detailAdSlot?.release()
        detailAdSlot = null
        val overlay = detailOverlay
        overlay.animate().alpha(0f).scaleX(0.94f).scaleY(0.94f).setDuration(180).withEndAction {
            overlay.visibility = View.GONE
            overlay.removeAllViews()
            overlay.alpha = 1f; overlay.scaleX = 1f; overlay.scaleY = 1f
            detailBinding = null
        }.start()
        onDetailOpenChanged(false)
    }

    private fun showDeleteDialog(photo: CapturedPhoto) {
        val ctx = host.requireContext()
        val deleteB = DialogDeleteBinding.inflate(host.layoutInflater)
        deleteB.root.background = rounded(0xFF161619.toInt(), 16f)
        deleteB.deleteCancel.background = outlined(12f)
        deleteB.deleteConfirm.background = rounded(0xFFEF5350.toInt(), 12f)
        val dialog = Dialog(ctx).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(deleteB.root)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // Programmatically set width to screen width minus 20dp margin on each side (40dp total).
                val width = (ctx.resources.displayMetrics.widthPixels - (40 * d)).toInt()
                setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.CENTER)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.7f)
            }
        }
        deleteDialog = dialog
        deleteB.deleteCancel.setOnClickListener { dialog.dismiss() }
        deleteB.deleteConfirm.setOnClickListener {
            dialog.dismiss()
            viewModel.deletePhoto(photo)
            closeDetail()
        }
        dialog.show()
    }

    // ---- Helpers -------------------------------------------------------------------------

    private fun loadPhoto(image: ImageView, broken: ImageView, path: String) {
        val file = File(path)
        if (file.exists()) {
            broken.visibility = View.GONE
            image.visibility = View.VISIBLE
            image.context.imageLoader.enqueue(
                ImageRequest.Builder(image.context).data(file).crossfade(true).target(image).build()
            )
        } else {
            image.visibility = View.GONE
            broken.visibility = View.VISIBLE
        }
    }

    private fun dismissKeyboard() {
        val imm = host.requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(root.windowToken, 0)
        binding.collectionsSearchInput.clearFocus()
    }

    private fun rounded(color: Int, radiusDp: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusDp * d
        setColor(color)
    }

    private fun outlined(radiusDp: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusDp * d
        setColor(Color.TRANSPARENT)
        setStroke((1f * d).toInt().coerceAtLeast(1), glass)
    }

    private fun roundOutline(radiusDp: Float) = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radiusDp * d)
        }
    }
}
