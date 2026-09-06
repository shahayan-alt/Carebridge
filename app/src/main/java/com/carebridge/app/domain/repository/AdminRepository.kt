package com.carebridge.app.domain.repository

import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observePendingCaregivers(): Flow<Resource<List<CaregiverProfile>>>
    fun observeAllUsers(): Flow<Resource<List<User>>>
    suspend fun updateCaregiverVerification(caregiverId: String, status: VerificationStatus): Resource<Unit>
    suspend fun updateUserActiveState(userId: String, isActive: Boolean): Resource<Unit>
}
