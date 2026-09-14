package com.carebridge.app.data.model

import com.carebridge.app.domain.model.BillDetails
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.CaregiverLocation
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.Review
import com.carebridge.app.domain.model.SettlementStatus
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.domain.model.VerificationStatus
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toUserDomain(): User? {
    if (!exists()) return null
    return try {
        User(
            userId = id,
            fullName = getString("fullName") ?: "",
            email = getString("email") ?: "",
            phoneNumber = getString("phoneNumber") ?: "",
            role = UserRole.fromString(getString("role")),
            profileImageUrl = getString("profileImageUrl") ?: "",
            isEmailVerified = getBoolean("isEmailVerified") ?: getBoolean("emailVerified") ?: false,
            createdAt = (get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
            isActive = getBoolean("isActive") ?: getBoolean("active") ?: true
        )
    } catch (_: Exception) {
        null
    }
}

fun DocumentSnapshot.toCaregiverProfileDomain(): CaregiverProfile? {
    if (!exists()) return null
    return try {
        val skillsRaw = get("skills")
        val skillsList = when (skillsRaw) {
            is List<*> -> skillsRaw.mapNotNull { it?.toString() }
            is String -> listOf(skillsRaw)
            else -> emptyList()
        }

        val locMap = get("location") as? Map<*, *>
        val locAddress = locMap?.get("address")?.toString() ?: ""
        val locLat = (locMap?.get("latitude") as? Number)?.toDouble() ?: 0.0
        val locLng = (locMap?.get("longitude") as? Number)?.toDouble() ?: 0.0

        val exp = (get("experienceYears") as? Number)?.toDouble() ?: 0.0
        val rate = (get("hourlyRate") as? Number)?.toDouble() ?: 0.0
        val avgRating = (get("averageRating") as? Number)?.toDouble() ?: 0.0
        val reviews = (get("totalReviews") as? Number)?.toInt() ?: 0

        CaregiverProfile(
            caregiverId = id,
            fullName = getString("fullName") ?: "",
            email = getString("email") ?: "",
            phoneNumber = getString("phoneNumber") ?: "",
            profileImageUrl = getString("profileImageUrl") ?: "",
            skills = skillsList,
            experienceYears = exp,
            hourlyRate = rate,
            location = CaregiverLocation(locAddress, locLat, locLng),
            bio = getString("bio") ?: "",
            kycDocumentUrl = getString("kycDocumentUrl") ?: "",
            verificationStatus = VerificationStatus.fromString(getString("verificationStatus")),
            averageRating = avgRating,
            totalReviews = reviews,
            isAvailable = getBoolean("isAvailable") ?: getBoolean("available") ?: true
        )
    } catch (_: Exception) {
        null
    }
}

fun DocumentSnapshot.toBookingDomain(): Booking? {
    if (!exists()) return null
    return try {
        val billMap = get("bill") as? Map<*, *>
        val billDetails = billMap?.let {
            BillDetails(
                hourlyRate = (it["hourlyRate"] as? Number)?.toDouble() ?: 0.0,
                loggedHours = (it["loggedHours"] as? Number)?.toDouble() ?: 0.0,
                subtotal = (it["subtotal"] as? Number)?.toDouble() ?: 0.0,
                platformFee = (it["platformFee"] as? Number)?.toDouble() ?: 50.0,
                totalAmount = (it["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                settlementStatus = SettlementStatus.fromString(it["settlementStatus"]?.toString())
            )
        }

        Booking(
            bookingId = id,
            familyId = getString("familyId") ?: "",
            familyName = getString("familyName") ?: "",
            caregiverId = getString("caregiverId") ?: "",
            caregiverName = getString("caregiverName") ?: "",
            scheduledDate = (get("scheduledDate") as? Number)?.toLong() ?: 0L,
            serviceNotes = getString("serviceNotes") ?: "",
            status = BookingStatus.fromString(getString("status")),
            checkInTimestamp = (get("checkInTimestamp") as? Number)?.toLong(),
            checkOutTimestamp = (get("checkOutTimestamp") as? Number)?.toLong(),
            totalLoggedHours = (get("totalLoggedHours") as? Number)?.toDouble() ?: 0.0,
            bill = billDetails
        )
    } catch (_: Exception) {
        null
    }
}

fun DocumentSnapshot.toReviewDomain(): Review? {
    if (!exists()) return null
    return try {
        Review(
            reviewId = id,
            bookingId = getString("bookingId") ?: "",
            familyId = getString("familyId") ?: "",
            familyName = getString("familyName") ?: "",
            caregiverId = getString("caregiverId") ?: "",
            rating = (get("rating") as? Number)?.toDouble() ?: 5.0,
            comment = getString("comment") ?: "",
            createdAt = (get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    } catch (_: Exception) {
        null
    }
}
