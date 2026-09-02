package com.carebridge.app.domain.model

data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.FAMILY,
    val profileImageUrl: String = "",
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
