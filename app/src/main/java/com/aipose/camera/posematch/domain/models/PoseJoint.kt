package com.aipose.camera.posematch.domain.models

enum class PoseJoint(val key: String) {
    Head("head"),
    LeftShoulder("left_shoulder"),
    RightShoulder("right_shoulder"),
    LeftElbow("left_elbow"),
    RightElbow("right_elbow"),
    LeftWrist("left_wrist"),
    RightWrist("right_wrist"),
    LeftHip("left_hip"),
    RightHip("right_hip"),
    LeftKnee("left_knee"),
    RightKnee("right_knee"),
    LeftAnkle("left_ankle"),
    RightAnkle("right_ankle");

    companion object {
        private val byKey = entries.associateBy { it.key }

        fun fromKey(key: String): PoseJoint? = byKey[key]
    }
}
