package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.AppTheme
import com.aipose.camera.posematch.ui.theme.White

val PremiumRateUsCorner = 32.dp
val PremiumRateUsMaxWidth = 340.dp
private val PremiumRateUsBottomPadding = 20.dp

private val BadgeOverhang = 52.dp
private val BadgeSize = 104.dp
private val BadgeStarSize = 50.dp
private val CardSidePadding = 18.dp
private val TitleTopPadding = 70.dp
private val StarSize = 24.dp
private val StarSpacing = 13.dp
private val ButtonHeight = 45.dp
private val CloseSize = 30.dp

private val CardEdgeGold = Color(0xFFD6B05C)
private val StarHighlight = Color(0xFFFFE9A6)
private val StarMid = Color(0xFFF2B437)
private val StarDeep = Color(0xFFB4701A)
private val ButtonHighlight = Color(0xFFFFE599)
private val ButtonMid = Color(0xFFF7C245)
private val ButtonDeep = Color(0xFFC88319)
private val ButtonLabelColor = Color(0xFF271600)
private val BadgeCore = Color(0xFFFFF4C8)
private val BadgeMid = Color(0xFFF7C241)
private val BadgeDeep = Color(0xFFBD730E)
private val BadgeEdge = Color(0xFF5C3404)
private val BadgeStarColor = Color(0xFFFFFDF3)
private val ShadowBrown = Color(0xFF4A2A04)
private val GoldGlow = Color(0xFFFFB838)

private val StarFillGradient = Brush.verticalGradient(
    0f to StarHighlight,
    0.45f to StarMid,
    1f to StarDeep,
)

@Immutable
private data class PremiumRateUsPalette(
    val cardTop: Color,
    val cardMid: Color,
    val cardBottom: Color,
    val cardEdge: Color,
    val cardSheen: Color,
    val cardShade: Color,
    val title: Color,
    val subtitle: Color,
    val dismiss: Color,
    val starEmpty: Color,
    val closeBackground: Color,
    val closeRim: Color,
    val closeIcon: Color,
    val cardShadow: Color,
    val cardElevation: Dp,
)

@Composable
private fun rememberPremiumRateUsPalette(): PremiumRateUsPalette {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = AppTheme.extendedColors
    val isDark = colorScheme.background.luminance() < 0.5f

    return remember(colorScheme, extendedColors, isDark) {
        val surface = colorScheme.secondaryContainer
        PremiumRateUsPalette(
            cardTop = if (isDark) lerp(surface, White, 0.09f) else White,
            cardMid = surface,
            cardBottom = lerp(surface, Color.Black, if (isDark) 0.35f else 0.04f),
            cardEdge = CardEdgeGold.copy(alpha = if (isDark) 0.28f else 0.45f),
            cardSheen = White.copy(alpha = if (isDark) 0.18f else 0.55f),
            cardShade = Color.Black.copy(alpha = if (isDark) 0.55f else 0.06f),
            title = colorScheme.onBackground,
            subtitle = colorScheme.onSurfaceVariant,
            dismiss = colorScheme.onSurfaceVariant,
            starEmpty = extendedColors.mediaPlaceholder,
            closeBackground = if (isDark) {
                White.copy(alpha = 0.07f)
            } else {
                extendedColors.mutedBackground
            },
            closeRim = Color.Black.copy(alpha = if (isDark) 0.6f else 0.08f),
            closeIcon = colorScheme.onSurfaceVariant,
            cardShadow = if (isDark) {
                Color.Black
            } else {
                lerp(colorScheme.onBackground, White, 0.5f)
            },
            cardElevation = if (isDark) 28.dp else 18.dp,
        )
    }
}

@Composable
fun PremiumRateUsContent(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    cardShape: Shape = RoundedCornerShape(PremiumRateUsCorner),
    cardBottomPadding: Dp = PremiumRateUsBottomPadding,
) {
    val palette = rememberPremiumRateUsPalette()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        PremiumRateUsCard(
            rating = rating,
            maxStars = maxStars,
            palette = palette,
            cardShape = cardShape,
            cardBottomPadding = cardBottomPadding,
            onRatingChanged = onRatingChanged,
            onSubmit = onSubmit,
            onDismiss = onDismiss,
        )

        PremiumRateUsBadge()
    }
}

@Composable
private fun PremiumRateUsCard(
    rating: Float,
    maxStars: Int,
    palette: PremiumRateUsPalette,
    cardShape: Shape,
    cardBottomPadding: Dp,
    onRatingChanged: (Float) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = BadgeOverhang)
            .shadow(
                elevation = palette.cardElevation,
                shape = cardShape,
                clip = false,
                ambientColor = palette.cardShadow,
                spotColor = palette.cardShadow,
            )
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    0f to palette.cardTop,
                    0.55f to palette.cardMid,
                    1f to palette.cardBottom,
                ),
            )
            .border(1.dp, palette.cardEdge, cardShape)
            .drawBehind {
                val rim = 1.5.dp.toPx()
                drawShapeRim(
                    shape = cardShape,
                    brush = Brush.verticalGradient(
                        colors = listOf(palette.cardSheen, Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.35f,
                    ),
                    width = rim,
                )
                drawShapeRim(
                    shape = cardShape,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, palette.cardShade),
                        startY = size.height * 0.65f,
                        endY = size.height,
                    ),
                    width = rim,
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CardSidePadding)
                .padding(top = TitleTopPadding, bottom = cardBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.rate_us_premium_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.title,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.rate_us_premium_subtitle),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = palette.subtitle,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            PremiumRateUsStars(
                rating = rating,
                maxStars = maxStars,
                emptyStarColor = palette.starEmpty,
                onRatingChanged = onRatingChanged,
            )

            Spacer(modifier = Modifier.height(20.dp))

            PremiumRateUsButton(onClick = onSubmit)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.rate_us_premium_dismiss),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.dismiss.copy(alpha = .5f),
                ),
                modifier = Modifier.click(onClick = onDismiss),
            )
        }

        PremiumRateUsClose(
            palette = palette,
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 22.dp, end = 22.dp),
        )
    }
}

