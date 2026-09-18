package com.carebridge.app.data.repository

import com.carebridge.app.data.model.toCaregiverProfileDomain
import com.carebridge.app.data.model.toUserDomain
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.domain.repository.AdminRepository
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepositoryImpl(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override fun observePendingCaregivers(): Flow<Resource<List<CaregiverProfile>>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES)
            .whereEqualTo("verificationStatus", VerificationStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading pending applications", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toCaregiverProfileDomain() } ?: emptyList()
                trySend(Resource.Success(list))
            }

        awaitClose { listener.remove() }
    }

    override fun observeAllUsers(): Flow<Resource<List<User>>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_USERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading users", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toUserDomain() } ?: emptyList()
                trySend(Resource.Success(list))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateCaregiverVerification(
        caregiverId: String,
        status: VerificationStatus
    ): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES)
                .document(caregiverId)
                .update("verificationStatus", status.name)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update verification status", e)
        }
    }

    override suspend fun updateUserActiveState(userId: String, isActive: Boolean): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("isActive", isActive)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update user status", e)
        }
    }
}
