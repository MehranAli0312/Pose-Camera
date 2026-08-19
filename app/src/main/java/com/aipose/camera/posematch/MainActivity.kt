package com.aipose.camera.posematch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aipose.camera.posematch.data.AppContainer
import com.aipose.camera.posematch.data.LocaleHelper
import com.aipose.camera.posematch.ui.screens.*
import com.aipose.camera.posematch.ui.theme.MyApplicationTheme
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import com.aipose.camera.posematch.ui.viewmodel.MainViewModelFactory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.tween
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    // Apply the persisted per-app language before any UI is inflated.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Manual central dependency Container (Clean Architecture integration)
        appContainer = AppContainer(applicationContext)

        // 2. Enable physical system bleed edge-to-edge
        enableEdgeToEdge()

        setContent {
            // Live theme selection from persisted preferences.
            val themeName by appContainer.repository.appTheme.collectAsState(initial = "Dark")
            MyApplicationTheme(themeName = themeName) {
                // Initialize Viewmodel with manual custom factory linking repository
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(application, appContainer.repository)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Dark slate aesthetics default
                ) {
                    AppNavigationFlow(mainViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigationFlow(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val localCtx = LocalContext.current

    // Launcher to select a customized image file from local device gallery roll
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                // Copy image file properties to persistent storage cache
                val copiedPath = copyUriToInternalStorage(localCtx, uri)
                if (copiedPath != null) {
                    viewModel.importPoseFromPath("Gallery Reference", copiedPath)
                    Toast.makeText(localCtx, localCtx.getString(R.string.toast_reference_imported), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(localCtx, localCtx.getString(R.string.toast_reference_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun launchGalleryPicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryPickerLauncher.launch(galleryIntent)
    }

    // Compose Native transitions NavHost flow
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(450)) + slideInHorizontally(tween(450)) { it / 6 } },
        exitTransition = { fadeOut(tween(300)) + slideOutHorizontally(tween(450)) { -it / 6 } },
        popEnterTransition = { fadeIn(tween(450)) + slideInHorizontally(tween(450)) { -it / 6 } },
        popExitTransition = { fadeOut(tween(300)) + slideOutHorizontally(tween(450)) { it / 6 } }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(viewModel) { destination ->
                navController.navigate(destination) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(viewModel) { destination ->
                navController.navigate(destination) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            }
        }

        composable(Routes.LANGUAGE) {
            LanguageSelectionScreen(viewModel) { destination ->
                navController.navigate(destination) {
                    popUpTo(Routes.LANGUAGE) { inclusive = true }
                }
            }
        }

        composable(Routes.MAIN_CONTAINER) {
            MainScenicContainer(
                viewModel = viewModel,
                onLaunchGalleryPicker = { launchGalleryPicker() }
            )
        }
    }
}

// Deep copy utility to duplicate Uri binary file to local application sandboxed directory
private fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFilename = "IMPORTED_$timeStamp.png"
        val outputFile = File(context.getExternalFilesDir(null), outputFilename)
        val outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(4096)
        var bytesRead = inputStream.read(buffer)
        while (bytesRead != -1) {
            outputStream.write(buffer, 0, bytesRead)
            bytesRead = inputStream.read(buffer)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
