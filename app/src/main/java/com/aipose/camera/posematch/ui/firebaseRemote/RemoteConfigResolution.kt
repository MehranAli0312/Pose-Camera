package com.aipose.camera.posematch.ui.firebaseRemote

enum class RemoteConfigResolution {

    Pending,

    DefaultsOrPreviouslyActivatedApplied,

    FetchResolved,

    FetchFailed,
    ;

    val isTerminal: Boolean
        get() = this == FetchResolved || this == FetchFailed

    fun isEnoughToDecide(hasEverActivated: Boolean): Boolean =
        if (hasEverActivated) this != Pending else isTerminal
}
