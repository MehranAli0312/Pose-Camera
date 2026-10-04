package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.domain.models.PoseBone
import com.aipose.camera.posematch.domain.models.PoseJoint
import com.aipose.camera.posematch.domain.models.SkeletonPoint
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseVioletLight

private val BoneWidth = 3.8.dp
private val JointRadius = 5.7.dp
private val HeadStroke = 3.8.dp
private const val BONE_ALPHA = 0.88f
private const val HEAD_RADIUS_RATIO = 0.05f

@Composable
internal fun PoseSkeletonOverlay(
    skeleton: List<SkeletonPoint>,
    isMirrored: Boolean,
    modifier: Modifier = Modifier,
) {
    if (skeleton.none { point -> point.isDetected }) return

    Canvas(modifier = modifier) {
        val positions = skeleton.mapNotNull { point ->
            point.position?.let { position ->
                point.joint to Offset(
                    x = (if (isMirrored) 1f - position.x else position.x) * size.width,
                    y = position.y * size.height,
                )
            }
        }.toMap()
        val boneColor = PoseVioletLight.copy(alpha = BONE_ALPHA)

        PoseBone.entries.forEach { bone ->
            val start = positions[bone.from] ?: return@forEach
            val end = positions[bone.to] ?: return@forEach
            drawBone(boneColor, start, end)
        }

        val leftShoulder = positions[PoseJoint.LeftShoulder]
        val rightShoulder = positions[PoseJoint.RightShoulder]
        if (leftShoulder != null && rightShoulder != null) {
            drawBone(boneColor, leftShoulder, rightShoulder)
            val neck = Offset(
                x = (leftShoulder.x + rightShoulder.x) / 2f,
                y = (leftShoulder.y + rightShoulder.y) / 2f,
            )
            positions[PoseJoint.Head]?.let { head ->
                drawBone(boneColor, neck, head)
                drawCircle(
                    color = boneColor,
                    radius = size.minDimension * HEAD_RADIUS_RATIO,
                    center = head,
                    style = Stroke(width = HeadStroke.toPx()),
                )
            }
        }

        positions.forEach { (joint, position) ->
            if (joint == PoseJoint.Head) return@forEach
            drawCircle(
                color = PoseCyanBright,
                radius = JointRadius.toPx(),
                center = position,
            )
        }
    }
}

private fun DrawScope.drawBone(
    color: Color,
    start: Offset,
    end: Offset,
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = BoneWidth.toPx(),
        cap = StrokeCap.Round,
    )
}
