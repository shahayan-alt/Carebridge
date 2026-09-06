package com.carebridge.app.domain.usecase

import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.domain.repository.AdminRepository
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

class ObservePendingCaregiversUseCase(private val repository: AdminRepository) {
    operator fun invoke(): Flow<Resource<List<CaregiverProfile>>> = repository.observePendingCaregivers()
}

class ObserveAllUsersUseCase(private val repository: AdminRepository) {
    operator fun invoke(): Flow<Resource<List<User>>> = repository.observeAllUsers()
}

class UpdateCaregiverVerificationUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(caregiverId: String, status: VerificationStatus): Resource<Unit> =
        repository.updateCaregiverVerification(caregiverId, status)
}

class UpdateUserActiveStateUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(userId: String, isActive: Boolean): Resource<Unit> =
        repository.updateUserActiveState(userId, isActive)
}
