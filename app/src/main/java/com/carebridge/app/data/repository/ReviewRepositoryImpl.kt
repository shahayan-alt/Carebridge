package com.carebridge.app.data.repository

import com.carebridge.app.data.model.ReviewDto
import com.carebridge.app.data.model.toReviewDomain
import com.carebridge.app.domain.model.Review
import com.carebridge.app.domain.repository.ReviewRepository
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.math.RoundingMode

class ReviewRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    override suspend fun submitReview(review: Review): Resource<Unit> {
        return try {
            val docRef = firestore.collection(Constants.COLLECTION_REVIEWS).document()
            val finalReview = review.copy(reviewId = docRef.id)
            val dto = ReviewDto.fromDomain(finalReview)
            docRef.set(dto).await()

            // Update caregiver's aggregated rating on client-side (Spark tier constraint)
            val reviewsSnapshot = firestore.collection(Constants.COLLECTION_REVIEWS)
                .whereEqualTo("caregiverId", review.caregiverId)
                .get()
                .await()

            val reviews = reviewsSnapshot.documents.mapNotNull { it.toReviewDomain() }
            val totalCount = reviews.size
            val avg = if (totalCount > 0) reviews.map { it.rating }.average() else review.rating
            val roundedAvg = BigDecimal(avg).setScale(1, RoundingMode.HALF_UP).toDouble()

            firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES)
                .document(review.caregiverId)
                .update(
                    mapOf(
                        "averageRating" to roundedAvg,
                        "totalReviews" to totalCount
                    )
                ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to submit review", e)
        }
    }

    override fun observeCaregiverReviews(caregiverId: String): Flow<Resource<List<Review>>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_REVIEWS)
            .whereEqualTo("caregiverId", caregiverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading reviews", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toReviewDomain() }
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(Resource.Success(list))
            }

        awaitClose { listener.remove() }
    }
}
