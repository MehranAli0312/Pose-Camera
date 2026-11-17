package com.aipose.camera.posematch.ui.screens.launch.models

import org.junit.Assert.assertEquals
import org.junit.Test

class LaunchFlowTest {

    @Test
    fun encodedRemainingFlowRoundTrips() {
        val flow = LaunchFlow(
            session = LaunchSession.First,
            steps = listOf(LaunchStep.Language, LaunchStep.Onboarding, LaunchStep.Home),
        )

        assertEquals(flow, LaunchFlow.decode(flow.encode()))
        assertEquals(
            flow.copy(steps = listOf(LaunchStep.Onboarding, LaunchStep.Home)),
            flow.remaining(),
        )
    }
}
