package com.carebridge.app.domain.usecase

import com.carebridge.app.domain.model.Review
import com.carebridge.app.domain.repository.ReviewRepository
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

class SubmitReviewUseCase(private val repository: ReviewRepository) {
    suspend operator fun invoke(review: Review): Resource<Unit> {
        if (review.caregiverId.isBlank() || review.familyId.isBlank()) {
            return Resource.Error("Invalid review references")
        }
        if (review.rating !in 1.0..5.0) {
            return Resource.Error("Rating must be between 1 and 5")
        }
        return repository.submitReview(review)
    }
}

class ObserveCaregiverReviewsUseCase(private val repository: ReviewRepository) {
    operator fun invoke(caregiverId: String): Flow<Resource<List<Review>>> =
        repository.observeCaregiverReviews(caregiverId)
}
