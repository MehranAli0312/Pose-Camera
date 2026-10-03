package com.aipose.camera.posematch.domain.models

enum class PoseBone(val from: PoseJoint, val to: PoseJoint) {
    LeftUpperArm(PoseJoint.LeftShoulder, PoseJoint.LeftElbow),
    LeftForearm(PoseJoint.LeftElbow, PoseJoint.LeftWrist),
    RightUpperArm(PoseJoint.RightShoulder, PoseJoint.RightElbow),
    RightForearm(PoseJoint.RightElbow, PoseJoint.RightWrist),
    LeftThigh(PoseJoint.LeftHip, PoseJoint.LeftKnee),
    LeftShin(PoseJoint.LeftKnee, PoseJoint.LeftAnkle),
    RightThigh(PoseJoint.RightHip, PoseJoint.RightKnee),
    RightShin(PoseJoint.RightKnee, PoseJoint.RightAnkle),
    LeftTorso(PoseJoint.LeftShoulder, PoseJoint.LeftHip),
    RightTorso(PoseJoint.RightShoulder, PoseJoint.RightHip)
}
