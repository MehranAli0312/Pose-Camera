package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.imageModelOf
import com.aipose.camera.posematch.ui.screens.photoEdit.models.photoFilterLabel
import com.aipose.camera.posematch.ui.theme.Emerald
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private val ChipShape = RoundedCornerShape(10.dp)

@Composable
internal fun PhotoFilterStrip(
    imagePath: String,
    selectedFilter: PhotoFilterId,
    gradeFor: (PhotoFilterId) -> ColorGrade?,
    onFilterSelected: (PhotoFilterId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(PhotoFilterId.entries) { filterId ->
            PhotoFilterChip(
                imagePath = imagePath,
                filterId = filterId,
                isSelected = filterId == selectedFilter,
                grade = gradeFor(filterId),
                onClick = { onFilterSelected(filterId) },
            )
        }
    }
}

@Composable
private fun PhotoFilterChip(
    imagePath: String,
    filterId: PhotoFilterId,
    isSelected: Boolean,
    grade: ColorGrade?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    val label = stringResource(photoFilterLabel(filterId))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(64.dp)
            .bounceClick(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(ChipShape)
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) palette.accent else palette.glass,
                    shape = ChipShape,
                )
        ) {
            AsyncImage(
                model = imageModelOf(imagePath),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(ChipShape),
                contentScale = ContentScale.Crop,
                colorFilter = grade?.let { colorGrade ->
                    ColorFilter.colorMatrix(ColorMatrix(colorGrade.values.toFloatArray()))
                },
            )
            if (filterId == PhotoFilterId.Auto) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Emerald)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) palette.accent else androidx.compose.ui.graphics.Color.Gray,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
