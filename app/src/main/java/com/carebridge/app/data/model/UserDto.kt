package com.carebridge.app.data.model

import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.UserRole
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class UserDto(
    @DocumentId
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "FAMILY",
    val profileImageUrl: String = "",
    @get:PropertyName("isEmailVerified")
    @set:PropertyName("isEmailVerified")
    var isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true
) {
    fun toDomain(): User {
        return User(
            userId = userId,
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            role = UserRole.fromString(role),
            profileImageUrl = profileImageUrl,
            isEmailVerified = isEmailVerified,
            createdAt = createdAt,
            isActive = isActive
        )
    }

    companion object {
        fun fromDomain(user: User): UserDto {
            return UserDto(
                userId = user.userId,
                fullName = user.fullName,
                email = user.email,
                phoneNumber = user.phoneNumber,
                role = user.role.name,
                profileImageUrl = user.profileImageUrl,
                isEmailVerified = user.isEmailVerified,
                createdAt = user.createdAt,
                isActive = user.isActive
            )
        }
    }
}
