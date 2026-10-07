package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.PoseProBadge
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseRaisedCard
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.home.models.HomeHero
import com.aipose.camera.posematch.ui.theme.PoseHeroBottom
import com.aipose.camera.posematch.ui.theme.PoseHeroMid
import com.aipose.camera.posematch.ui.theme.PoseHeroScrimMid
import com.aipose.camera.posematch.ui.theme.PoseHeroScrimStart
import com.aipose.camera.posematch.ui.theme.PoseHeroTop
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardCorner = 26.dp
private val CardHeight = 172.dp
private const val PHOTO_WIDTH_FRACTION = 164f / 350f
private const val CONTENT_WIDTH_FRACTION = 1f - PHOTO_WIDTH_FRACTION
private val CtaWidth = 152.dp
private val CtaHeight = 42.dp
private val CtaShape = RoundedCornerShape(21.dp)
private val CtaShadowOffset = 5.dp
private val CtaShadowInset = 2.dp
private val CtaGlossInset = 6.dp
private val CtaGlossTop = 3.dp

private const val CARD_BORDER_ALPHA = 0.10f
private const val CARD_SHADOW_ALPHA = 0.55f
private const val SCRIM_MID_ALPHA = 0.6f
private const val SCRIM_END_STOP = 0.744f
private const val CTA_SHADOW_ALPHA = 0.55f
private const val CTA_GLOSS_ALPHA = 0.4f
private const val CTA_GLOSS_HEIGHT_RATIO = 0.45f
private const val GRADIENT_MID_STOP = 0.55f

@Composable
internal fun HomeHeroCard(
    hero: HomeHero,
    onStartPosing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardHeight)
            .poseRaisedCard(
                cornerRadius = CardCorner,
                brush = Brush.linearGradient(
                    0f to PoseHeroTop,
                    GRADIENT_MID_STOP to PoseHeroMid,
                    1f to PoseHeroBottom,
                ),
                shadowColor = PoseShadow,
                shadowAlpha = CARD_SHADOW_ALPHA,
                borderColor = Color.White.copy(alpha = CARD_BORDER_ALPHA),
            ),
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterStart,
        ) {
            HeroPhoto(
                imagePath = hero.pose.imagePath,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(PHOTO_WIDTH_FRACTION),
            )
        }

        if (hero.isLocked) {
            PoseProBadge(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(CONTENT_WIDTH_FRACTION)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp),
                text = hero.pose.category,
                style = poseTextStyle(19.sp, FontWeight.Bold, Color.White),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(10.dp))
            StartPosingButton(
                onClick = onStartPosing,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun StartPosingButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = GlossyBadgePalette.HeroCta
    Row(
        modifier = modifier
            .widthIn(max = CtaWidth)
            .fillMaxWidth()
            .height(CtaHeight)
            .drawBehind {
                drawRoundRect(
                    color = palette.shadow.copy(alpha = CTA_SHADOW_ALPHA),
                    topLeft = Offset(CtaShadowInset.toPx(), CtaShadowOffset.toPx()),
                    size = Size(size.width - CtaShadowInset.toPx(), size.height),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            }
            .clip(CtaShape)
            .background(
                Brush.linearGradient(
                    0f to palette.top,
                    GRADIENT_MID_STOP to palette.mid,
                    1f to palette.bottom,
                )
            )
            .drawBehind {
                val inset = CtaGlossInset.toPx()
                val top = CtaGlossTop.toPx()
                val glossHeight = size.height * CTA_GLOSS_HEIGHT_RATIO
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = CTA_GLOSS_ALPHA),
                            Color.Transparent,
                        ),
                        startY = top,
                        endY = top + glossHeight,
                    ),
                    topLeft = Offset(inset, top),
                    size = Size(size.width - inset * 2f, glossHeight),
                    cornerRadius = CornerRadius(glossHeight / 2f),
                )
            }
            .bounceClick(onClick = onClick)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_camera_cta),
            contentDescription = null,
            modifier = Modifier.size(width = 18.dp, height = 14.dp),
        )
        Text(
            text = stringResource(R.string.home_hero_cta),
            style = poseTextStyle(13.5.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
        )
    }
}

@Composable
private fun HeroPhoto(imagePath: String, modifier: Modifier = Modifier) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Ltr
    Box(
        modifier = modifier
    ) {
        PoseImage(
            imagePath = imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(colorStops = scrimStops(isRtl))),
        )
    }
}

private fun scrimStops(isRtl: Boolean): Array<Pair<Float, Color>> {
    val stops = arrayOf(
        0f to PoseHeroScrimStart,
        SCRIM_END_STOP / 2f to PoseHeroScrimMid.copy(alpha = SCRIM_MID_ALPHA),
        SCRIM_END_STOP to Color.Transparent,
    )
    if (!isRtl) return stops
    return stops.reversed().map { (stop, color) -> (1f - stop) to color }.toTypedArray()
}
