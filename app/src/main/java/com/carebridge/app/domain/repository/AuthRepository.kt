package com.carebridge.app.domain.repository

import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val isUserLoggedIn: Boolean
    
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: UserRole
    ): Resource<User>
    
    suspend fun sendEmailVerification(): Resource<Unit>
    suspend fun reloadUser(): Resource<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    suspend fun getCurrentUserProfile(): Resource<User?>
    fun observeCurrentUserProfile(): Flow<Resource<User?>>
    suspend fun updateUserProfile(user: User): Resource<Unit>
    fun logout()
}
