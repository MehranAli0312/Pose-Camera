package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.screens.pro.data.proHeroCenterPose
import com.aipose.camera.posematch.ui.screens.pro.data.proHeroEndPose
import com.aipose.camera.posematch.ui.screens.pro.data.proHeroStartPose
import com.aipose.camera.posematch.ui.screens.pro.models.ProHeroPose
import com.aipose.camera.posematch.ui.screens.pro.models.ProPoseSkeleton
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PosePremiumHeroClay
import com.aipose.camera.posematch.ui.theme.PosePremiumHeroSand
import kotlin.math.max

private val HeroCardShape = RoundedCornerShape(18.dp)
private val SideCardTop = 26.dp
private val HeroBottomSpace = 8.dp
private val CenterBorderWidth = 1.5.dp
private val SideBorderWidth = 1.dp
private val CenterElevation = 22.dp
private val SideElevation = 10.dp
private val BracketInset = 12.dp
private val BracketLength = 18.dp
private val BracketStroke = 2.dp
private val BoneStroke = 1.6.dp
private val JointRadius = 3.dp
private const val CENTER_WIDTH_RATIO = 0.38f
private const val SIDE_WIDTH_RATIO = 0.31f
private const val SIDE_EDGE_RATIO = 0.02f
private const val CARD_ASPECT_RATIO = 0.7f
private const val SIDE_TILT_DEGREES = 8f
private const val SIDE_BORDER_ALPHA = 0.45f
private const val CENTER_GLOW_ALPHA = 0.55f
private const val SIDE_GLOW_ALPHA = 0.25f
private const val BRACKET_ALPHA = 0.9f
private const val BONE_ALPHA = 0.85f

@Composable
internal fun ProHero(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val centerWidth = maxWidth * CENTER_WIDTH_RATIO
        val centerHeight = centerWidth / CARD_ASPECT_RATIO
        val sideWidth = maxWidth * SIDE_WIDTH_RATIO
        val sideHeight = sideWidth / CARD_ASPECT_RATIO
        val sideInset = maxWidth * SIDE_EDGE_RATIO
        val endCardStart = maxWidth - sideInset - sideWidth
        val centerCardStart = (maxWidth - centerWidth) / 2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(centerHeight + HeroBottomSpace),
            contentAlignment = AbsoluteAlignment.TopLeft,
        ) {
            ProHeroCard(
                pose = proHeroStartPose,
                highlighted = false,
                modifier = Modifier
                    .absoluteOffset(x = sideInset, y = SideCardTop)
                    .size(width = sideWidth, height = sideHeight)
                    .rotate(-SIDE_TILT_DEGREES),
            )
            ProHeroCard(
                pose = proHeroEndPose,
                highlighted = false,
                modifier = Modifier
                    .absoluteOffset(x = endCardStart, y = SideCardTop)
                    .size(width = sideWidth, height = sideHeight)
                    .rotate(SIDE_TILT_DEGREES),
            )
            ProHeroCard(
                pose = proHeroCenterPose,
                highlighted = true,
                modifier = Modifier
                    .absoluteOffset(x = centerCardStart)
                    .size(width = centerWidth, height = centerHeight),
            )
        }
    }
}

@Composable
private fun ProHeroCard(
    pose: ProHeroPose,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    var imageSize by remember(pose) { mutableStateOf(Size.Unspecified) }
    val glowColor = PosePremiumGold.copy(alpha = if (highlighted) CENTER_GLOW_ALPHA else SIDE_GLOW_ALPHA)
    Box(
        modifier = modifier
            .shadow(
                elevation = if (highlighted) CenterElevation else SideElevation,
                shape = HeroCardShape,
                ambientColor = glowColor,
                spotColor = glowColor,
            )
            .clip(HeroCardShape)
            .background(Brush.verticalGradient(listOf(PosePremiumHeroSand, PosePremiumHeroClay)))
            .border(
                width = if (highlighted) CenterBorderWidth else SideBorderWidth,
                color = if (highlighted) PosePremiumGold else PosePremiumGold.copy(alpha = SIDE_BORDER_ALPHA),
                shape = HeroCardShape,
            ),
    ) {
        PoseImage(
            imagePath = pose.imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholderColor = Color.Transparent,
            onImageSizeKnown = pose.skeleton?.let { { size -> imageSize = size } },
        )
        if (highlighted) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCornerBrackets()
                val skeleton = pose.skeleton
                if (skeleton != null && imageSize.isSpecified) drawSkeleton(skeleton, imageSize)
            }
        }
    }
}

private fun DrawScope.drawCornerBrackets() {
    val inset = BracketInset.toPx()
    val length = BracketLength.toPx()
    val stroke = BracketStroke.toPx()
    val color = Color.White.copy(alpha = BRACKET_ALPHA)
    val right = size.width - inset
    val bottom = size.height - inset
    listOf(
        Triple(Offset(inset, inset), 1f, 1f),
        Triple(Offset(right, inset), -1f, 1f),
        Triple(Offset(inset, bottom), 1f, -1f),
        Triple(Offset(right, bottom), -1f, -1f),
    ).forEach { (corner, horizontal, vertical) ->
        drawLine(color, corner, Offset(corner.x + horizontal * length, corner.y), stroke, StrokeCap.Round)
        drawLine(color, corner, Offset(corner.x, corner.y + vertical * length), stroke, StrokeCap.Round)
    }
}

private fun DrawScope.drawSkeleton(skeleton: ProPoseSkeleton, imageSize: Size) {
    if (imageSize.width <= 0f || imageSize.height <= 0f) return
    val scale = max(size.width / imageSize.width, size.height / imageSize.height)
    val left = (size.width - imageSize.width * scale) / 2f
    val top = (size.height - imageSize.height * scale) / 2f
    val points = skeleton.joints.map { joint ->
        Offset(left + joint.x * imageSize.width * scale, top + joint.y * imageSize.height * scale)
    }
    val boneColor = Color.White.copy(alpha = BONE_ALPHA)
    val stroke = BoneStroke.toPx()
    skeleton.bones.forEach { (from, to) ->
        drawLine(boneColor, points[from], points[to], stroke, StrokeCap.Round)
    }
    val radius = JointRadius.toPx()
    points.forEach { point -> drawCircle(Color.White, radius, point) }
}
