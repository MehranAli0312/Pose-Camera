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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

@Composable
internal fun AdSlotPlaceholder(height: Dp, modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val boneColor = PlaceholderBoneColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(PlaceholderSurfaceColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .shimmer(shimmer),
        ) {
            if (height <= CompactMaxHeight) {
                CompactAdSkeleton(boneColor)
            } else {
                TallAdSkeleton(boneColor)
            }
        }
    }
}

@Composable
private fun CompactAdSkeleton(boneColor: Color) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Bone(
            color = boneColor,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f, matchHeightConstraintsFirst = true),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Bone(boneColor, Modifier.fillMaxWidth(0.7f).height(12.dp))
            Bone(boneColor, Modifier.fillMaxWidth(0.45f).height(10.dp))
        }
        Bone(
            color = boneColor,
            modifier = Modifier
                .width(CtaWidth)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun TallAdSkeleton(boneColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Bone(boneColor, Modifier.size(40.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Bone(boneColor, Modifier.fillMaxWidth(0.7f).height(12.dp))
                Bone(boneColor, Modifier.fillMaxWidth(0.45f).height(10.dp))
            }
        }
        MediaBone(boneColor)
        Bone(boneColor, Modifier.fillMaxWidth().height(36.dp))
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
private fun Bone(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color),
    )
}

private val PlaceholderSurfaceColor = Color.White
private val PlaceholderBoneColor = Color(0xFFE1E1E1)
private val CompactMaxHeight = 100.dp
private val CtaWidth = 96.dp
