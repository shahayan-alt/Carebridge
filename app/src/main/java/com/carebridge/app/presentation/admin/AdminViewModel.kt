package com.carebridge.app.presentation.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.carebridge.app.di.ServiceLocator
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val adminRepository = ServiceLocator.adminRepository
    private val authRepository = ServiceLocator.authRepository

    val pendingCaregivers: LiveData<Resource<List<CaregiverProfile>>> =
        adminRepository.observePendingCaregivers().asLiveData()

    val allUsers: LiveData<Resource<List<User>>> =
        adminRepository.observeAllUsers().asLiveData()

    fun updateCaregiverVerification(caregiverId: String, status: VerificationStatus) {
        viewModelScope.launch {
            adminRepository.updateCaregiverVerification(caregiverId, status)
        }
    }

    fun updateUserActiveState(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            adminRepository.updateUserActiveState(userId, isActive)
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
