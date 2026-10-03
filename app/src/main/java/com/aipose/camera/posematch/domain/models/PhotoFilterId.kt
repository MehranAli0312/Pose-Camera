package com.aipose.camera.posematch.domain.models

enum class PhotoFilterId(val key: String) {
    Auto("auto"),
    Original("original"),
    Vivid("vivid"),
    Golden("golden"),
    Sunrise("sunrise"),
    Sunset("sunset"),
    Azure("teal"),
    Cinema("cinematic"),
    Fade("fade"),
    Noir("noir"),
    Vintage("vintage"),
    Mono("mono");

    companion object {
        private val byKey = entries.associateBy { it.key }

        fun fromKey(key: String): PhotoFilterId = byKey[key] ?: Original
    }
}
