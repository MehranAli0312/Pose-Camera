package com.example.ads.compose.nativead

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.ads.NativeAdColors
import com.example.ads.R
import com.example.ads.compose.AdSlotDefaults
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import kotlin.math.roundToInt

@Composable
internal fun SmallNativeTemplate(
    nativeAd: NativeAd,
    colors: NativeAdColors,
    modifier: Modifier = Modifier,
) {
    NativeAdSurface(modifier, cornerRadius = SmallCornerRadius) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AdAttributionBadge(colors)
                Spacer(modifier = Modifier.weight(1f))
                NativeAdChoicesView()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NativeAdIcon(nativeAd, size = 48.dp, cornerRadius = 12.dp)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    NativeAdHeadlineView {
                        Text(
                            text = nativeAd.headline.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AdSlotDefaults.colors.headline,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    SmallNativeSupportingText(nativeAd)
                }

                NativeAdCta(
                    nativeAd = nativeAd,
                    colors = colors,
                    verticalPadding = 10.dp,
                    shape = CircleShape,
                )
            }
        }
    }
}

@Composable
private fun SmallNativeSupportingText(nativeAd: NativeAd) {
    val body = nativeAd.body?.takeIf { it.isNotBlank() }
    if (body != null) {
        NativeAdBodyView {
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = AdSlotDefaults.colors.body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        return
    }
    val advertiser = nativeAd.advertiser?.takeIf { it.isNotBlank() } ?: return
    NativeAdAdvertiserView {
        Text(
            text = advertiser,
            style = MaterialTheme.typography.bodySmall,
            color = AdSlotDefaults.colors.body,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )
    }
}

@Composable
internal fun MediumNativeTemplate(
    nativeAd: NativeAd,
    colors: NativeAdColors,
    modifier: Modifier = Modifier,
) {
    NativeAdSurface(modifier, cornerRadius = MediumCornerRadius) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MediumContentHeight + MediumPadding * 2)
                .padding(MediumPadding),
            horizontalArrangement = Arrangement.spacedBy(MediumPadding),
        ) {
            if (nativeAd.hasMedia()) {
                NativeAdMediaView(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(MEDIUM_MEDIA_WEIGHT)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(MEDIUM_DETAILS_WEIGHT),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    NativeAdIcon(nativeAd, size = 36.dp)
                    Spacer(modifier = Modifier.weight(1f))
                    AdAttributionBadge(colors)
                    NativeAdChoicesView()
                }

                NativeAdHeadlineView {
                    Text(
                        text = nativeAd.headline.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AdSlotDefaults.colors.headline,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }

                MediumNativeRatingOrAdvertiser(nativeAd)

                nativeAd.body?.takeIf { it.isNotBlank() }?.let { body ->
                    NativeAdBodyView {
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodySmall,
                            color = AdSlotDefaults.colors.body,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                NativeAdCta(nativeAd, colors, fillWidth = true, verticalPadding = 10.dp)
            }
        }
    }
}

@Composable
private fun MediumNativeRatingOrAdvertiser(nativeAd: NativeAd) {
    val rating = nativeAd.starRating?.takeIf { it > 0.0 }
    if (rating != null) {
        NativeAdStarRatingView {
            NativeAdStars(rating)
        }
        return
    }
    val advertiser = nativeAd.advertiser?.takeIf { it.isNotBlank() } ?: return
    NativeAdAdvertiserView {
        Text(
            text = advertiser,
            style = MaterialTheme.typography.labelSmall,
            color = AdSlotDefaults.colors.body,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )
    }
}

@Composable
private fun NativeAdStars(rating: Double) {
    val filledStars = rating.roundToInt().coerceIn(0, MAX_STARS)
    val emptyStarColor = AdSlotDefaults.colors.body.copy(alpha = EMPTY_STAR_ALPHA)
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(MAX_STARS) { index ->
            Icon(
                painter = painterResource(R.drawable.ads_ic_star),
                contentDescription = null,
                tint = if (index < filledStars) FilledStarColor else emptyStarColor,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
internal fun LargeNativeTemplate(
    nativeAd: NativeAd,
    colors: NativeAdColors,
    modifier: Modifier = Modifier,
) {
    NativeAdSurface(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AdAttributionBadge(colors)
                NativeAdChoicesView()
            }

            if (nativeAd.hasMedia()) {
                NativeAdMediaView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(MEDIA_ASPECT_RATIO)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NativeAdIcon(nativeAd, size = 44.dp)

                Column(modifier = Modifier.weight(1f)) {
                    NativeAdHeadlineView {
                        Text(
                            text = nativeAd.headline.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AdSlotDefaults.colors.headline,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    nativeAd.body?.takeIf { it.isNotBlank() }?.let { body ->
                        NativeAdBodyView {
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodySmall,
                                color = AdSlotDefaults.colors.body,
                                maxLines = 2,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    }
                }
            }

            NativeAdCta(nativeAd, colors, fillWidth = true)
        }
    }
}

@Composable
internal fun FullScreenNativeTemplate(
    nativeAd: NativeAd,
    colors: NativeAdColors,
    modifier: Modifier = Modifier,
) {
    NativeAdSurface(modifier.fillMaxSize(), cornerRadius = 0.dp, strokeWidth = 0.dp) {
        Column(modifier = Modifier.fillMaxSize()) {
            FullScreenNativeMedia(
                nativeAd = nativeAd,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            FullScreenNativeDetails(nativeAd, colors)
        }
    }
}

@Composable
private fun FullScreenNativeMedia(nativeAd: NativeAd, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        if (nativeAd.hasMedia()) {
            NativeAdMediaView(
                modifier = Modifier.fillMaxSize(),
                scaleType = ImageView.ScaleType.FIT_CENTER,
            )
        }
        NativeAdChoicesView(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(FullScreenAdChoicesInset),
        )
    }
}

@Composable
private fun FullScreenNativeDetails(nativeAd: NativeAd, colors: NativeAdColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FullScreenHorizontalPadding, vertical = FullScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(FullScreenDetailsSpacing),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FullScreenInfoSpacing),
        ) {
            NativeAdIcon(nativeAd, size = FullScreenIconSize, cornerRadius = FullScreenIconCornerRadius)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FullScreenTextSpacing),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FullScreenBadgeSpacing),
                ) {
                    NativeAdHeadlineView(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = nativeAd.headline.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AdSlotDefaults.colors.headline,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    AdAttributionBadge(colors)
                }
                SmallNativeSupportingText(nativeAd)
            }
        }

        NativeAdCta(
            nativeAd = nativeAd,
            colors = colors,
            fillWidth = true,
            verticalPadding = FullScreenCtaVerticalPadding,
            shape = RoundedCornerShape(FullScreenCtaCornerRadius),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NativeAdSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    strokeWidth: Dp = NativeStrokeWidth,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AdSlotDefaults.colors.container)
            .border(strokeWidth, AdSlotDefaults.colors.nativeStroke, shape)
            .consumeNonAssetTouches(),
    ) {
        content()
    }
}

