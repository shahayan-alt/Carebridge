package com.carebridge.app.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.carebridge.app.CareBridgeApp
import com.carebridge.app.data.model.CaregiverProfileDto
import com.carebridge.app.data.model.toCaregiverProfileDomain
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.domain.repository.CaregiverRepository
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class CaregiverRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : CaregiverRepository {

    override suspend fun getCaregiverProfile(caregiverId: String): Resource<CaregiverProfile?> {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES)
                .document(caregiverId).get().await()
            Resource.Success(doc.toCaregiverProfileDomain())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch caregiver profile", e)
        }
    }

    override fun observeCaregiverProfile(caregiverId: String): Flow<Resource<CaregiverProfile?>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES)
            .document(caregiverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading caregiver profile", error))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snapshot?.toCaregiverProfileDomain()))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateCaregiverProfile(profile: CaregiverProfile): Resource<Unit> {
        return try {
            val dto = CaregiverProfileDto.fromDomain(profile)
            firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES)
                .document(profile.caregiverId)
                .set(dto)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile", e)
        }
    }

    override suspend fun uploadKycDocument(caregiverId: String, fileUriString: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(fileUriString)
                val context = CareBridgeApp.appContext

                // Read and compress image to a compact JPEG string (100% Spark free tier compatible)
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Resource.Error("Unable to open image file")
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (originalBitmap == null) {
                    return@withContext Resource.Error("Could not decode image")
                }

                // Scale down bitmap to max 800px width/height to keep document compact (~40-70KB)
                val maxDimension = 800
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scale = if (width > height) {
                    maxDimension.toFloat() / width
                } else {
                    maxDimension.toFloat() / height
                }.coerceAtMost(1.0f)

                val scaledBitmap = if (scale < 1.0f) {
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (width * scale).toInt(),
                        (height * scale).toInt(),
                        true
                    )
                } else {
                    originalBitmap
                }

                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64Data = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)

                // Save directly to Firestore profile (bypasses Blaze requirement entirely)
                firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES)
                    .document(caregiverId)
                    .update(
                        mapOf(
                            "kycDocumentUrl" to base64Data,
                            "verificationStatus" to VerificationStatus.PENDING.name
                        )
                    ).await()

                Resource.Success(base64Data)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "KYC document processing failed", e)
            }
        }
    }

    override fun searchCaregivers(
        query: String,
        skillFilter: String,
        maxHourlyRate: Double?,
        onlyVerified: Boolean
    ): Flow<Resource<List<CaregiverProfile>>> = callbackFlow {
        val baseQuery: Query = firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES)

        val listener = baseQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Error fetching caregivers", error))
                return@addSnapshotListener
            }
            if (snapshot == null) {
                trySend(Resource.Success(emptyList()))
                return@addSnapshotListener
            }

            var list = snapshot.documents.mapNotNull { it.toCaregiverProfileDomain() }

            // Apply client-side filters (to respect Firebase Spark tier index simplicity)
            if (onlyVerified) {
                list = list.filter { it.verificationStatus == VerificationStatus.VERIFIED }
            }
            if (skillFilter.isNotBlank()) {
                list = list.filter { it.skills.any { s -> s.contains(skillFilter, ignoreCase = true) } }
            }
            if (maxHourlyRate != null && maxHourlyRate > 0) {
                list = list.filter { it.hourlyRate <= maxHourlyRate }
            }
            if (query.isNotBlank()) {
                val q = query.lowercase().trim()
                list = list.filter {
                    it.fullName.lowercase().contains(q) ||
                    it.location.address.lowercase().contains(q) ||
                    it.skills.any { s -> s.lowercase().contains(q) }
                }
            }

            trySend(Resource.Success(list))
        }

        awaitClose { listener.remove() }
    }
}
