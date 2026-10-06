package com.example.ads.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ads.R
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

@Composable
internal fun AdSlotPlaceholder(height: Dp, modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val colors = AdSlotDefaults.colors
    val boneColor = colors.placeholderBone

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.container),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .shimmer(shimmer),
        ) {
            if (height <= CompactMaxHeight) {
                CompactAdSkeleton(
                    boneColor = boneColor,
                    textColor = colors.body,
                    showRating = height >= RatingMinHeight,
                )
            } else {
                TallAdSkeleton(boneColor = boneColor, textColor = colors.body)
            }
            Bone(
                color = boneColor,
                shape = MarkerShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(MarkerSize),
            )
        }
    }
}

@Composable
private fun CompactAdSkeleton(boneColor: Color, textColor: Color, showRating: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = MarkerSize),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AdBadge(
            boneColor = boneColor,
            textColor = textColor,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f, matchHeightConstraintsFirst = true),
        )
        HeadlineBones(
            boneColor = boneColor,
            textColor = textColor,
            showRating = showRating,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TallAdSkeleton(boneColor: Color, textColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(end = MarkerSize),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AdBadge(
                boneColor = boneColor,
                textColor = textColor,
                modifier = Modifier.size(TallBadgeSize),
            )
            HeadlineBones(
                boneColor = boneColor,
                textColor = textColor,
                showRating = true,
                modifier = Modifier.weight(1f),
            )
        }
        MediaBone(boneColor)
        Bone(boneColor, Modifier.fillMaxWidth().height(36.dp))
    }
}

@Composable
private fun AdBadge(boneColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(boneColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.ads_attribution_label),
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun HeadlineBones(
    boneColor: Color,
    textColor: Color,
    showRating: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Bone(boneColor, Modifier.fillMaxWidth(0.7f).height(12.dp))
        Bone(boneColor, Modifier.fillMaxWidth(0.45f).height(10.dp))
        if (showRating) {
            Text(
                text = stringResource(R.string.ads_placeholder_rating),
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ColumnScope.MediaBone(boneColor: Color) {
    Bone(
        color = boneColor,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
    )
}

@Composable
private fun Bone(
    color: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color),
    )
}

private val MarkerShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(0f, size.height)
    close()
}

private val CompactMaxHeight = 100.dp
private val RatingMinHeight = 64.dp
private val TallBadgeSize = 40.dp
private val MarkerSize = 10.dp