@Composable
private fun AdAttributionBadge(colors: NativeAdColors) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(colors.labelBackground.orThemeColor(MaterialTheme.colorScheme.primary))
            .padding(horizontal = 4.dp, vertical = 1.dp),
    ) {
        Text(
            text = stringResource(R.string.ads_attribution_label),
            style = MaterialTheme.typography.labelSmall,
            color = colors.labelText.orThemeColor(MaterialTheme.colorScheme.onPrimary),
        )
    }
}

@Composable
private fun NativeAdIcon(nativeAd: NativeAd, size: Dp, cornerRadius: Dp = 8.dp) {
    val iconDrawable = nativeAd.icon?.drawable ?: return
    NativeAdIconView(modifier = Modifier.size(size)) {
        androidx.compose.foundation.Image(
            painter = BitmapPainter(iconDrawable.toBitmap().asImageBitmap()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius)),
        )
    }
}

@Composable
private fun NativeAdCta(
    nativeAd: NativeAd,
    colors: NativeAdColors,
    fillWidth: Boolean = false,
    verticalPadding: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    fontWeight: FontWeight? = null,
) {
    val callToAction = nativeAd.callToAction?.takeIf { it.isNotBlank() } ?: return
    NativeAdCallToActionView(
        modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier.width(CtaWidth),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.ctaBackground.orThemeColor(MaterialTheme.colorScheme.primary))
                .padding(horizontal = 12.dp, vertical = verticalPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = callToAction,
                style = MaterialTheme.typography.labelLarge,
                color = colors.ctaText.orThemeColor(MaterialTheme.colorScheme.onPrimary),
                fontWeight = fontWeight,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

private const val MEDIA_ASPECT_RATIO = 1.78f
private val CtaWidth = 96.dp
private val SmallCornerRadius = 12.dp
private val MediumCornerRadius = 12.dp
private val MediumPadding = 8.dp
private val MediumContentHeight = 164.dp
private const val MEDIUM_MEDIA_WEIGHT = 0.42f
private const val MEDIUM_DETAILS_WEIGHT = 0.58f
private val NativeStrokeWidth = 1.5.dp
private val FullScreenAdChoicesInset = 8.dp
private val FullScreenHorizontalPadding = 16.dp
private val FullScreenVerticalPadding = 12.dp
private val FullScreenDetailsSpacing = 12.dp
private val FullScreenInfoSpacing = 10.dp
private val FullScreenTextSpacing = 2.dp
private val FullScreenBadgeSpacing = 6.dp
private val FullScreenIconSize = 44.dp
private val FullScreenIconCornerRadius = 10.dp
private val FullScreenCtaVerticalPadding = 13.dp
private val FullScreenCtaCornerRadius = 10.dp
private const val MAX_STARS = 5
private const val EMPTY_STAR_ALPHA = 0.35f
private val FilledStarColor = Color(0xFFFFB300)

/**
 * Claims touches on the template background so they never reach the enclosing NativeAdView,
 * keeping only the registered asset views clickable. Asset views sit deeper in the hierarchy
 * and receive their events first.
 */
private fun Modifier.consumeNonAssetTouches(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent().changes.forEach { it.consume() }
        }
    }
}

private fun Int?.orThemeColor(themeColor: Color): Color = this?.let(::Color) ?: themeColor

private fun NativeAd.hasMedia(): Boolean =
    mediaContent.hasVideoContent || mediaContent.mainImage != null
