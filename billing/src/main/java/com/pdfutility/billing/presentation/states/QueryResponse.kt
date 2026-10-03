package com.pdfutility.billing.presentation.states

sealed class QueryResponse<out T> {
    object Loading : QueryResponse<Nothing>()
    data class Success<out T>(val data: T) : QueryResponse<T>()
    data class Error(val errorMessage: String) : QueryResponse<Nothing>()
}