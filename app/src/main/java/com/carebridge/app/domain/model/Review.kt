package com.carebridge.app.domain.model

data class Review(
    val reviewId: String = "",
    val bookingId: String = "",
    val familyId: String = "",
    val familyName: String = "",
    val caregiverId: String = "",
    val rating: Double = 5.0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
