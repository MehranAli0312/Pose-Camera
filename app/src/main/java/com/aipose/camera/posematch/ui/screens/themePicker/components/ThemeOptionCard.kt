package com.aipose.camera.posematch.ui.screens.themePicker.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.models.themeOptionLabel
import com.aipose.camera.posematch.ui.screens.themePicker.models.themePreviewStyle
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardShape = RoundedCornerShape(24.dp)
private val CardMinHeight = 120.dp
private val SelectedBorder = 1.6.dp
private val RadioSize = 26.dp
private val RadioStroke = 2.dp
private val CheckSize = 12.dp
private val ChipShape = RoundedCornerShape(10.dp)
private const val SELECTED_FILL_ALPHA = 0.12f
private const val CARD_BORDER_ALPHA = 0.09f
private const val RADIO_BORDER_ALPHA = 0.25f

@Composable
internal fun ThemeOptionCard(
    option: AppThemeOption,
    isSelected: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = themePreviewStyle(option)
    val surface = if (isSelected) {
        Modifier
            .clip(CardShape)
            .background(Violet.copy(alpha = SELECTED_FILL_ALPHA))
            .border(SelectedBorder, Violet, CardShape)
    } else {
        Modifier.poseRaisedSurface(CardShape, borderAlpha = CARD_BORDER_ALPHA)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardMinHeight)
            .then(surface)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
            }
            .bounceClick(onClick = onClick)
            .padding(start = 16.dp, end = 21.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ThemePreviewTile(style = style)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(themeOptionLabel(option)),
                style = poseTextStyle(15.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(style.descriptionRes),
                style = poseTextStyle(10.5.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (isCurrent) {
                Text(
                    text = stringResource(R.string.theme_current),
                    style = poseTextStyle(9.sp, FontWeight.Bold, Color.White),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(ChipShape)
                        .background(Violet)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                )
            }
        }
        ThemeRadio(isSelected = isSelected)
    }
}

@Composable
private fun ThemeRadio(isSelected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(RadioSize)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.background(Violet)
                } else {
                    Modifier.border(RadioStroke, Color.White.copy(alpha = RADIO_BORDER_ALPHA), CircleShape)
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Image(
                painter = painterResource(R.drawable.ic_pose_check_small),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(CheckSize),
            )
        }
    }
}
