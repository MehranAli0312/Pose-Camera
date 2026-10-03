package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.theme.AppTheme
import com.aipose.camera.posematch.ui.theme.Icon3DIndigoEnd
import com.aipose.camera.posematch.ui.theme.Icon3DVioletEnd

private val CardShape = RoundedCornerShape(24.dp)
private val BadgeShape = RoundedCornerShape(14.dp)
private val ButtonShape = RoundedCornerShape(16.dp)
private val CardGradient = listOf(Icon3DVioletEnd, Icon3DIndigoEnd)
private const val SECONDARY_TEXT_ALPHA = 0.85f
private const val BADGE_ALPHA = 0.18f
private const val LARGE_BUBBLE_ALPHA = 0.08f
private const val SMALL_BUBBLE_ALPHA = 0.07f

@Composable
fun InsightGradientCard(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    amount: String,
    amountCaption: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = CardShape,
                ambientColor = AppTheme.extendedColors.contactShadow,
                spotColor = AppTheme.extendedColors.contactShadow,
            )
            .clip(CardShape)
            .background(Brush.linearGradient(CardGradient))
            .bounceClick(onClick = onClick),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val endX = if (isRtl) 0f else size.width
            drawCircle(
                color = Color.White.copy(alpha = LARGE_BUBBLE_ALPHA),
                radius = 46.dp.toPx(),
                center = Offset(
                    if (isRtl) endX + 26.dp.toPx() else endX - 26.dp.toPx(),
                    14.dp.toPx()
                ),
            )
            drawCircle(
                color = Color.White.copy(alpha = SMALL_BUBBLE_ALPHA),
                radius = 34.dp.toPx(),
                center = Offset(endX, size.height - 8.dp.toPx()),
            )
        }
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(BadgeShape)
                    .background(Color.White.copy(alpha = BADGE_ALPHA)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = subtitle,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .basicMarquee(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = SECONDARY_TEXT_ALPHA),
                    ),
                    maxLines = 2,
                )
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                            ),
                            maxLines = 1,
                        )
                        Text(
                            text = amountCaption,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = SECONDARY_TEXT_ALPHA),
                            ),
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = actionLabel,
                        modifier = Modifier
                            .clip(ButtonShape)
                            .background(Color.White)
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Icon3DVioletEnd,
                        ),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
