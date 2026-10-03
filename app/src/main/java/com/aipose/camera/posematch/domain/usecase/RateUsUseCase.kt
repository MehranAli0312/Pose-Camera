package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.RateUsOutcome
import com.aipose.camera.posematch.domain.repo.RateUsRepository

private const val MAX_PROMPTS = 3
private const val MIN_CLEANED_ITEMS = 3
private const val MIN_FREED_BYTES = 10L * 1024 * 1024
private const val POSITIVE_RATING = 4f

class RateUsUseCase(private val rateUsRepository: RateUsRepository) {

    suspend fun shouldPrompt(cleanedItems: Int, freedBytes: Long): Boolean {
        if (!isSatisfyingResult(cleanedItems, freedBytes)) return false
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

    private fun isSatisfyingResult(cleanedItems: Int, freedBytes: Long): Boolean =
        cleanedItems >= MIN_CLEANED_ITEMS || freedBytes >= MIN_FREED_BYTES
}
