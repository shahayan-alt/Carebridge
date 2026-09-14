package com.carebridge.app.data.model

import com.carebridge.app.domain.model.Review
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ReviewDto(
    @DocumentId
    val reviewId: String = "",
    val bookingId: String = "",
    val familyId: String = "",
    val familyName: String = "",
    val caregiverId: String = "",
    val rating: Double = 5.0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Review(
        reviewId = reviewId,
        bookingId = bookingId,
        familyId = familyId,
        familyName = familyName,
        caregiverId = caregiverId,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(r: Review) = ReviewDto(
            reviewId = r.reviewId,
            bookingId = r.bookingId,
            familyId = r.familyId,
            familyName = r.familyName,
            caregiverId = r.caregiverId,
            rating = r.rating,
            comment = r.comment,
            createdAt = r.createdAt
        )
    }
}
