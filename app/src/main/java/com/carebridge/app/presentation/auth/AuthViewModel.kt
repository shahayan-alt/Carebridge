package com.carebridge.app.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carebridge.app.di.ServiceLocator
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = ServiceLocator.authRepository
    private val loginUseCase = ServiceLocator.loginUseCase
    private val registerUseCase = ServiceLocator.registerUseCase
    private val getCurrentUserUseCase = ServiceLocator.getCurrentUserUseCase

    private val _authState = MutableLiveData<Resource<User>>()
    val authState: LiveData<Resource<User>> = _authState

    private val _currentUser = MutableLiveData<Resource<User?>>()
    val currentUser: LiveData<Resource<User?>> = _currentUser

    private val _actionState = MutableLiveData<Resource<Unit>>()
    val actionState: LiveData<Resource<Unit>> = _actionState

    fun login(email: String, password: String) {
        _authState.value = Resource.Loading
        viewModelScope.launch {
            _authState.value = loginUseCase(email, password)
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: UserRole
    ) {
        _authState.value = Resource.Loading
        viewModelScope.launch {
            _authState.value = registerUseCase(fullName, email, password, phoneNumber, role)
        }
    }

    fun checkCurrentUser() {
        _currentUser.value = Resource.Loading
        viewModelScope.launch {
            _currentUser.value = getCurrentUserUseCase()
        }
    }

    fun resendVerificationEmail() {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            _actionState.value = authRepository.sendEmailVerification()
        }
    }

    fun reloadUser(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.reloadUser()
            onResult(result.getOrNull() == true)
        }
    }

    fun sendPasswordReset(email: String) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            _actionState.value = authRepository.sendPasswordResetEmail(email)
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
