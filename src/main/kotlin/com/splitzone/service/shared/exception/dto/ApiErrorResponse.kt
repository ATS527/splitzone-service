package com.splitzone.service.shared.exception.dto

import java.time.Instant

data class ApiErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val code: String,
    val message: String,
    val path: String,
    val requestId: String,
    val fieldErrors: List<FieldValidationError> = emptyList()
)

data class FieldValidationError(
    val field: String,
    val message: String
)
