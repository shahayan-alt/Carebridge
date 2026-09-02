package com.carebridge.app.domain.model

enum class UserRole {
    FAMILY,
    CAREGIVER,
    ADMIN;

    companion object {
        fun fromString(role: String?): UserRole {
            return entries.firstOrNull { it.name.equals(role, ignoreCase = true) } ?: FAMILY
        }
    }
}

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED;

    companion object {
        fun fromString(status: String?): VerificationStatus {
            return entries.firstOrNull { it.name.equals(status, ignoreCase = true) } ?: PENDING
        }
    }
}

enum class BookingStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    companion object {
        fun fromString(status: String?): BookingStatus {
            return entries.firstOrNull { it.name.equals(status, ignoreCase = true) } ?: PENDING
        }
    }
}

enum class SettlementStatus {
    UNSETTLED,
    SETTLED;

    companion object {
        fun fromString(status: String?): SettlementStatus {
            return entries.firstOrNull { it.name.equals(status, ignoreCase = true) } ?: UNSETTLED
        }
    }
}
