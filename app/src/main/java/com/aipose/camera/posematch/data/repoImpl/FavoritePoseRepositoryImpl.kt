package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.repo.FavoritePoseRepository
import kotlinx.coroutines.flow.Flow

class FavoritePoseRepositoryImpl(
    private val appDataStore: AppDataStore
) : FavoritePoseRepository {

    override fun observeFavorites(): Flow<Map<Int, Long>> = appDataStore.getFavoritePoses()

    override suspend fun setFavorite(poseId: Int, isFavorite: Boolean) {
        appDataStore.setFavoritePose(poseId, isFavorite)
    }
}
