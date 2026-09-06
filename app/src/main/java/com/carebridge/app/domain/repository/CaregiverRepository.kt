package com.carebridge.app.domain.repository

import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

interface CaregiverRepository {
    suspend fun getCaregiverProfile(caregiverId: String): Resource<CaregiverProfile?>
    fun observeCaregiverProfile(caregiverId: String): Flow<Resource<CaregiverProfile?>>
    suspend fun updateCaregiverProfile(profile: CaregiverProfile): Resource<Unit>
    suspend fun uploadKycDocument(caregiverId: String, fileUriString: String): Resource<String>
    
    fun searchCaregivers(
        query: String = "",
        skillFilter: String = "",
        maxHourlyRate: Double? = null,
        onlyVerified: Boolean = false
    ): Flow<Resource<List<CaregiverProfile>>>
}
