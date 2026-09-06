package com.carebridge.app.domain.usecase

import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.domain.repository.AuthRepository
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank()) return Resource.Error("Email cannot be empty")
        if (password.isBlank()) return Resource.Error("Password cannot be empty")
        return repository.login(email.trim(), password)
    }
}

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: UserRole
    ): Resource<User> {
        if (fullName.isBlank()) return Resource.Error("Full name is required")
        if (email.isBlank()) return Resource.Error("Email is required")
        if (password.length < 6) return Resource.Error("Password must be at least 6 characters")
        return repository.register(fullName.trim(), email.trim(), password, phoneNumber.trim(), role)
    }
}

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<User?> = repository.getCurrentUserProfile()
    fun observe(): Flow<Resource<User?>> = repository.observeCurrentUserProfile()
}

class LogoutUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.logout()
}
