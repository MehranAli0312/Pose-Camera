package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.theme.AppTheme

private val HeroShape = RoundedCornerShape(24.dp)
private const val LARGE_BUBBLE_ALPHA = 0.08f
private const val SMALL_BUBBLE_ALPHA = 0.06f
const val HERO_SECONDARY_ALPHA = 0.85f
const val HERO_TRACK_ALPHA = 0.22f

@Composable
fun GradientHeroSurface(
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = HeroShape,
                ambientColor = AppTheme.extendedColors.contactShadow,
                spotColor = AppTheme.extendedColors.contactShadow,
            )
            .clip(HeroShape)
            .background(Brush.linearGradient(gradient)),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val endX = if (isRtl) 0f else size.width
            val inset = 26.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = LARGE_BUBBLE_ALPHA),
                radius = 56.dp.toPx(),
                center = Offset(if (isRtl) endX + inset else endX - inset, 12.dp.toPx()),
            )
            drawCircle(
                color = Color.White.copy(alpha = SMALL_BUBBLE_ALPHA),
                radius = 40.dp.toPx(),
                center = Offset(size.width / 2f, size.height),
            )
        }
        content()
    }
}

@Composable
fun HeroCaptionText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.basicMarquee(),
        style = heroTextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Bold, alpha = HERO_SECONDARY_ALPHA),
        maxLines = 1,
    )
}

@Composable
fun HeroBodyText(text: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Text(
        text = text,
        modifier = if (maxLines == 1) modifier.basicMarquee() else modifier,
        style = heroTextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Normal, alpha = HERO_SECONDARY_ALPHA),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun heroTextStyle(
    fontSize: TextUnit,
    fontWeight: FontWeight,
    alpha: Float = 1f,
): TextStyle = MaterialTheme.typography.labelMedium.copy(
    fontSize = fontSize,
    fontWeight = fontWeight,
    color = Color.White.copy(alpha = alpha),
)
