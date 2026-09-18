package com.carebridge.app.data.repository

import com.carebridge.app.data.model.UserDto
import com.carebridge.app.data.model.toUserDomain
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.domain.repository.AuthRepository
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Resource.Error("User ID not found")
            
            var user: User? = null
            try {
                val userDoc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
                user = userDoc.toUserDomain()
            } catch (_: Exception) {}

            val finalUser = user ?: User(
                userId = uid,
                email = email,
                fullName = authResult.user?.displayName?.ifBlank { null } ?: email.substringBefore("@"),
                isEmailVerified = auth.currentUser?.isEmailVerified == true,
                role = UserRole.FAMILY
            )

            val isVerified = auth.currentUser?.isEmailVerified == true
            if (finalUser.isEmailVerified != isVerified) {
                try {
                    firestore.collection(Constants.COLLECTION_USERS).document(uid).update("isEmailVerified", isVerified).await()
                } catch (_: Exception) {}
            }
            
            Resource.Success(finalUser.copy(isEmailVerified = isVerified))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Login failed", e)
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: UserRole
    ): Resource<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Resource.Error("Failed to generate user ID")
            
            try {
                authResult.user?.sendEmailVerification()?.await()
            } catch (_: Exception) {}

            val userDto = UserDto(
                userId = uid,
                email = email,
                fullName = fullName,
                phoneNumber = phoneNumber,
                role = role.name,
                isEmailVerified = false,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )

            try {
                firestore.collection(Constants.COLLECTION_USERS).document(uid).set(userDto).await()
            } catch (e: Exception) {
                return Resource.Error("Account created in Auth, but Firestore rules denied write: ${e.localizedMessage}", e)
            }

            if (role == UserRole.CAREGIVER) {
                try {
                    val caregiverDto = mapOf(
                        "caregiverId" to uid,
                        "fullName" to fullName,
                        "hourlyRate" to 150.0,
                        "experienceYears" to 1.0,
                        "bio" to "Compassionate caregiver ready to support your family.",
                        "skills" to listOf("Companionship", "Mobility Assistance"),
                        "verificationStatus" to "PENDING",
                        "averageRating" to 0.0,
                        "totalReviews" to 0
                    )
                    firestore.collection(Constants.COLLECTION_CAREGIVER_PROFILES).document(uid).set(caregiverDto).await()
                } catch (_: Exception) {}
            }

            Resource.Success(userDto.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Registration failed", e)
        }
    }

    override suspend fun sendEmailVerification(): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: return Resource.Error("No user signed in")
            user.sendEmailVerification().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send verification email", e)
        }
    }

    override suspend fun reloadUser(): Resource<Boolean> {
        return try {
            val user = auth.currentUser ?: return Resource.Error("No user signed in")
            user.reload().await()
            val isVerified = user.isEmailVerified
            
            try {
                firestore.collection(Constants.COLLECTION_USERS).document(user.uid)
                    .update("isEmailVerified", isVerified).await()
            } catch (_: Exception) {}

            Resource.Success(isVerified)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to check email verification", e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send password reset email", e)
        }
    }

    override suspend fun getCurrentUserProfile(): Resource<User?> {
        return try {
            val uid = currentUserId ?: return Resource.Success(null)
            val doc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            Resource.Success(doc.toUserDomain())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch user profile", e)
        }
    }

    override fun observeCurrentUserProfile(): Flow<Resource<User?>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(Resource.Success(null))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(Constants.COLLECTION_USERS).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading user profile", error))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snapshot?.toUserDomain()))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateUserProfile(user: User): Resource<Unit> {
        return try {
            val dto = UserDto.fromDomain(user)
            firestore.collection(Constants.COLLECTION_USERS).document(user.userId).set(dto).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile", e)
        }
    }

    override fun logout() {
        auth.signOut()
    }
}
