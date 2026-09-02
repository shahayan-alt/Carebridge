package com.carebridge.app.domain.model

data class CaregiverLocation(
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class CaregiverProfile(
    val caregiverId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val skills: List<String> = emptyList(),
    val experienceYears: Double = 0.0,
    val hourlyRate: Double = 0.0,
    val location: CaregiverLocation = CaregiverLocation(),
    val bio: String = "",
    val kycDocumentUrl: String = "",
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val isAvailable: Boolean = true
)
