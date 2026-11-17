package com.aipose.camera.posematch.ui.screens.launch.models

data class LaunchFlow(
    val session: LaunchSession,
    val steps: List<LaunchStep>,
) {

    fun remaining(): LaunchFlow = copy(steps = steps.drop(1))

    fun encode(): String = buildString {
        append(session.name)
        append(SEPARATOR)
        append(steps.joinToString(STEP_SEPARATOR) { step -> step.name })
    }

    companion object {
        private const val SEPARATOR = ":"
        private const val STEP_SEPARATOR = ","

        fun decode(value: String?): LaunchFlow? {
            val parts = value?.split(SEPARATOR, limit = 2) ?: return null
            if (parts.size != 2) return null
            val session = LaunchSession.entries.firstOrNull { it.name == parts[0] } ?: return null
            val steps = parts[1]
                .split(STEP_SEPARATOR)
                .filter(String::isNotBlank)
                .mapNotNull { name -> LaunchStep.entries.firstOrNull { it.name == name } }
            return LaunchFlow(session, steps.ifEmpty { listOf(LaunchStep.Home) })
        }
    }
}
