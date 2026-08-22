package com.aipose.camera.posematch.ui.fragments

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aipose.camera.posematch.MainActivity
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.NavTab
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main container — the old MainScenicContainer converted to a real-XML host (migration step 5).
 *
 * Owns the 3-tab bottom nav, the pushed full-screen camera, the camera-permission card (with its
 * bounce + Allow flow) and the exit bottom sheet. Home/Collections/Camera are still hosted as
 * themed ComposeViews inside this XML shell; Settings is real XML (see [SettingsBinder]). Behaviour
 * matches the original: back from a sub-tab returns to Home, back from Home asks to exit, opening
 * the camera without permission only bounces the card.
 */
class MainFragment : Fragment(R.layout.fragment_main) {

    private val viewModel: MainViewModel by activityViewModels { (requireActivity() as MainActivity).viewModelFactory() }

    private var activeTab = NavTab.HOME
    private var showCamera = false
    private var cameraGranted = false
    private var askedOnce = false
    private var settingsBinder: SettingsBinder? = null
    private var collectionsBinder: CollectionsBinder? = null
    private var homeBinder: HomeBinder? = null
    private var cameraBinder: CameraBinder? = null
    private var contentBackHandler: (() -> Boolean)? = null

    private val locationPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* optional; capture works regardless */ }

    private var accent = 0
    private var d = 1f

    private val camPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            cameraGranted = granted
            askedOnce = true
            updatePermissionCard()
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    val path = copyUriToInternalStorage(requireContext(), uri)
                    if (path != null) {
                        viewModel.importPoseFromPath("Gallery Reference", path)
                        Toast.makeText(requireContext(), getString(R.string.toast_reference_imported), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.toast_reference_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    fun launchGalleryPicker() {
        galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        d = resources.displayMetrics.density
        cameraGranted = isCamGranted()

        // Nav-bar bottom inset (windowInsetsPadding(navigationBars) in the original).
        val bottomNav = view.findViewById<View>(R.id.main_bottom_nav)
        val baseNavBottom = bottomNav.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomNav.updatePadding(bottom = baseNavBottom + bars.bottom)
            insets
        }

        view.findViewById<View>(R.id.nav_home).setOnClickListener { selectTab(NavTab.HOME) }
        view.findViewById<View>(R.id.nav_collections).setOnClickListener { selectTab(NavTab.HISTORY) }
        view.findViewById<View>(R.id.nav_settings).setOnClickListener { selectTab(NavTab.SETTINGS) }
        view.findViewById<View>(R.id.perm_allow).setOnClickListener { onAllowClick() }

        stylePermissionCard(view)
        updatePermissionCard()

        // Back handling: camera -> tabs; sub-tab -> Home; Home -> exit sheet.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when {
                showCamera -> { if (cameraBinder?.onBack() != true) closeCamera() }
                contentBackHandler?.invoke() == true -> {}
                activeTab != NavTab.HOME -> selectTab(NavTab.HOME)
                else -> showExitSheet()
            }
        }

        // Live theme: re-grade the XML chrome when the palette changes (ComposeViews self-theme).
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appTheme.collect { applyPalette() }
            }
        }

        selectTab(NavTab.HOME)
    }

    override fun onResume() {
        super.onResume()
        // Re-check when returning to the app (e.g. back from system Settings) so the card hides.
        cameraGranted = isCamGranted()
        updatePermissionCard()
    }

    override fun onDestroyView() {
        settingsBinder?.unbind()
        settingsBinder = null
        collectionsBinder?.unbind()
        collectionsBinder = null
        homeBinder?.unbind()
        homeBinder = null
        cameraBinder?.unbind()
        cameraBinder = null
        contentBackHandler = null
        super.onDestroyView()
    }

    // ---- Palette / nav styling -----------------------------------------------------------

    private fun applyPalette() {
        val palette = paletteFor(viewModel.appTheme.value)
        accent = palette.accent
        view?.findViewById<View>(R.id.main_tabs_root)?.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )
        updateNavStyles()
    }

    private fun updateNavStyles() {
        val gray = 0xFF888888.toInt()
        val accent15 = (accent and 0x00FFFFFF) or (38 shl 24)
        data class Item(val root: Int, val pill: Int, val icon: Int, val label: Int, val tab: NavTab)
        val items = listOf(
            Item(R.id.nav_home, R.id.nav_home_pill, R.id.nav_home_icon, R.id.nav_home_label, NavTab.HOME),
            Item(R.id.nav_collections, R.id.nav_collections_pill, R.id.nav_collections_icon, R.id.nav_collections_label, NavTab.HISTORY),
            Item(R.id.nav_settings, R.id.nav_settings_pill, R.id.nav_settings_icon, R.id.nav_settings_label, NavTab.SETTINGS)
        )
        val v = view ?: return
        for (it in items) {
            val selected = it.tab == activeTab
            val pill = v.findViewById<View>(it.pill)
            pill.background = if (selected) GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f * d
                setColor(accent15)
            } else null
            v.findViewById<ImageView>(it.icon).imageTintList =
                android.content.res.ColorStateList.valueOf(if (selected) accent else gray)
            v.findViewById<TextView>(it.label).apply {
                setTextColor(if (selected) accent else gray)
                setTypeface(null, if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            }
        }
    }

    // ---- Tab content ---------------------------------------------------------------------

    private fun selectTab(tab: NavTab) {
        // Tear down any per-tab bindings before swapping content.
        settingsBinder?.unbind(); settingsBinder = null
        collectionsBinder?.unbind(); collectionsBinder = null
        homeBinder?.unbind(); homeBinder = null
        contentBackHandler = null
        activeTab = tab
        applyPalette()

        val content = view?.findViewById<FrameLayout>(R.id.main_content) ?: return
        content.removeAllViews()
        val child: View = when (tab) {
            NavTab.HOME -> {
                val v = layoutInflater.inflate(R.layout.fragment_home, content, false)
                val binder = HomeBinder(
                    this, viewModel, v,
                    onOpenCamera = { tryOpenCamera() },
                    onLaunchGalleryPicker = { launchGalleryPicker() }
                ).also { it.bind() }
                homeBinder = binder
                contentBackHandler = { binder.onBack() }
                v
            }
            NavTab.HISTORY -> {
                val v = layoutInflater.inflate(R.layout.fragment_collections, content, false)
                val binder = CollectionsBinder(this, viewModel, v).also { it.bind() }
                collectionsBinder = binder
                contentBackHandler = { binder.onBack() }
                v
            }
            NavTab.SETTINGS -> {
                val v = layoutInflater.inflate(R.layout.fragment_settings, content, false)
                settingsBinder = SettingsBinder(this, viewModel, v).also { it.bind() }
                v
            }
        }
        content.addView(child)
        // Gentle fade-in (the original crossfaded between tabs).
        child.alpha = 0f
        child.animate().alpha(1f).setDuration(220).start()
    }

    // ---- Camera push ---------------------------------------------------------------------

    private fun tryOpenCamera() {
        if (cameraGranted) openCamera() else bounceCard()
    }

    private fun openCamera() {
        if (showCamera) return
        showCamera = true
        // Location is optional (used only to tag captures); request once when entering the camera.
        val hasLoc = ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasLoc) {
            locationPermLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        val container = view?.findViewById<FrameLayout>(R.id.main_camera_container) ?: return
        container.removeAllViews()
        val v = layoutInflater.inflate(R.layout.fragment_camera, container, false)
        cameraBinder = CameraBinder(
            this, viewModel, v,
            onExit = { closeCamera() },
            onGoHome = { activeTab = NavTab.HOME; selectTab(NavTab.HOME); closeCamera() }
        ).also { it.bind() }
        container.addView(v)
        val width = (view?.width ?: resources.displayMetrics.widthPixels).toFloat()
        container.visibility = View.VISIBLE
        container.translationX = width
        container.alpha = 0f
        container.animate().translationX(0f).alpha(1f).setDuration(300).start()
    }

    private fun closeCamera() {
        if (!showCamera) return
        showCamera = false
        cameraBinder?.unbind(); cameraBinder = null
        val container = view?.findViewById<FrameLayout>(R.id.main_camera_container) ?: return
        val width = (view?.width ?: resources.displayMetrics.widthPixels).toFloat()
        container.animate().translationX(width).alpha(0f).setDuration(300).withEndAction {
            container.visibility = View.GONE
            container.removeAllViews()
            container.translationX = 0f
        }.start()
    }

    // ---- Camera permission ---------------------------------------------------------------

    private fun isCamGranted() = ContextCompat.checkSelfPermission(
        requireContext(), android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun canPromptCamera(): Boolean {
        val activity = activity ?: return !askedOnce
        return !askedOnce || ActivityCompat.shouldShowRequestPermissionRationale(
            activity, android.Manifest.permission.CAMERA
        )
    }

    private fun onAllowClick() {
        when {
            cameraGranted -> {}
            canPromptCamera() -> camPermLauncher.launch(android.Manifest.permission.CAMERA)
            else -> runCatching {
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireContext().packageName, null)
                    )
                )
            }
        }
    }

    private fun updatePermissionCard() {
        val card = view?.findViewById<View>(R.id.main_permission_card) ?: return
        val shouldShow = !cameraGranted
        if (shouldShow && card.visibility != View.VISIBLE) {
            card.alpha = 0f
            card.visibility = View.VISIBLE
            card.animate().alpha(1f).setDuration(200).start()
        } else if (!shouldShow && card.visibility == View.VISIBLE) {
            card.animate().alpha(0f).setDuration(160).withEndAction { card.visibility = View.GONE }.start()
        }
    }

    private fun bounceCard() {
        val card = view?.findViewById<View>(R.id.main_permission_card) ?: return
        // 1 -> 1.07 -> 0.96 -> 1, played twice (matches the Compose repeat(2)).
        val sx = PropertyValuesHolder.ofKeyframe(
            View.SCALE_X,
            Keyframe.ofFloat(0f, 1f), Keyframe.ofFloat(0.349f, 1.07f),
            Keyframe.ofFloat(0.698f, 0.96f), Keyframe.ofFloat(1f, 1f)
        )
        val sy = PropertyValuesHolder.ofKeyframe(
            View.SCALE_Y,
            Keyframe.ofFloat(0f, 1f), Keyframe.ofFloat(0.349f, 1.07f),
            Keyframe.ofFloat(0.698f, 0.96f), Keyframe.ofFloat(1f, 1f)
        )
        ObjectAnimator.ofPropertyValuesHolder(card, sx, sy).apply {
            duration = 430
            repeatCount = 1
        }.start()
    }

    private fun stylePermissionCard(view: View) {
        val warn = 0xFFED8B4E.toInt()
        val card = view.findViewById<View>(R.id.main_permission_card)
        card.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(0xFF2A1A12.toInt(), 0xFF1B1B21.toInt())
        ).apply {
            cornerRadius = 16f * d
            setStroke((1f * d).toInt().coerceAtLeast(1), (warn and 0x00FFFFFF) or (128 shl 24))
        }
        view.findViewById<View>(R.id.perm_icon_circle).background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor((warn and 0x00FFFFFF) or (46 shl 24))
        }
        (view.findViewById<FrameLayout>(R.id.perm_icon_circle).getChildAt(0) as? ImageView)
            ?.imageTintList = android.content.res.ColorStateList.valueOf(warn)
        view.findViewById<TextView>(R.id.perm_allow).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10f * d
                setColor(warn)
            }
        }
    }

    // ---- Exit sheet ----------------------------------------------------------------------

    private fun showExitSheet() {
        val ctx = requireContext()
        val palette = paletteFor(viewModel.appTheme.value)
        val accentC = palette.accent
        val glassC = palette.glass
        val content = layoutInflater.inflate(R.layout.view_exit_sheet, null) as LinearLayout
        content.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(20f * d, 20f * d, 20f * d, 20f * d, 0f, 0f, 0f, 0f)
            setColor(0xFF161619.toInt())
        }
        content.findViewById<View>(R.id.exit_drag_handle).background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 2f * d
            setColor(0xFF555555.toInt())
        }
        content.findViewById<View>(R.id.exit_icon_circle).background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor((accentC and 0x00FFFFFF) or (38 shl 24))
        }
        content.findViewById<ImageView>(R.id.exit_icon).imageTintList =
            android.content.res.ColorStateList.valueOf(accentC)

        val dialog = Dialog(ctx).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(content)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.BOTTOM)
                setDimAmount(0.5f)
            }
        }
        content.findViewById<TextView>(R.id.exit_cancel).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14f * d
                setColor(Color.TRANSPARENT)
                setStroke((1f * d).toInt().coerceAtLeast(1), glassC)
            }
            setOnClickListener { dialog.dismiss() }
        }
        content.findViewById<TextView>(R.id.exit_confirm).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14f * d
                setColor(accentC)
            }
            setOnClickListener {
                dialog.dismiss()
                (activity as? Activity)?.finish()
            }
        }
        dialog.show()
        // Slide the sheet up on show.
        content.post {
            content.translationY = content.height.toFloat()
            content.animate().translationY(0f).setDuration(250).start()
        }
    }
}

// Copies a picked gallery Uri into the app's external-files dir; returns the absolute path.
private fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(context.getExternalFilesDir(null), "IMPORTED_$timeStamp.png")
        FileOutputStream(outputFile).use { out ->
            val buffer = ByteArray(4096)
            var read = inputStream.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = inputStream.read(buffer)
            }
        }
        inputStream.close()
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
