package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val FieldHeight = 52.dp
private val FieldCorner = 18.dp
private val FieldShape = RoundedCornerShape(FieldCorner)
private val SearchGlyphSize = 20.dp
private val ClearGlyphSize = 14.dp
private val ShadowOffsetY = 6.dp
private val ShadowInsetX = 2.dp
private const val FIELD_BORDER_ALPHA = 0.1f
private const val SHADOW_ALPHA = 0.45f

@Composable
fun StudioSearchField(
    query: String,
    hint: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    val textStyle = poseTextStyle(12.5.sp, FontWeight.Normal, Color.White)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(palette.accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchSubmitted() }),
        modifier = modifier
            .fillMaxWidth()
            .height(FieldHeight)
            .drawBehind {
                val insetX = ShadowInsetX.toPx()
                drawRoundRect(
                    color = PoseShadow.copy(alpha = SHADOW_ALPHA),
                    topLeft = Offset(insetX, ShadowOffsetY.toPx()),
                    size = size.copy(width = size.width - insetX * 2f),
                    cornerRadius = CornerRadius(FieldCorner.toPx()),
                )
            }
            .poseRaisedSurface(FieldShape, borderAlpha = FIELD_BORDER_ALPHA),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 17.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(palette.textFaint),
                    modifier = Modifier.size(SearchGlyphSize),
                )
                Spacer(modifier = Modifier.width(13.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = hint,
                            style = textStyle.copy(color = palette.textFaint),
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .click { onQueryChange("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.action_clear),
                            colorFilter = ColorFilter.tint(palette.textFaint),
                            modifier = Modifier.size(ClearGlyphSize),
                        )
                    }
                }
            }
        },
    )
}
