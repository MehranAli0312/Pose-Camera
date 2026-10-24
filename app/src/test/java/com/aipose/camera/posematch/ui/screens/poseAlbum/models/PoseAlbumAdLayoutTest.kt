package com.aipose.camera.posematch.ui.screens.poseAlbum.models

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class PoseAlbumAdLayoutTest {

    @Test
    fun noAdWhenFewerThanThreeRows() {
        assertEquals(PoseAlbumAdLayout.None, PoseAlbumAdLayout.of(columns = 2, poseCount = 5))
    }

    @Test
    fun firstAdAfterTwoRowsWhenARowFollows() {
        assertEquals(
            PoseAlbumAdLayout(firstAdIndex = 4, secondAdIndex = null),
            PoseAlbumAdLayout.of(columns = 2, poseCount = 6),
        )
    }

    @Test
    fun secondAdAfterTenRowsWhenARowFollows() {
        assertEquals(
            PoseAlbumAdLayout(firstAdIndex = 4, secondAdIndex = 20),
            PoseAlbumAdLayout.of(columns = 2, poseCount = 22),
        )
        assertEquals(
            PoseAlbumAdLayout(firstAdIndex = 4, secondAdIndex = null),
            PoseAlbumAdLayout.of(columns = 2, poseCount = 21),
        )
    }

    @Test
    fun adIndicesFollowColumnCount() {
        assertEquals(
            PoseAlbumAdLayout(firstAdIndex = 6, secondAdIndex = 30),
            PoseAlbumAdLayout.of(columns = 3, poseCount = 40),
        )
    }

    @Test
    fun columnCountMatchesAdaptiveCells() {
        assertEquals(2, adaptiveColumnCount(336.dp, 150.dp, 20.dp))
        assertEquals(1, adaptiveColumnCount(300.dp, 150.dp, 20.dp))
        assertEquals(3, adaptiveColumnCount(500.dp, 150.dp, 20.dp))
    }
}
