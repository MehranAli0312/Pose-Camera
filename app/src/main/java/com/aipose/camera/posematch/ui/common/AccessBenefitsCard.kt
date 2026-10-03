package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.models.AccessBenefit
import com.aipose.camera.posematch.ui.theme.AppTheme

private val CardShape = RoundedCornerShape(24.dp)
private val TagShape = RoundedCornerShape(8.dp)

@Composable
fun AccessBenefitsCard(
    benefits: List<AccessBenefit>,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.extendedColors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = CardShape,
                ambientColor = colors.contactShadow,
                spotColor = colors.contactShadow,
            )
            .clip(CardShape)
            .background(colors.glassSurface)
            .border(1.dp, colors.glassBorder, CardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        benefits.forEach { benefit ->
            AccessBenefitRow(benefit = benefit, trailingContent = trailingContent)
        }
    }
}

@Composable
private fun AccessBenefitRow(
    benefit: AccessBenefit,
    trailingContent: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon3D(
            iconRes = benefit.iconRes,
            palette = benefit.palette,
            size = 40.dp,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f, fill = trailingContent != null),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(benefit.titleRes),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                benefit.tagRes?.let { tagRes ->
                    Spacer(modifier = Modifier.width(6.dp))
                    BenefitTag(text = stringResource(tagRes))
                }
            }
            benefit.bodyRes?.let { bodyRes ->
                Text(
                    text = stringResource(bodyRes),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailingContent?.let { trailing ->
            Spacer(modifier = Modifier.width(10.dp))
            trailing()
        }
    }
}

@Composable
private fun BenefitTag(text: String) {
    val colors = AppTheme.extendedColors
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = colors.onSoftAccent,
        ),
        maxLines = 1,
        modifier = Modifier
            .clip(TagShape)
            .background(colors.softAccent)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}
