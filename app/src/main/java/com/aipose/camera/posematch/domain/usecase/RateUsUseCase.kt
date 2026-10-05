package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.domain.models.RateUsOutcome
import com.aipose.camera.posematch.domain.repo.RateUsRepository

private const val MAX_PROMPTS = 3
private const val MIN_SAVED_SHOTS = 3
private const val POSITIVE_RATING = 4f

class RateUsUseCase(private val rateUsRepository: RateUsRepository) {

    suspend fun shouldPrompt(savedShotCount: Int, matchScore: Int): Boolean {
        if (!isSatisfyingResult(savedShotCount, matchScore)) return false
        if (rateUsRepository.isSubmitted()) return false
        return rateUsRepository.promptCount() < MAX_PROMPTS
    }

    suspend fun markPrompted() {
        rateUsRepository.incrementPromptCount()
    }

    suspend fun markSubmitted() {
        rateUsRepository.setSubmitted(true)
    }

    fun outcomeFor(rating: Float): RateUsOutcome = when {
        rating >= POSITIVE_RATING -> RateUsOutcome.OpenStore
        rating > 0f -> RateUsOutcome.SendFeedback
        else -> RateUsOutcome.NotRated
    }

    private fun isSatisfyingResult(savedShotCount: Int, matchScore: Int): Boolean =
        savedShotCount >= MIN_SAVED_SHOTS && matchScore >= PERFECT_MATCH_SCORE
}
