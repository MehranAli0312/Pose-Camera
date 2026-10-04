package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private val SheetShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsThemeSheet(
    selected: AppThemeOption,
    onSelect: (AppThemeOption) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = LocalAppPalette.current.card,
        shape = SheetShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            ThemeModeSelector(
                selectedOption = selected,
                onThemeSelected = onSelect,
            )
        }
    }
}
