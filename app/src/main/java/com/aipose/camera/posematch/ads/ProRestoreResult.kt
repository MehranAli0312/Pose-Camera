package com.aipose.camera.posematch.ads

sealed interface ProRestoreResult {

    data object Restored : ProRestoreResult

    data object NothingFound : ProRestoreResult

    data object Failed : ProRestoreResult
}
