package com.carebridge.app.data.model

import com.carebridge.app.domain.model.CaregiverLocation
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.VerificationStatus
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class LocationDto(
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun toDomain() = CaregiverLocation(address, latitude, longitude)
    companion object {
        fun fromDomain(loc: CaregiverLocation) = LocationDto(loc.address, loc.latitude, loc.longitude)
    }
}

@IgnoreExtraProperties
data class CaregiverProfileDto(
    @DocumentId
    val caregiverId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val skills: List<String> = emptyList(),
    val experienceYears: Double = 0.0,
    val hourlyRate: Double = 0.0,
    val location: LocationDto = LocationDto(),
    val bio: String = "",
    val kycDocumentUrl: String = "",
    val verificationStatus: String = "PENDING",
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true
) {
    fun toDomain(): CaregiverProfile {
        return CaregiverProfile(
            caregiverId = caregiverId,
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            profileImageUrl = profileImageUrl,
            skills = skills,
            experienceYears = experienceYears,
            hourlyRate = hourlyRate,
            location = location.toDomain(),
            bio = bio,
            kycDocumentUrl = kycDocumentUrl,
            verificationStatus = VerificationStatus.fromString(verificationStatus),
            averageRating = averageRating,
            totalReviews = totalReviews,
            isAvailable = isAvailable
        )
    }

    companion object {
        fun fromDomain(p: CaregiverProfile): CaregiverProfileDto {
            return CaregiverProfileDto(
                caregiverId = p.caregiverId,
                fullName = p.fullName,
                email = p.email,
                phoneNumber = p.phoneNumber,
                profileImageUrl = p.profileImageUrl,
                skills = p.skills,
                experienceYears = p.experienceYears,
                hourlyRate = p.hourlyRate,
                location = LocationDto.fromDomain(p.location),
                bio = p.bio,
                kycDocumentUrl = p.kycDocumentUrl,
                verificationStatus = p.verificationStatus.name,
                averageRating = p.averageRating,
                totalReviews = p.totalReviews,
                isAvailable = p.isAvailable
            )
        }
    }
}
