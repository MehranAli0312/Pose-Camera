package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.screens.saved.models.SavedSort
import com.aipose.camera.posematch.ui.screens.saved.models.SavedTab
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PosePink
import com.aipose.camera.posematch.ui.theme.PoseRaisedBottom
import com.aipose.camera.posematch.ui.theme.PoseRaisedTop
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val SheetShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
private val OptionShape = RoundedCornerShape(18.dp)
private val OptionHeight = 54.dp
private val HandleWidth = 44.dp
private val HandleHeight = 4.dp
private val CheckSize = 16.dp

private const val OPTION_BORDER_ALPHA = 0.09f
private const val HANDLE_ALPHA = 0.3f
private const val SELECTED_BORDER_ALPHA = 0.55f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SavedSortSheet(
    selected: SavedSort,
    tab: SavedTab,
    onSelect: (SavedSort) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalAppPalette.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = palette.card,
        shape = SheetShape,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(HandleWidth)
                        .height(HandleHeight)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = HANDLE_ALPHA)),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.saved_sort_title),
                style = poseTextStyle(16.sp, FontWeight.Bold, Color.White),
            )
            Spacer(modifier = Modifier.height(2.dp))
            SavedSort.entries
                .filterNot { it.shotsOnly && tab == SavedTab.Poses }
                .forEach { option ->
                    SortOption(
                        option = option,
                        isSelected = option == selected,
                        onClick = { onSelect(option) },
                    )
                }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SortOption(
    option: SavedSort,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) {
        PosePink.copy(alpha = SELECTED_BORDER_ALPHA)
    } else {
        Color.White.copy(alpha = OPTION_BORDER_ALPHA)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(OptionHeight)
            .clip(OptionShape)
            .background(Brush.verticalGradient(listOf(PoseRaisedTop, PoseRaisedBottom)))
            .border(1.dp, borderColor, OptionShape)
            .click(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(option.labelRes),
            style = poseTextStyle(13.5.sp, FontWeight.Bold, Color.White),
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check_mark),
                contentDescription = null,
                tint = PosePink,
                modifier = Modifier.size(CheckSize),
            )
        }
    }
}
