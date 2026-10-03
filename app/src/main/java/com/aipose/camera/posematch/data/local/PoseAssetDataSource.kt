package com.aipose.camera.posematch.data.local

import android.content.Context
import com.aipose.camera.posematch.data.local.dto.PoseTemplateDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class PoseAssetDataSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun readTemplates(): List<PoseTemplateDto> = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(TEMPLATES_FILE).bufferedReader().use { it.readText() }
        }.mapCatching { json.decodeFromString<List<PoseTemplateDto>>(it) }
            .getOrDefault(emptyList())
    }

    private companion object {
        const val TEMPLATES_FILE = "default_poses.json"
    }
}
