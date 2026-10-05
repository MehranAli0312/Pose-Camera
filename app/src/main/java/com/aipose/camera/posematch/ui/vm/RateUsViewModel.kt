package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.RateUsOutcome
import com.aipose.camera.posematch.domain.usecase.RateUsUseCase
import kotlinx.coroutines.launch

class RateUsViewModel(private val rateUsUseCase: RateUsUseCase) : ViewModel() {

    suspend fun shouldPrompt(savedShotCount: Int, matchScore: Int): Boolean =
        rateUsUseCase.shouldPrompt(savedShotCount, matchScore)

    fun markPrompted() {
        viewModelScope.launch { rateUsUseCase.markPrompted() }
    }

    fun submitRating(rating: Float): RateUsOutcome {
        val outcome = rateUsUseCase.outcomeFor(rating)
        if (outcome != RateUsOutcome.NotRated) {
            viewModelScope.launch { rateUsUseCase.markSubmitted() }
        }
        return outcome
    }
}
