package com.carebridge.app.domain.usecase

import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.repository.CaregiverRepository
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

class GetCaregiverProfileUseCase(private val repository: CaregiverRepository) {
    suspend operator fun invoke(caregiverId: String): Resource<CaregiverProfile?> =
        repository.getCaregiverProfile(caregiverId)

    fun observe(caregiverId: String): Flow<Resource<CaregiverProfile?>> =
        repository.observeCaregiverProfile(caregiverId)
}

class UpdateCaregiverProfileUseCase(private val repository: CaregiverRepository) {
    suspend operator fun invoke(profile: CaregiverProfile): Resource<Unit> {
        if (profile.caregiverId.isBlank()) return Resource.Error("Invalid caregiver ID")
        return repository.updateCaregiverProfile(profile)
    }
}

class SearchCaregiversUseCase(private val repository: CaregiverRepository) {
    operator fun invoke(
        query: String = "",
        skillFilter: String = "",
        maxHourlyRate: Double? = null,
        onlyVerified: Boolean = false
    ): Flow<Resource<List<CaregiverProfile>>> {
        return repository.searchCaregivers(query, skillFilter, maxHourlyRate, onlyVerified)
    }
}

class UploadKycDocumentUseCase(private val repository: CaregiverRepository) {
    suspend operator fun invoke(caregiverId: String, fileUriString: String): Resource<String> {
        return repository.uploadKycDocument(caregiverId, fileUriString)
    }
}
