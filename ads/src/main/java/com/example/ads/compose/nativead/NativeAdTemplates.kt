package com.example.ads.compose.nativead

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.ads.NativeAdColors
import com.example.ads.R
import com.example.ads.compose.AdSlotDefaults
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd

@Composable
internal fun SmallNativeTemplate(
    nativeAd: NativeAd,
    colors: NativeAdColors,
    modifier: Modifier = Modifier,
) {
    NativeAdSurface(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NativeAdIcon(nativeAd, size = 40.dp)

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AdAttributionBadge(colors)
                    NativeAdChoicesView()
                }
                NativeAdHeadlineView {
                    Text(
                        text = nativeAd.headline.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AdSlotDefaults.colors.headline,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
                nativeAd.advertiser?.takeIf { it.isNotBlank() }?.let { advertiser ->
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
            }

            NativeAdCta(nativeAd, colors)
        }
    }
}

@Composable
internal fun MediumNativeTemplate(
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NativeAdIcon(nativeAd, size = 40.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        AdAttributionBadge(colors)
                        NativeAdChoicesView()
                    }
                    NativeAdHeadlineView {
                        Text(
                            text = nativeAd.headline.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
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

            if (nativeAd.hasMedia()) {
                NativeAdMediaView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            }

            NativeAdCta(nativeAd, colors, fillWidth = true)
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
    NativeAdSurface(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    scaleType = ImageView.ScaleType.FIT_CENTER,
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NativeAdIcon(nativeAd, size = 56.dp)

                Column(modifier = Modifier.weight(1f)) {
                    NativeAdHeadlineView {
                        Text(
                            text = nativeAd.headline.orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AdSlotDefaults.colors.headline,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    nativeAd.advertiser?.takeIf { it.isNotBlank() }?.let { advertiser ->
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
                }
            }

            nativeAd.body?.takeIf { it.isNotBlank() }?.let { body ->
                NativeAdBodyView {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdSlotDefaults.colors.body,
                        maxLines = 3,
                    )
                }
            }

            NativeAdCta(nativeAd, colors, fillWidth = true, verticalPadding = 14.dp)
        }
    }
}

@Composable
private fun NativeAdSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AdSlotDefaults.colors.container)
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
private fun NativeAdIcon(nativeAd: NativeAd, size: androidx.compose.ui.unit.Dp) {
    val iconDrawable = nativeAd.icon?.drawable ?: return
    NativeAdIconView(modifier = Modifier.size(size)) {
        androidx.compose.foundation.Image(
            painter = BitmapPainter(iconDrawable.toBitmap().asImageBitmap()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp)),
        )
    }
}

@Composable
private fun NativeAdCta(
    nativeAd: NativeAd,
    colors: NativeAdColors,
    fillWidth: Boolean = false,
    verticalPadding: Dp = 8.dp,
) {
    val callToAction = nativeAd.callToAction?.takeIf { it.isNotBlank() } ?: return
    NativeAdCallToActionView(
        modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier.width(CtaWidth),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.ctaBackground.orThemeColor(MaterialTheme.colorScheme.primary))
                .padding(horizontal = 12.dp, vertical = verticalPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = callToAction,
                style = MaterialTheme.typography.labelLarge,
                color = colors.ctaText.orThemeColor(MaterialTheme.colorScheme.onPrimary),
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

private const val MEDIA_ASPECT_RATIO = 1.78f
private val CtaWidth = 96.dp

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
