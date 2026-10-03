package com.aipose.camera.posematch.domain.repo

interface RateUsRepository {
    suspend fun isSubmitted(): Boolean
    suspend fun setSubmitted(submitted: Boolean)
    suspend fun promptCount(): Int
    suspend fun incrementPromptCount()
}
