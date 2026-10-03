package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.ui.models.themeOptionLabel

@Composable
internal fun ThemeModeSelector(
    selectedOption: AppThemeOption,
    onThemeSelected: (AppThemeOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
            Text(
                text = stringResource(R.string.settings_theme_desc),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                ),
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            items(AppThemeOption.entries.size) { index ->
                val option = AppThemeOption.entries[index]
                ThemeOptionChip(
                    label = stringResource(themeOptionLabel(option)),
                    selected = option == selectedOption,
                    onClick = { onThemeSelected(option) },
                )
            }
        }
    }
}
