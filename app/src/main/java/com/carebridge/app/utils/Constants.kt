package com.carebridge.app.utils

object Constants {
    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CAREGIVER_PROFILES = "caregiver_profiles"
    const val COLLECTION_BOOKINGS = "bookings"
    const val COLLECTION_REVIEWS = "reviews"

    // Storage Paths
    const val STORAGE_KYC_DOCS = "kyc_documents"
    const val STORAGE_PROFILE_PICS = "profile_images"

    // Billing Constants
    const val DEFAULT_PLATFORM_FEE = 50.0 // Flat platform fee in INR

    // Bundles & Intent Extras
    const val EXTRA_CAREGIVER_ID = "extra_caregiver_id"
    const val EXTRA_BOOKING_ID = "extra_booking_id"
    const val EXTRA_USER_ROLE = "extra_user_role"

    // Skills List
    val DEFAULT_SKILLS = listOf(
        "Elderly Companionship",
        "Dementia & Alzheimer Care",
        "Mobility Assistance",
        "Medication Reminders",
        "Post-Surgery Recovery",
        "Meal Preparation",
        "Basic Nursing & Vitals",
        "Palliative Care Support"
    )
}
