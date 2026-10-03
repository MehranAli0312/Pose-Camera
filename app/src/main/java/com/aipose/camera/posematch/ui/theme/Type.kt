package com.aipose.camera.posematch.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R

val appFontFamily = FontFamily(
    fonts = listOf(
        Font(R.font.inter, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold),
        Font(R.font.inter_black, FontWeight.Black)
    )
)

private fun appTextStyle(
    weight: FontWeight,
    size: androidx.compose.ui.unit.TextUnit,
    color: androidx.compose.ui.graphics.Color,
) = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = weight,
    fontSize = size,
    color = color,
)

val Typography = Typography(
    headlineLarge = appTextStyle(FontWeight.Black, 32.sp, TextColor),
    headlineMedium = appTextStyle(FontWeight.ExtraBold, 28.sp, TextColor),
    headlineSmall = appTextStyle(FontWeight.Bold, 24.sp, TextColor),
    titleLarge = appTextStyle(FontWeight.Bold, 22.sp, TextColor),
    titleMedium = appTextStyle(FontWeight.Bold, 16.sp, TextColor),
    titleSmall = appTextStyle(FontWeight.SemiBold, 14.sp, TextColor),
    bodyLarge = appTextStyle(FontWeight.Normal, 16.sp, TextColor),
    bodyMedium = appTextStyle(FontWeight.Normal, 14.sp, TextColor),
    bodySmall = appTextStyle(FontWeight.Normal, 12.sp, TextColor),
    labelLarge = appTextStyle(FontWeight.Medium, 14.sp, TextColor),
    labelMedium = appTextStyle(FontWeight.Normal, 12.sp, TextColor),
    labelSmall = appTextStyle(FontWeight.Light, 11.sp, TextColor),
)

val TypographyDark = Typography(
    headlineLarge = appTextStyle(FontWeight.Black, 32.sp, TextColorDark),
    headlineMedium = appTextStyle(FontWeight.ExtraBold, 28.sp, TextColorDark),
    headlineSmall = appTextStyle(FontWeight.Bold, 24.sp, TextColorDark),
    titleLarge = appTextStyle(FontWeight.Bold, 22.sp, TextColorDark),
    titleMedium = appTextStyle(FontWeight.Bold, 16.sp, TextColorDark),
    titleSmall = appTextStyle(FontWeight.SemiBold, 14.sp, TextColorDark),
    bodyLarge = appTextStyle(FontWeight.Normal, 16.sp, TextColorDark),
    bodyMedium = appTextStyle(FontWeight.Normal, 14.sp, TextColorDark),
    bodySmall = appTextStyle(FontWeight.Normal, 12.sp, TextColorDark),
    labelLarge = appTextStyle(FontWeight.Medium, 14.sp, TextColorDark),
    labelMedium = appTextStyle(FontWeight.Normal, 12.sp, TextColorDark),
    labelSmall = appTextStyle(FontWeight.Light, 11.sp, TextColorDark),
)
