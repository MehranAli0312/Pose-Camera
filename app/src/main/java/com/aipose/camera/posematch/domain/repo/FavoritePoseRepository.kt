package com.aipose.camera.posematch.domain.repo

import kotlinx.coroutines.flow.Flow

interface FavoritePoseRepository {

    fun observeFavorites(): Flow<Map<Int, Long>>

    suspend fun setFavorite(poseId: Int, isFavorite: Boolean)
}
