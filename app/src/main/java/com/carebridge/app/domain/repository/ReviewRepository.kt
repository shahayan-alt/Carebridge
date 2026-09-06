package com.carebridge.app.domain.repository

import com.carebridge.app.domain.model.Review
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    suspend fun submitReview(review: Review): Resource<Unit>
    fun observeCaregiverReviews(caregiverId: String): Flow<Resource<List<Review>>>
}