private fun DrawScope.drawShapeRim(shape: Shape, brush: Brush, width: Float) {
    drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        brush = brush,
        style = Stroke(width = width),
    )
}

@Composable
private fun PremiumRateUsBadge() {
    Box(
        modifier = Modifier
            .size(BadgeSize)
            .drawBehind {
                val bloom = size.maxDimension * 0.9f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GoldGlow.copy(alpha = 0.32f), Color.Transparent),
                        center = center,
                        radius = bloom,
                    ),
                    radius = bloom,
                )
            }
            .shadow(
                elevation = 22.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = ShadowBrown,
                spotColor = ShadowBrown,
            )
            .clip(CircleShape)
            .drawBehind {
                val radius = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to BadgeCore,
                        0.4f to BadgeMid,
                        0.8f to BadgeDeep,
                        1f to BadgeEdge,
                        center = Offset(size.width * 0.36f, size.height * 0.34f),
                        radius = radius * 1.05f,
                    ),
                    radius = radius,
                )

                val shadeWidth = 16.dp.toPx()
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, ShadowBrown.copy(alpha = 0.8f)),
                        startY = size.height * 0.35f,
                        endY = size.height,
                    ),
                    radius = radius - shadeWidth / 2f,
                    style = Stroke(width = shadeWidth),
                )

                val rimWidth = 4.dp.toPx()
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(White.copy(alpha = 0.75f), Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.4f,
                    ),
                    radius = radius - rimWidth / 2f,
                    style = Stroke(width = rimWidth),
                )

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(White.copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(size.width * 0.34f, size.height * 0.24f),
                        radius = size.minDimension * 0.2f,
                    ),
                    topLeft = Offset(size.width * 0.18f, size.height * 0.13f),
                    size = Size(size.width * 0.32f, size.height * 0.2f),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_rate_star),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(ShadowBrown.copy(alpha = 0.7f)),
            modifier = Modifier
                .size(BadgeStarSize)
                .offset(y = 1.dp),
        )

        Image(
            painter = painterResource(R.drawable.ic_rate_star),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(BadgeStarColor),
            modifier = Modifier
                .size(BadgeStarSize)
                .offset(y = (-2).dp),
        )
    }
}

@Composable
private fun PremiumRateUsStars(
    rating: Float,
    maxStars: Int,
    emptyStarColor: Color,
    onRatingChanged: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(StarSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (star in 1..maxStars) {
            val isSelected = star <= rating
            Image(
                painter = painterResource(R.drawable.ic_rate_star),
                contentDescription = pluralStringResource(
                    R.plurals.rate_us_premium_star,
                    star,
                    star,
                ),
                contentScale = ContentScale.Fit,
                colorFilter = if (isSelected) null else ColorFilter.tint(emptyStarColor),
                modifier = Modifier
                    .size(StarSize)
                    .selectable(
                        selected = isSelected,
                        onClick = { onRatingChanged(star.toFloat()) },
                    )
                    .then(if (isSelected) Modifier.starGlow() else Modifier)
                    .then(if (isSelected) Modifier.goldStarFill() else Modifier),
            )
        }
    }
}

private fun Modifier.starGlow() = drawBehind {
    val glow = size.maxDimension * 0.75f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GoldGlow.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(center.x, center.y + size.height * 0.1f),
            radius = glow,
        ),
        radius = glow,
    )
}

private fun Modifier.goldStarFill() = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(brush = StarFillGradient, blendMode = BlendMode.SrcIn)
    }

@Composable
private fun PremiumRateUsButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(ButtonHeight / 2)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .shadow(
                elevation = 16.dp,
                shape = shape,
                clip = false,
                ambientColor = ButtonDeep,
                spotColor = ButtonDeep,
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    0f to ButtonHighlight,
                    0.35f to ButtonMid,
                    1f to ButtonDeep,
                ),
            )
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(White.copy(alpha = 0.7f), Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.2f,
                    ),
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, ShadowBrown.copy(alpha = 0.45f)),
                        startY = size.height * 0.7f,
                        endY = size.height,
                    ),
                )
            }
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.rate_us_premium_action),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ButtonLabelColor,
            ),
        )
    }
}

@Composable
private fun PremiumRateUsClose(
    palette: PremiumRateUsPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(CloseSize)
            .clip(CircleShape)
            .background(palette.closeBackground)
            .drawBehind {
                val inset = 2.dp.toPx()
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(palette.closeRim, Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.5f,
                    ),
                    radius = size.minDimension / 2f - inset / 2f,
                    style = Stroke(width = inset),
                )
            }
            .click(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.close),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(palette.closeIcon),
            modifier = Modifier.size(13.dp),
        )
    }
}
