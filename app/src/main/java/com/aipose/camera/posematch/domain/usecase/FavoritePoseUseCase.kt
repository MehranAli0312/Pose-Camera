package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.repo.FavoritePoseRepository
import kotlinx.coroutines.flow.Flow

class FavoritePoseUseCase(private val favoritePoseRepository: FavoritePoseRepository) {

    fun observeFavorites(): Flow<Map<Int, Long>> = favoritePoseRepository.observeFavorites()

    suspend fun setFavorite(poseId: Int, isFavorite: Boolean) {
        favoritePoseRepository.setFavorite(poseId, isFavorite)
    }
}
