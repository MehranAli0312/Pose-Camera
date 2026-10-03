package com.example.common.update

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateManager(
    private val activity: ComponentActivity,
) : DefaultLifecycleObserver {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(activity.applicationContext) }

    private var forceUpdate = false
    private var flowStarted = false

    private val updateLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::onUpdateFlowResult,
    )

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateManager.completeUpdate()
        }
    }

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        appUpdateManager.registerListener(installStateListener)
    }

    fun checkForUpdate(requiredVersionCode: Long, forceUpdate: Boolean) {
        if (flowStarted || currentVersionCode() >= requiredVersionCode) return
        this.forceUpdate = forceUpdate
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val updateType = updateType()
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(updateType)
            ) {
                startUpdateFlow(info, updateType)
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        if (!flowStarted) return
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> appUpdateManager.completeUpdate()
                forceUpdate && info.updateAvailability() ==
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                    startUpdateFlow(info, AppUpdateType.IMMEDIATE)
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        appUpdateManager.unregisterListener(installStateListener)
        owner.lifecycle.removeObserver(this)
    }

    private fun startUpdateFlow(info: AppUpdateInfo, @AppUpdateType updateType: Int) {
        flowStarted = true
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(updateType).build(),
            )
        }.onFailure { flowStarted = false }
    }

    private fun onUpdateFlowResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) return
        if (forceUpdate) {
            activity.finishAffinity()
        } else {
            flowStarted = false
        }
    }

    @AppUpdateType
    private fun updateType(): Int =
        if (forceUpdate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE

    private fun currentVersionCode(): Long {
        val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
        return PackageInfoCompat.getLongVersionCode(packageInfo)
    }
}
