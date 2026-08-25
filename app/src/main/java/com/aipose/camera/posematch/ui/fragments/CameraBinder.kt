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
import com.aipose.camera.posematch.databinding.FragmentCameraBinding
import com.aipose.camera.posematch.databinding.FragmentSuccessBinding
import com.aipose.camera.posematch.databinding.ItemRefPoseBinding
import com.aipose.camera.posematch.databinding.ViewCameraCoachBinding
import com.aipose.camera.posematch.domain.PhotoFilters
import com.aipose.camera.posematch.domain.PoseDetectorProcessor
import com.aipose.camera.posematch.domain.SubjectExtractor
import com.aipose.camera.posematch.ui.screens.assetPathOf
import com.aipose.camera.posematch.ui.screens.decodeAsset
import com.aipose.camera.posematch.ui.screens.loadOverlayBitmap
import com.aipose.camera.posematch.ui.screens.savePhoto
import com.aipose.camera.posematch.ui.screens.takePhotoToFile
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.util.SELECTION_BLUE
import com.aipose.camera.posematch.ui.util.applySystemBarInsets
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import com.aipose.camera.posematch.ui.widget.TransformGestureDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
class CameraBinder(
    private val host: Fragment,
    private val viewModel: MainViewModel,
    private val root: View,
    private val onExit: () -> Unit,
    private val onGoHome: () -> Unit
) {
    private val ctx get() = host.requireContext()
    private val binding = FragmentCameraBinding.bind(root)
    private val d = root.resources.displayMetrics.density
    private val palette = paletteFor(root.context)
    private val accent = palette.accent
    private val card = palette.card

    private val previewView = binding.cameraPreviewView
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

    private val refItems = HashMap<Int, ItemRefPoseBinding>()

    private val uiPrefs = ctx.getSharedPreferences("posematch_ui", Context.MODE_PRIVATE)

    private val transform = TransformGestureDetector { panX, panY, zoom, rotation ->
        val region = binding.cameraPreviewRegion
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
        // Insets: status bar on the top bar, nav bar on the shutter dock.
        binding.cameraTopbar.applySystemBarInsets(top = true)
        binding.cameraDock.applySystemBarInsets(bottom = true)

        styleChrome()
        wireControls()

        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        @Suppress("ClickableViewAccessibility")
        binding.cameraPreviewRegion.setOnTouchListener { _, e -> transform.onTouchEvent(e) }

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
        savedPhotoPath != null -> { savedPhotoPath = null; hideOverlay(binding.cameraSuccessOverlay); onGoHome(); true }
        reviewPhotoPath != null -> {
            reviewPhotoPath?.let { runCatching { File(it).delete() } }
            reviewPhotoPath = null; hideOverlay(binding.cameraEditorOverlay); true
        }
        else -> false
    }

    // ---- Static chrome styling -----------------------------------------------------------

    private fun styleChrome() {
        // Shutter: white outer ring + white inner disc with a black rim.
        binding.cameraShutter.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(Color.WHITE)
        }
        binding.cameraShutterInner.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.WHITE)
            setStroke((3f * d).toInt(), Color.BLACK)
        }
        binding.cameraFlip.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(card)
        }
        binding.cameraProPanel.setBackgroundColor(card)
        binding.cameraMatchCoach.background = rounded(0x8C000000.toInt(), 14f)
        binding.cameraCountdown.setTextColor(accent)
        binding.cameraGreatFlash.background =
            rounded((0xFF2E7D32.toInt() and 0x00FFFFFF) or (235 shl 24), 24f)
    }

    private fun wireControls() {
        binding.cameraBack.setOnClickListener { onExit() }
        binding.cameraFlash.setOnClickListener { viewModel.toggleFlash() }
        binding.cameraTune.setOnClickListener {
            showProPanel = !showProPanel
            binding.cameraProPanel.visibility = if (showProPanel) View.VISIBLE else View.GONE
            updateTuneTint()
        }
        binding.cameraShutter.setOnClickListener { triggerCapture() }
        binding.cameraFlip.setOnClickListener { viewModel.toggleCameraFacing() }
        binding.cameraGridToggle.setOnClickListener { viewModel.toggleGridVisible() }
        binding.cameraTimerToggle.setOnClickListener {
            viewModel.setTimer(when (viewModel.cameraTimer.value) { 0 -> 3; 3 -> 5; else -> 0 })
        }

        seek(binding.cameraOpacitySeek) { viewModel.updatePoseOpacity(it / 100f) }
        seek(binding.cameraIsoSeek) { viewModel.setProIso(100 + it) }
        seek(binding.cameraEvSeek) { viewModel.setProExposure(it / 10f - 2f) }
        for (sb in listOf(binding.cameraOpacitySeek, binding.cameraIsoSeek, binding.cameraEvSeek)) {
            sb.progressTintList = tint(accent); sb.thumbTintList = tint(accent)
        }
    }

    // ---- Camera ---------------------------------------------------------------------------

    private fun startCameraIfPermitted() {
        val granted = ContextCompat.checkSelfPermission(
            ctx, android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) {
            binding.cameraSim.visibility = View.VISIBLE
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
        val tv = binding.cameraCountdown
        if (activeTimerCountdown > 0) { tv.text = activeTimerCountdown.toString(); tv.visibility = View.VISIBLE }
        else tv.visibility = View.GONE
    }

    // ---- Editor / success overlays -------------------------------------------------------

    private fun showEditor(path: String) {
        reviewPhotoPath = path
        val overlay = binding.cameraEditorOverlay
        overlay.removeAllViews()
        val editorView = LayoutInflater.from(ctx).inflate(R.layout.fragment_editor, overlay, false)
        EditorBinder(
            ctx, viewModel, editorView, path,
            autoFilterMatrix = viewModel.autoFilterMatrix.value,
            onDiscard = {
                runCatching { File(path).delete() }
                reviewPhotoPath = null
                hideOverlay(binding.cameraEditorOverlay)
            },
            onDone = { result ->
                savePhoto(
                    ctx, File(path), result.matrix,
                    viewModel.selectedPose.value?.title ?: "Pose Frame", viewModel,
                    result.rotationDeg, result.cropAspect
                )
                reviewPhotoPath = null
                hideOverlay(binding.cameraEditorOverlay)
                showSuccess(path)
            }
        ).bind()
        overlay.addView(editorView)
        showOverlay(overlay)
    }

    private fun showSuccess(path: String) {
        savedPhotoPath = path
        val overlay = binding.cameraSuccessOverlay
        overlay.removeAllViews()
        val s = FragmentSuccessBinding.inflate(LayoutInflater.from(ctx), overlay, false)

        // Insets on the banner (status bar) and the action row (nav bar).
        val bannerTop = (s.successBanner.layoutParams as android.widget.FrameLayout.LayoutParams).topMargin
        val actionsBottom = (s.successActions.layoutParams as android.widget.FrameLayout.LayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(s.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (s.successBanner.layoutParams as android.widget.FrameLayout.LayoutParams).topMargin = bannerTop + bars.top
            (s.successActions.layoutParams as android.widget.FrameLayout.LayoutParams).bottomMargin = actionsBottom + bars.bottom
            s.successBanner.requestLayout(); s.successActions.requestLayout()
            insets
        }

        s.successBanner.background = rounded((0xFF2E7D32.toInt() and 0x00FFFFFF) or (235 shl 24), 10f)
        s.successHome.background = glassCircle()
        s.successShare.background = glassCircle()

        ctx.imageLoader.enqueue(
            ImageRequest.Builder(ctx).data(File(path)).crossfade(true).target(s.successImage).build()
        )

        s.successHome.setOnClickListener {
            savedPhotoPath = null
            hideOverlay(binding.cameraSuccessOverlay)
            onGoHome()
        }
        s.successShare.setOnClickListener {
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
        overlay.addView(s.root)
        ViewCompat.requestApplyInsets(s.root)
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

    private fun hideOverlay(overlay: android.widget.FrameLayout) {
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
                binding.cameraPoseOverlay.alpha = op
                binding.cameraOpacityValue.text = "${(op * 100).toInt()}%"
                setSeek(binding.cameraOpacitySeek, (op * 100).toInt())
            }
        }
        combineTransform()
        collect { viewModel.cameraFlashMode.collect { updateFlash(it) } }
        collect {
            viewModel.cameraGridVisible.collect {
                binding.cameraGrid.visibility = if (it) View.VISIBLE else View.GONE
                updateProToggles(); updateTuneTint()
            }
        }
        collect { viewModel.cameraTimer.collect { updateProToggles(); updateTuneTint() } }
        collect {
            viewModel.proIso.collect {
                setSeek(binding.cameraIsoSeek, it - 100)
                binding.cameraIsoValue.text = it.toString()
            }
        }
        collect {
            viewModel.proExposure.collect {
                setSeek(binding.cameraEvSeek, ((it + 2f) * 10f).toInt())
                binding.cameraEvValue.text = String.format("%.1f", it)
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
        val region = binding.cameraPreviewRegion
        val overlay = binding.cameraPoseOverlay
        overlay.scaleX = viewModel.poseScale.value
        overlay.scaleY = viewModel.poseScale.value
        overlay.translationX = viewModel.poseOffsetX.value * region.width
        overlay.translationY = viewModel.poseOffsetY.value * region.height
        overlay.rotation = viewModel.poseRotation.value
    }

    private fun onSelectedPoseChanged(pose: PoseItem?) {
        // Show/hide pose-dependent chrome.
        val hasPose = pose != null
        binding.cameraOpacityRow.visibility = if (hasPose) View.VISIBLE else View.GONE
        binding.cameraMatchGroup.visibility = if (hasPose) View.VISIBLE else View.GONE
        updateRefSelection(pose?.id)

        poseImageJob?.cancel()
        val overlay = binding.cameraPoseOverlay
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
        val group = binding.cameraMatchGroup
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
        binding.cameraMatchBadge.apply {
            text = "Pose Match $score%"
            background = rounded((colorGroup and 0x00FFFFFF) or (230 shl 24), 20f)
        }
        binding.cameraMatchCoach.text = label

        if (score >= 80 && score != prevScore) vibrate(150)
        val great = score >= 85
        if (great && !wasGreat) { runCatching { vibrate(120) }; flashGreat() }
        wasGreat = great
        prevScore = score
    }

    private fun flashGreat() {
        val flash = binding.cameraGreatFlash
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
        val strip = binding.cameraRefStrip
        strip.removeAllViews()
        refItems.clear()
        val inflater = LayoutInflater.from(ctx)
        val gap = (10f * d).toInt()
        poses.forEachIndexed { i, pose ->
            val itemB = ItemRefPoseBinding.inflate(inflater, strip, false)
            (itemB.root.layoutParams as LinearLayout.LayoutParams).marginStart = if (i > 0) gap else 0
            loadRawOverlay(itemB.refImage, pose.image)
            itemB.root.setOnClickListener { viewModel.selectPose(pose) }
            strip.addView(itemB.root)
            refItems[pose.id] = itemB
        }
        updateRefSelection(viewModel.selectedPose.value?.id)
    }

    private fun updateRefSelection(selectedId: Int?) {
        for ((id, itemB) in refItems) {
            val selected = id == selectedId
            // Dark fill behind the thumbnail image.
            itemB.root.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f * d
                setColor(0xFF14141A.toInt())
            }
            itemB.root.clipToOutline = true
            // 1dp blue selection stroke drawn OVER the image (foreground), glass otherwise.
            itemB.root.foreground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f * d
                setColor(Color.TRANSPARENT)
                setStroke((1f * d).toInt().coerceAtLeast(1), if (selected) SELECTION_BLUE else palette.glass)
            }
            itemB.refCheck.apply {
                visibility = if (selected) View.VISIBLE else View.GONE
                imageTintList = tint(SELECTION_BLUE)
            }
        }
    }

    // ---- Small helpers -------------------------------------------------------------------

    private fun updateFlash(mode: String) {
        val iv = binding.cameraFlash
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
        binding.cameraTune.imageTintList = tint(if (active) accent else Color.WHITE)
    }

    private fun updateProToggles() {
        val gridOn = viewModel.cameraGridVisible.value
        styleToggle(binding.cameraGridToggle, binding.cameraGridIcon, binding.cameraGridLabel, gridOn, "Grid")
        val timer = viewModel.cameraTimer.value
        styleToggle(binding.cameraTimerToggle, binding.cameraTimerIcon, binding.cameraTimerLabel,
            timer > 0, if (timer > 0) "Timer ${timer}s" else "Timer")
    }

    private fun styleToggle(row: View, icon: ImageView, label: TextView, on: Boolean, text: String) {
        row.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * d
            setColor(if (on) (accent and 0x00FFFFFF) or (56 shl 24) else 0xFF1E1E24.toInt())
        }
        icon.imageTintList = tint(if (on) accent else 0xFF888888.toInt())
        label.apply {
            this.text = text
            setTextColor(if (on) accent else Color.WHITE)
        }
    }

    private fun maybeShowCoach() {
        if (uiPrefs.getBoolean("camera_coach_seen", false)) return
        val coach = binding.cameraCoach
        val coachB = ViewCameraCoachBinding.inflate(LayoutInflater.from(ctx), coach, false)
        coachB.root.background = rounded(0xFF161619.toInt(), 20f)
        for (circle in listOf(coachB.coachC1, coachB.coachC2, coachB.coachC3)) {
            circle.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor((accent and 0x00FFFFFF) or (46 shl 24))
            }
        }
        coachB.coachGotIt.apply {
            background = rounded(accent, 12f)
            setOnClickListener {
                uiPrefs.edit().putBoolean("camera_coach_seen", true).apply()
                coach.visibility = View.GONE
                coach.removeAllViews()
            }
        }
        coach.addView(coachB.root)
        coach.visibility = View.VISIBLE
    }

    private fun seek(sb: SeekBar, onUser: (Int) -> Unit) {
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) { if (fromUser) onUser(progress) }
            override fun onStartTrackingTouch(bar: SeekBar) {}
            override fun onStopTrackingTouch(bar: SeekBar) {}
        })
    }

    private fun setSeek(sb: SeekBar, value: Int) {
        if (sb.progress != value) sb.progress = value.coerceIn(0, sb.max)
    }

    private fun tint(color: Int) = android.content.res.ColorStateList.valueOf(color)

    private fun rounded(color: Int, radiusDp: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusDp * d
        setColor(color)
    }
}
