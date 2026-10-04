package com.aipose.camera.posematch.ui.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.theme.Emerald as EmeraldColor
import com.aipose.camera.posematch.ui.theme.Indigo as IndigoColor
import com.aipose.camera.posematch.ui.theme.Orange as OrangeColor
import com.aipose.camera.posematch.ui.theme.Violet as VioletColor
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PoseBlue
import com.aipose.camera.posematch.ui.theme.PoseBlueDeep
import com.aipose.camera.posematch.ui.theme.PoseBlueLight
import com.aipose.camera.posematch.ui.theme.PoseBlueShadow
import com.aipose.camera.posematch.ui.theme.PoseAmberDeep
import com.aipose.camera.posematch.ui.theme.PoseAmberLight
import com.aipose.camera.posematch.ui.theme.PoseAmberPale
import com.aipose.camera.posematch.ui.theme.PoseAmberShadow
import com.aipose.camera.posematch.ui.theme.PoseCyan
import com.aipose.camera.posematch.ui.theme.PoseCyanDeep
import com.aipose.camera.posematch.ui.theme.PoseCyanLight
import com.aipose.camera.posematch.ui.theme.PoseCyanShadow
import com.aipose.camera.posematch.ui.theme.PoseEmeraldDeep
import com.aipose.camera.posematch.ui.theme.PoseEmeraldLight
import com.aipose.camera.posematch.ui.theme.PoseEmeraldShadow
import com.aipose.camera.posematch.ui.theme.PoseIndigoDeep
import com.aipose.camera.posematch.ui.theme.PoseIndigoLight
import com.aipose.camera.posematch.ui.theme.PoseIndigoShadow
import com.aipose.camera.posematch.ui.theme.PoseMagenta
import com.aipose.camera.posematch.ui.theme.PoseMagentaShadow
import com.aipose.camera.posematch.ui.theme.PoseNavActive
import com.aipose.camera.posematch.ui.theme.PoseOrangeDeep
import com.aipose.camera.posematch.ui.theme.PoseOrangeLight
import com.aipose.camera.posematch.ui.theme.PoseOrangeShadow
import com.aipose.camera.posematch.ui.theme.PosePink
import com.aipose.camera.posematch.ui.theme.PosePinkBright
import com.aipose.camera.posematch.ui.theme.PosePinkDeep
import com.aipose.camera.posematch.ui.theme.PosePinkLight
import com.aipose.camera.posematch.ui.theme.PosePinkShadow
import com.aipose.camera.posematch.ui.theme.PoseRose
import com.aipose.camera.posematch.ui.theme.PoseRoseDeep
import com.aipose.camera.posematch.ui.theme.PoseRoseLight
import com.aipose.camera.posematch.ui.theme.PoseRoseShadow
import com.aipose.camera.posematch.ui.theme.PoseSky
import com.aipose.camera.posematch.ui.theme.PoseSkyDeep
import com.aipose.camera.posematch.ui.theme.PoseSkyLight
import com.aipose.camera.posematch.ui.theme.PoseSkyShadow
import com.aipose.camera.posematch.ui.theme.PoseVioletDeep
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.PoseVioletShadow

@Immutable
data class GlossyBadgePalette(
    val top: Color,
    val mid: Color,
    val bottom: Color,
    val shadow: Color,
) {
    companion object {
        val Indigo = GlossyBadgePalette(PoseIndigoLight, IndigoColor, PoseIndigoDeep, PoseIndigoShadow)
        val Orange = GlossyBadgePalette(PoseOrangeLight, OrangeColor, PoseOrangeDeep, PoseOrangeShadow)
        val Rose = GlossyBadgePalette(PoseRoseLight, PoseRose, PoseRoseDeep, PoseRoseShadow)
        val Pink = GlossyBadgePalette(PosePinkLight, PosePink, PosePinkDeep, PosePinkShadow)
        val Amber = GlossyBadgePalette(PoseAmberLight, PoseAmber, PoseAmberDeep, PoseAmberShadow)
        val Bulb = GlossyBadgePalette(PoseAmberPale, PoseAmber, PoseAmberDeep, PoseAmberShadow)
        val Emerald = GlossyBadgePalette(PoseEmeraldLight, EmeraldColor, PoseEmeraldDeep, PoseEmeraldShadow)
        val Sky = GlossyBadgePalette(PoseSkyLight, PoseSky, PoseSkyDeep, PoseSkyShadow)
        val Violet = GlossyBadgePalette(PoseVioletPale, VioletColor, PoseVioletDeep, PoseVioletShadow)
        val Cyan = GlossyBadgePalette(PoseCyanLight, PoseCyan, PoseCyanDeep, PoseCyanShadow)
        val Blue = GlossyBadgePalette(PoseBlueLight, PoseBlue, PoseBlueDeep, PoseBlueShadow)
        val Brand = GlossyBadgePalette(PoseVioletLight, IndigoColor, PoseIndigoDeep, PoseIndigoShadow)
        val Shuffle = GlossyBadgePalette(PoseIndigoLight, IndigoColor, PoseVioletDeep, PoseIndigoShadow)
        val HeroCta = GlossyBadgePalette(IndigoColor, VioletColor, PoseMagenta, PoseVioletShadow)
        val Capture = GlossyBadgePalette(PoseVioletLight, VioletColor, PosePinkBright, PoseMagentaShadow)
        val NavCapture = GlossyBadgePalette(PoseNavActive, VioletColor, PosePinkBright, PoseVioletShadow)
    }
}
