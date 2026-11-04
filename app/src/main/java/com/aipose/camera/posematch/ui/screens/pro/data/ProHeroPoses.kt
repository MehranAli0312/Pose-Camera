package com.aipose.camera.posematch.ui.screens.pro.data

import androidx.compose.ui.geometry.Offset
import com.aipose.camera.posematch.ui.screens.pro.models.ProHeroPose
import com.aipose.camera.posematch.ui.screens.pro.models.ProPoseSkeleton

private const val POSE_ASSETS = "file:///android_asset/poses/"

private const val LEFT_SHOULDER = 0
private const val RIGHT_SHOULDER = 1
private const val LEFT_ELBOW = 2
private const val LEFT_WRIST = 3
private const val RIGHT_ELBOW = 4
private const val RIGHT_WRIST = 5
private const val LEFT_HIP = 6
private const val RIGHT_HIP = 7
private const val LEFT_KNEE = 8
private const val RIGHT_KNEE = 9
private const val LEFT_ANKLE = 10
private const val RIGHT_ANKLE = 11

private val standingPoseSkeleton = ProPoseSkeleton(
    joints = listOf(
        Offset(0.39f, 0.19f),
        Offset(0.67f, 0.19f),
        Offset(0.37f, 0.33f),
        Offset(0.49f, 0.36f),
        Offset(0.71f, 0.33f),
        Offset(0.655f, 0.44f),
        Offset(0.455f, 0.5f),
        Offset(0.625f, 0.5f),
        Offset(0.475f, 0.69f),
        Offset(0.565f, 0.69f),
        Offset(0.48f, 0.88f),
        Offset(0.545f, 0.9f),
    ),
    bones = listOf(
        LEFT_SHOULDER to RIGHT_SHOULDER,
        LEFT_SHOULDER to LEFT_ELBOW,
        LEFT_ELBOW to LEFT_WRIST,
        RIGHT_SHOULDER to RIGHT_ELBOW,
        RIGHT_ELBOW to RIGHT_WRIST,
        LEFT_SHOULDER to LEFT_HIP,
        RIGHT_SHOULDER to RIGHT_HIP,
        LEFT_HIP to RIGHT_HIP,
        LEFT_HIP to LEFT_KNEE,
        LEFT_KNEE to LEFT_ANKLE,
        RIGHT_HIP to RIGHT_KNEE,
        RIGHT_KNEE to RIGHT_ANKLE,
    ),
)

val proHeroStartPose = ProHeroPose(imagePath = POSE_ASSETS + "cafe_1.webp")

val proHeroCenterPose = ProHeroPose(
    imagePath = POSE_ASSETS + "viral_2.webp",
    skeleton = standingPoseSkeleton,
)

val proHeroEndPose = ProHeroPose(imagePath = POSE_ASSETS + "viral_1.webp")
