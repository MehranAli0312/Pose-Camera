package com.aipose.camera.posematch.ui.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.theme.AppMainColor
import com.aipose.camera.posematch.ui.theme.AppSecondaryColor
import com.aipose.camera.posematch.ui.theme.BlueLight
import com.aipose.camera.posematch.ui.theme.FixStepDepth
import com.aipose.camera.posematch.ui.theme.Green
import com.aipose.camera.posematch.ui.theme.GreenLight
import com.aipose.camera.posematch.ui.theme.Icon3DAmberDepth
import com.aipose.camera.posematch.ui.theme.Icon3DAmberEnd
import com.aipose.camera.posematch.ui.theme.Icon3DAmberShade
import com.aipose.camera.posematch.ui.theme.Icon3DAmberStart
import com.aipose.camera.posematch.ui.theme.Icon3DCoralDepth
import com.aipose.camera.posematch.ui.theme.Icon3DCoralShade
import com.aipose.camera.posematch.ui.theme.Icon3DCoralStart
import com.aipose.camera.posematch.ui.theme.Icon3DIndigoDepth
import com.aipose.camera.posematch.ui.theme.Icon3DIndigoEnd
import com.aipose.camera.posematch.ui.theme.Icon3DIndigoShade
import com.aipose.camera.posematch.ui.theme.Icon3DIndigoStart
import com.aipose.camera.posematch.ui.theme.Icon3DMintDepth
import com.aipose.camera.posematch.ui.theme.Icon3DMintShade
import com.aipose.camera.posematch.ui.theme.Icon3DOceanShade
import com.aipose.camera.posematch.ui.theme.Icon3DRoseDepth
import com.aipose.camera.posematch.ui.theme.Icon3DRoseEnd
import com.aipose.camera.posematch.ui.theme.Icon3DRoseShade
import com.aipose.camera.posematch.ui.theme.Icon3DRoseStart
import com.aipose.camera.posematch.ui.theme.Icon3DSkyDepth
import com.aipose.camera.posematch.ui.theme.Icon3DSkyEnd
import com.aipose.camera.posematch.ui.theme.Icon3DSkyShade
import com.aipose.camera.posematch.ui.theme.Icon3DSkyStart
import com.aipose.camera.posematch.ui.theme.Icon3DTealDepth
import com.aipose.camera.posematch.ui.theme.Icon3DTealEnd
import com.aipose.camera.posematch.ui.theme.Icon3DTealShade
import com.aipose.camera.posematch.ui.theme.Icon3DTealStart
import com.aipose.camera.posematch.ui.theme.Icon3DVioletDepth
import com.aipose.camera.posematch.ui.theme.Icon3DVioletEnd
import com.aipose.camera.posematch.ui.theme.Icon3DVioletShade
import com.aipose.camera.posematch.ui.theme.Icon3DVioletStart
import com.aipose.camera.posematch.ui.theme.Orange
import com.aipose.camera.posematch.ui.theme.ShieldCheckShadow
import com.aipose.camera.posematch.ui.theme.SoftAccentDark

@Immutable
data class Icon3DPalette(
    val start: Color,
    val end: Color,
    val depth: Color,
    val glyphShade: Color,
) {
    companion object {
        val Sky = Icon3DPalette(Icon3DSkyStart, Icon3DSkyEnd, Icon3DSkyDepth, Icon3DSkyShade)
        val Violet = Icon3DPalette(Icon3DVioletStart, Icon3DVioletEnd, Icon3DVioletDepth, Icon3DVioletShade)
        val Rose = Icon3DPalette(Icon3DRoseStart, Icon3DRoseEnd, Icon3DRoseDepth, Icon3DRoseShade)
        val Amber = Icon3DPalette(Icon3DAmberStart, Icon3DAmberEnd, Icon3DAmberDepth, Icon3DAmberShade)
        val Teal = Icon3DPalette(Icon3DTealStart, Icon3DTealEnd, Icon3DTealDepth, Icon3DTealShade)
        val Indigo = Icon3DPalette(Icon3DIndigoStart, Icon3DIndigoEnd, Icon3DIndigoDepth, Icon3DIndigoShade)
        val Ocean = Icon3DPalette(BlueLight, AppMainColor, SoftAccentDark, Icon3DOceanShade)
        val Mint = Icon3DPalette(GreenLight, Green, Icon3DMintDepth, Icon3DMintShade)
        val Coral = Icon3DPalette(Icon3DCoralStart, Orange, Icon3DCoralDepth, Icon3DCoralShade)
        val Brand = Icon3DPalette(AppMainColor, AppSecondaryColor, FixStepDepth, ShieldCheckShadow)
    }
}
