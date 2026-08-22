package com.aipose.camera.posematch.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
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
import com.aipose.camera.posematch.domain.PhotoFilters
import com.aipose.camera.posematch.domain.PoseDetectorProcessor
import com.aipose.camera.posematch.domain.SubjectExtractor
import com.aipose.camera.posematch.ui.screens.assetPathOf
import com.aipose.camera.posematch.ui.screens.decodeAsset
import com.aipose.camera.posematch.ui.screens.loadOverlayBitmap
import com.aipose.camera.posematch.ui.screens.savePhoto
import com.aipose.camera.posematch.ui.screens.takePhotoToFile
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import com.aipose.camera.posematch.ui.widget.TransformGestureDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

/**
 * Binds the real-XML camera ([R.layout.fragment_camera]) to the shared [MainViewModel]. Ports the
 * Compose PoseCameraScreen logic verbatim — CameraX preview + ML Kit pose analysis, the transformable
 * reference overlay, live match feedback, opacity, the pro panel, the reference strip, capture with
 * timer — in a tidier, more compact layout. The post-capture editor and success screens remain
 * ComposeViews (their own migration steps) shown as overlays here.
 */
class CameraBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View,
    private val onExit: () -> Unit,
    private val onGoHome: () -> Unit
) {
    private val ctx get() = host.requireContext()
    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(viewModel.appTheme.value)
    private val accent = palette.accent
    private val card = palette.card

    private val previewView = root.findViewById<PreviewView>(R.id.camera_preview_view)
    private val imageCapture = ImageCapture.Builder().build()
    private val poseProcessor = PoseDetectorProcessor()
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx.applicationContext)
    private var cameraProvider: ProcessCameraProvider? = null
    private val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val jobs = mutableListOf<Job>()
    private var poseImageJob: Job? = null
    private var timerJob: Job? = null
    private var greatFlashJob: Job? = null

    private var showProPanel = false
    private var activeTimerCountdown = -1
    private var wasGreat = false
    private var prevScore = Int.MIN_VALUE
    private var reviewPhotoPath: String? = null
    private var savedPhotoPath: String? = null

    private val refItems = HashMap<Int, View>()

    private val uiPrefs = ctx.getSharedPreferences("posematch_ui", Context.MODE_PRIVATE)

    private val transform = TransformGestureDetector { panX, panY, zoom, rotation ->
        val region = root.findViewById<View>(R.id.camera_preview_region)
        val w = if (region.width > 0) region.width.toFloat() else 1f
        val h = if (region.height > 0) region.height.toFloat() else 1f
        viewModel.updatePoseGestures(
            scale = viewModel.poseScale.value * zoom,
            offsetX = viewModel.poseOffsetX.value + panX / w,
            offsetY = viewModel.poseOffsetY.value + panY / h,
            rotation = viewModel.poseRotation.value + rotation
        )
    }

    fun bind() {
        // Insets.
        val topbar = root.findViewById<View>(R.id.camera_topbar)
        val dock = root.findViewById<View>(R.id.camera_dock)
        val baseTop = topbar.paddingTop
        val baseBottom = dock.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            topbar.updatePadding(top = baseTop + bars.top)
            dock.updatePadding(bottom = baseBottom + bars.bottom)
            insets
        }

        styleChrome()
        wireControls()

        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        @Suppress("ClickableViewAccessibility")
        root.findViewById<View>(R.id.camera_preview_region).setOnTouchListener { _, e -> transform.onTouchEvent(e) }

        startCameraIfPermitted()
        observeFlows()
        maybeShowCoach()
    }

    fun unbind() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        poseImageJob?.cancel(); timerJob?.cancel(); greatFlashJob?.cancel()
        try { cameraProvider?.unbindAll() } catch (_: Exception) {}
        cameraExecutor.shutdown()
    }

    /** @return true if a back press was consumed (an overlay was open). */
    fun onBack(): Boolean = when {
        savedPhotoPath != null -> { savedPhotoPath = null; hideOverlay(R.id.camera_success_overlay); onGoHome(); true }
        reviewPhotoPath != null -> {
            reviewPhotoPath?.let { runCatching { File(it).delete() } }
            reviewPhotoPath = null; hideOverlay(R.id.camera_editor_overlay); true
        }
        else -> false
    }

    // ---- Static chrome styling -----------------------------------------------------------

    private fun styleChrome() {
        // Shutter: white outer ring + white inner disc with a black rim.
        root.findViewById<View>(R.id.camera_shutter).background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(Color.WHITE)
        }
        root.findViewById<View>(R.id.camera_shutter_inner).background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.WHITE)
            setStroke((3f * d).toInt(), Color.BLACK)
        }
        root.findViewById<View>(R.id.camera_flip).background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(card)
        }
        root.findViewById<View>(R.id.camera_pro_panel).setBackgroundColor(card)
        root.findViewById<TextView>(R.id.camera_match_coach).background = rounded(0x8C000000.toInt(), 14f)
        root.findViewById<TextView>(R.id.camera_countdown).setTextColor(accent)
        root.findViewById<View>(R.id.camera_great_flash).background =
            rounded((0xFF2E7D32.toInt() and 0x00FFFFFF) or (235 shl 24), 24f)
    }

    private fun wireControls() {
        root.findViewById<View>(R.id.camera_back).setOnClickListener { onExit() }
        root.findViewById<View>(R.id.camera_flash).setOnClickListener { viewModel.toggleFlash() }
        root.findViewById<View>(R.id.camera_tune).setOnClickListener {
            showProPanel = !showProPanel
            root.findViewById<View>(R.id.camera_pro_panel).visibility = if (showProPanel) View.VISIBLE else View.GONE
            updateTuneTint()
        }
        root.findViewById<View>(R.id.camera_shutter).setOnClickListener { triggerCapture() }
        root.findViewById<View>(R.id.camera_flip).setOnClickListener { viewModel.toggleCameraFacing() }
        root.findViewById<View>(R.id.camera_grid_toggle).setOnClickListener { viewModel.toggleGridVisible() }
        root.findViewById<View>(R.id.camera_timer_toggle).setOnClickListener {
            viewModel.setTimer(when (viewModel.cameraTimer.value) { 0 -> 3; 3 -> 5; else -> 0 })
        }

        seek(R.id.camera_opacity_seek) { viewModel.updatePoseOpacity(it / 100f) }
        seek(R.id.camera_iso_seek) { viewModel.setProIso(100 + it) }
        seek(R.id.camera_ev_seek) { viewModel.setProExposure(it / 10f - 2f) }
        for (id in intArrayOf(R.id.camera_opacity_seek, R.id.camera_iso_seek, R.id.camera_ev_seek)) {
            root.findViewById<SeekBar>(id).apply {
                progressTintList = tint(accent); thumbTintList = tint(accent)
            }
        }
    }

    // ---- Camera ---------------------------------------------------------------------------

    private fun startCameraIfPermitted() {
        val granted = ContextCompat.checkSelfPermission(
            ctx, android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) {
            root.findViewById<View>(R.id.camera_sim).visibility = View.VISIBLE
            return
        }
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(ctx))
    }

    private fun bindCamera() {
        val provider = cameraProvider ?: return
        val facing = viewModel.cameraFacing.value
        try {
            val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
            val preview = Preview.Builder().setTargetRotation(rotation).build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture.targetRotation = rotation
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { a ->
                    a.setAnalyzer(cameraExecutor) { proxy ->
                        poseProcessor.processImageProxy(proxy, isFrontCamera = facing != "Back") { lm ->
                            viewModel.updateDetectedPose(lm)
                        }
                    }
                }
            provider.unbindAll()
            provider.bindToLifecycle(
                host.viewLifecycleOwner,
                if (facing == "Back") CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA,
                preview, imageCapture, analysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerCapture() {
        if (activeTimerCountdown > 0) return
        val timer = viewModel.cameraTimer.value
        if (timer > 0 && activeTimerCountdown == -1) {
            timerJob = host.viewLifecycleOwner.lifecycleScope.launch {
                activeTimerCountdown = timer; updateCountdown()
                while (activeTimerCountdown > 0) {
                    delay(1000); activeTimerCountdown--; updateCountdown()
                }
                activeTimerCountdown = -1; updateCountdown()
                capture()
            }
        } else {
            capture()
        }
    }

    private fun capture() {
        takePhotoToFile(ctx, imageCapture, cameraExecutor, viewModel.cameraFlashMode.value) { path ->
            showEditor(path)
        }
    }

    private fun updateCountdown() {
        val tv = root.findViewById<TextView>(R.id.camera_countdown)
        if (activeTimerCountdown > 0) { tv.text = activeTimerCountdown.toString(); tv.visibility = View.VISIBLE }
        else tv.visibility = View.GONE
    }

    // ---- Editor / success overlays -------------------------------------------------------

    private fun showEditor(path: String) {
        reviewPhotoPath = path
        val overlay = root.findViewById<android.widget.FrameLayout>(R.id.camera_editor_overlay)
        overlay.removeAllViews()
        val editorView = LayoutInflater.from(ctx).inflate(R.layout.fragment_editor, overlay, false)
        EditorBinder(
            ctx, viewModel, editorView, path,
            autoFilterMatrix = viewModel.autoFilterMatrix.value,
            onDiscard = {
                runCatching { File(path).delete() }
                reviewPhotoPath = null
                hideOverlay(R.id.camera_editor_overlay)
            },
            onDone = { result ->
                savePhoto(
                    ctx, File(path), result.matrix,
                    viewModel.selectedPose.value?.title ?: "Pose Frame", viewModel,
                    result.rotationDeg, result.cropAspect
                )
                reviewPhotoPath = null
                hideOverlay(R.id.camera_editor_overlay)
                showSuccess(path)
            }
        ).bind()
        overlay.addView(editorView)
        showOverlay(overlay)
    }

    private fun showSuccess(path: String) {
        savedPhotoPath = path
        val overlay = root.findViewById<android.widget.FrameLayout>(R.id.camera_success_overlay)
        overlay.removeAllViews()
        val v = LayoutInflater.from(ctx).inflate(R.layout.fragment_success, overlay, false)

        // Insets on the banner (status bar) and the action row (nav bar).
        val banner = v.findViewById<View>(R.id.success_banner)
        val actions = v.findViewById<View>(R.id.success_actions)
        val bannerTop = (banner.layoutParams as android.widget.FrameLayout.LayoutParams).topMargin
        val actionsBottom = (actions.layoutParams as android.widget.FrameLayout.LayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (banner.layoutParams as android.widget.FrameLayout.LayoutParams).topMargin = bannerTop + bars.top
            (actions.layoutParams as android.widget.FrameLayout.LayoutParams).bottomMargin = actionsBottom + bars.bottom
            banner.requestLayout(); actions.requestLayout()
            insets
        }

        banner.background = rounded((0xFF2E7D32.toInt() and 0x00FFFFFF) or (235 shl 24), 10f)
        v.findViewById<View>(R.id.success_home).background = glassCircle()
        v.findViewById<View>(R.id.success_share).background = glassCircle()

        ctx.imageLoader.enqueue(
            ImageRequest.Builder(ctx).data(File(path)).crossfade(true)
                .target(v.findViewById<ImageView>(R.id.success_image)).build()
        )

        v.findViewById<View>(R.id.success_home).setOnClickListener {
            savedPhotoPath = null
            hideOverlay(R.id.camera_success_overlay)
            onGoHome()
        }
        v.findViewById<View>(R.id.success_share).setOnClickListener {
            runCatching {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    ctx, "${ctx.packageName}.fileprovider", File(path)
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                ctx.startActivity(Intent.createChooser(intent, ctx.getString(R.string.share_frame)))
            }
        }
        overlay.addView(v)
        ViewCompat.requestApplyInsets(v)
        showOverlay(overlay)
    }

    private fun glassCircle() = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0x80000000.toInt())
        setStroke((1f * d).toInt().coerceAtLeast(1), palette.glass)
    }

    private fun showOverlay(overlay: View) {
        overlay.visibility = View.VISIBLE
        overlay.alpha = 0f
        overlay.translationY = 40f * d
        overlay.animate().alpha(1f).translationY(0f).setDuration(240).start()
    }

    private fun hideOverlay(id: Int) {
        val overlay = root.findViewById<android.widget.FrameLayout>(id)
        overlay.animate().alpha(0f).setDuration(180).withEndAction {
            overlay.visibility = View.GONE
            overlay.removeAllViews()
            overlay.alpha = 1f
            overlay.translationY = 0f
        }.start()
    }

    // ---- Flow observation ----------------------------------------------------------------

    private fun observeFlows() {
        collect { viewModel.selectedPose.collect { onSelectedPoseChanged(it) } }
        collect { viewModel.poseMatchState.collect { onMatchChanged(it.similarityScore) } }
        collect {
            viewModel.poseOpacity.collect { op ->
                root.findViewById<ImageView>(R.id.camera_pose_overlay).alpha = op
                root.findViewById<TextView>(R.id.camera_opacity_value).text = "${(op * 100).toInt()}%"
                setSeek(R.id.camera_opacity_seek, (op * 100).toInt())
            }
        }
        combineTransform()
        collect { viewModel.cameraFlashMode.collect { updateFlash(it) } }
        collect {
            viewModel.cameraGridVisible.collect {
                root.findViewById<View>(R.id.camera_grid).visibility = if (it) View.VISIBLE else View.GONE
                updateProToggles(); updateTuneTint()
            }
        }
        collect { viewModel.cameraTimer.collect { updateProToggles(); updateTuneTint() } }
        collect {
            viewModel.proIso.collect {
                setSeek(R.id.camera_iso_seek, it - 100)
                root.findViewById<TextView>(R.id.camera_iso_value).text = it.toString()
            }
        }
        collect {
            viewModel.proExposure.collect {
                setSeek(R.id.camera_ev_seek, ((it + 2f) * 10f).toInt())
                root.findViewById<TextView>(R.id.camera_ev_value).text = String.format("%.1f", it)
            }
        }
        collect { viewModel.cameraFacing.collect { if (cameraProvider != null) bindCamera() } }
        collect { viewModel.defaultPoses.collect { buildRefStrip(it) } }
    }

    private fun collect(block: suspend () -> Unit) {
        jobs += host.viewLifecycleOwner.lifecycleScope.launch {
            host.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { block() }
        }
    }

    private fun combineTransform() {
        collect {
            viewModel.poseScale.collect { applyOverlayTransform() }
        }
        collect { viewModel.poseOffsetX.collect { applyOverlayTransform() } }
        collect { viewModel.poseOffsetY.collect { applyOverlayTransform() } }
        collect { viewModel.poseRotation.collect { applyOverlayTransform() } }
    }

    private fun applyOverlayTransform() {
        val region = root.findViewById<View>(R.id.camera_preview_region)
        val overlay = root.findViewById<ImageView>(R.id.camera_pose_overlay)
        overlay.scaleX = viewModel.poseScale.value
        overlay.scaleY = viewModel.poseScale.value
        overlay.translationX = viewModel.poseOffsetX.value * region.width
        overlay.translationY = viewModel.poseOffsetY.value * region.height
        overlay.rotation = viewModel.poseRotation.value
    }

    private fun onSelectedPoseChanged(pose: PoseItem?) {
        // Show/hide pose-dependent chrome.
        val hasPose = pose != null
        root.findViewById<View>(R.id.camera_opacity_row).visibility = if (hasPose) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.camera_match_group).visibility = if (hasPose) View.VISIBLE else View.GONE
        updateRefSelection(pose?.id)

        poseImageJob?.cancel()
        val overlay = root.findViewById<ImageView>(R.id.camera_pose_overlay)
        overlay.setImageDrawable(null)
        viewModel.setReferenceLandmarks(emptyMap())
        val img = pose?.image
        if (pose == null || img.isNullOrEmpty()) return

        poseImageJob = host.viewLifecycleOwner.lifecycleScope.launch {
            // Auto look for the post-capture editor.
            val look = withContext(Dispatchers.IO) {
                loadOverlayBitmap(ctx, img)?.let { PhotoFilters.extractLook(it) }
            }
            if (look != null) viewModel.setAutoFilter(look.first, look.second)

            // Background-removed subject overlay (fallback to the raw reference).
            val extracted = withContext(Dispatchers.IO) {
                loadOverlayBitmap(ctx, img, size = 720)?.let { SubjectExtractor.removeBackground(it) }
            }
            if (extracted != null) overlay.setImageBitmap(extracted) else loadRawOverlay(overlay, img)
            overlay.alpha = viewModel.poseOpacity.value
            applyOverlayTransform()

            // Reference landmarks detected from the actual photo.
            val landmarks = withContext(Dispatchers.IO) {
                loadOverlayBitmap(ctx, img, size = 512)?.let { poseProcessor.detectBitmap(it) } ?: emptyMap()
            }
            if (landmarks.isNotEmpty()) viewModel.setReferenceLandmarks(landmarks)
        }
    }

    private fun loadRawOverlay(overlay: ImageView, img: String) {
        val assetPath = assetPathOf(img)
        when {
            assetPath != null -> host.viewLifecycleOwner.lifecycleScope.launch {
                val bmp = withContext(Dispatchers.IO) { decodeAsset(ctx, assetPath) }
                if (bmp != null) overlay.setImageBitmap(bmp)
            }
            img.startsWith("http") || img.contains("/") ->
                ctx.imageLoader.enqueue(
                    ImageRequest.Builder(ctx).data(img).crossfade(true).target(overlay).build()
                )
            else -> {
                val id = ctx.resources.getIdentifier(img, "drawable", ctx.packageName)
                if (id != 0) overlay.setImageResource(id)
            }
        }
    }

    private fun onMatchChanged(score: Int) {
        val group = root.findViewById<View>(R.id.camera_match_group)
        if (group.visibility != View.VISIBLE) { prevScore = score; return }
        val colorGroup = when {
            score >= 80 -> 0xFF2E7D32.toInt()
            score >= 60 -> 0xFFE0A900.toInt()
            else -> 0xFFC62828.toInt()
        }
        val label = when {
            score >= 80 -> "Perfect pose — hold it!"
            score >= 60 -> "Almost there…"
            score >= 30 -> "Keep adjusting your pose"
            score > 0 -> "Match the pose to raise your score"
            else -> "Move into frame to match"
        }
        root.findViewById<TextView>(R.id.camera_match_badge).apply {
            text = "Pose Match $score%"
            background = rounded((colorGroup and 0x00FFFFFF) or (230 shl 24), 20f)
        }
        root.findViewById<TextView>(R.id.camera_match_coach).text = label

        if (score >= 80 && score != prevScore) vibrate(150)
        val great = score >= 85
        if (great && !wasGreat) { runCatching { vibrate(120) }; flashGreat() }
        wasGreat = great
        prevScore = score
    }

    private fun flashGreat() {
        val flash = root.findViewById<View>(R.id.camera_great_flash)
        flash.visibility = View.VISIBLE
        flash.alpha = 1f
        greatFlashJob?.cancel()
        greatFlashJob = host.viewLifecycleOwner.lifecycleScope.launch {
            delay(1300)
            flash.animate().alpha(0f).setDuration(300).withEndAction { flash.visibility = View.GONE }.start()
        }
    }

    private fun vibrate(ms: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(ms)
        }
    }

    // ---- Reference strip -----------------------------------------------------------------

    private fun buildRefStrip(poses: List<PoseItem>) {
        val strip = root.findViewById<LinearLayout>(R.id.camera_ref_strip)
        strip.removeAllViews()
        refItems.clear()
        val inflater = LayoutInflater.from(ctx)
        val gap = (10f * d).toInt()
        poses.forEachIndexed { i, pose ->
            val item = inflater.inflate(R.layout.item_ref_pose, strip, false)
            (item.layoutParams as LinearLayout.LayoutParams).marginStart = if (i > 0) gap else 0
            loadRawOverlay(item.findViewById(R.id.ref_image), pose.image)
            item.setOnClickListener { viewModel.selectPose(pose) }
            strip.addView(item)
            refItems[pose.id] = item
        }
        updateRefSelection(viewModel.selectedPose.value?.id)
    }

    private fun updateRefSelection(selectedId: Int?) {
        for ((id, item) in refItems) {
            val selected = id == selectedId
            item.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f * d
                setColor(0xFF14141A.toInt())
                setStroke(((if (selected) 2f else 1f) * d).toInt().coerceAtLeast(1),
                    if (selected) accent else palette.glass)
            }
            item.clipToOutline = true
            item.findViewById<ImageView>(R.id.ref_check).apply {
                visibility = if (selected) View.VISIBLE else View.GONE
                imageTintList = tint(accent)
            }
        }
    }

    // ---- Small helpers -------------------------------------------------------------------

    private fun updateFlash(mode: String) {
        val iv = root.findViewById<ImageView>(R.id.camera_flash)
        iv.setImageResource(
            when (mode) {
                "On" -> R.drawable.ic_flash_on
                "Auto" -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
        )
        iv.imageTintList = tint(if (mode != "Off") accent else Color.WHITE)
    }

    private fun updateTuneTint() {
        val active = showProPanel || viewModel.cameraGridVisible.value || viewModel.cameraTimer.value > 0
        root.findViewById<ImageView>(R.id.camera_tune).imageTintList = tint(if (active) accent else Color.WHITE)
    }

    private fun updateProToggles() {
        val gridOn = viewModel.cameraGridVisible.value
        styleToggle(R.id.camera_grid_toggle, R.id.camera_grid_icon, R.id.camera_grid_label, gridOn, "Grid")
        val timer = viewModel.cameraTimer.value
        styleToggle(R.id.camera_timer_toggle, R.id.camera_timer_icon, R.id.camera_timer_label,
            timer > 0, if (timer > 0) "Timer ${timer}s" else "Timer")
    }

    private fun styleToggle(rowId: Int, iconId: Int, labelId: Int, on: Boolean, text: String) {
        root.findViewById<View>(rowId).background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * d
            setColor(if (on) (accent and 0x00FFFFFF) or (56 shl 24) else 0xFF1E1E24.toInt())
        }
        root.findViewById<ImageView>(iconId).imageTintList = tint(if (on) accent else 0xFF888888.toInt())
        root.findViewById<TextView>(labelId).apply {
            this.text = text
            setTextColor(if (on) accent else Color.WHITE)
        }
    }

    private fun maybeShowCoach() {
        if (uiPrefs.getBoolean("camera_coach_seen", false)) return
        val coach = root.findViewById<android.widget.FrameLayout>(R.id.camera_coach)
        val cardView = LayoutInflater.from(ctx).inflate(R.layout.view_camera_coach, coach, false)
        cardView.background = rounded(0xFF161619.toInt(), 20f)
        for (cid in intArrayOf(R.id.coach_c1, R.id.coach_c2, R.id.coach_c3)) {
            cardView.findViewById<View>(cid).background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor((accent and 0x00FFFFFF) or (46 shl 24))
            }
        }
        cardView.findViewById<TextView>(R.id.coach_got_it).apply {
            background = rounded(accent, 12f)
            setOnClickListener {
                uiPrefs.edit().putBoolean("camera_coach_seen", true).apply()
                coach.visibility = View.GONE
                coach.removeAllViews()
            }
        }
        coach.addView(cardView)
        coach.visibility = View.VISIBLE
    }

    private fun seek(id: Int, onUser: (Int) -> Unit) {
        root.findViewById<SeekBar>(id).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) { if (fromUser) onUser(progress) }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
    }

    private fun setSeek(id: Int, value: Int) {
        val sb = root.findViewById<SeekBar>(id)
        if (sb.progress != value) sb.progress = value.coerceIn(0, sb.max)
    }

    private fun tint(color: Int) = android.content.res.ColorStateList.valueOf(color)

    private fun rounded(color: Int, radiusDp: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusDp * d
        setColor(color)
    }
}
