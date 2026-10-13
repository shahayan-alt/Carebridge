package com.carebridge.app.presentation.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.carebridge.app.di.ServiceLocator
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.User
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.launch

class CaregiverViewModel : ViewModel() {

    private val authRepository = ServiceLocator.authRepository
    private val caregiverRepository = ServiceLocator.caregiverRepository
    private val bookingRepository = ServiceLocator.bookingRepository

    val currentUserProfile: LiveData<Resource<User?>> = authRepository.observeCurrentUserProfile().asLiveData()

    private val _actionState = MutableLiveData<Resource<Unit>>()
    val actionState: LiveData<Resource<Unit>> = _actionState

    fun getCaregiverProfile(caregiverId: String): LiveData<Resource<CaregiverProfile?>> {
        return caregiverRepository.observeCaregiverProfile(caregiverId).asLiveData()
    }

    fun getCaregiverBookings(caregiverId: String): LiveData<Resource<List<Booking>>> {
        return bookingRepository.observeCaregiverBookings(caregiverId).asLiveData()
    }

    fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, status)
        }
    }

    fun updateProfile(profile: CaregiverProfile) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            _actionState.value = caregiverRepository.updateCaregiverProfile(profile)
        }
    }

    fun uploadKycDocument(caregiverId: String, fileUriString: String) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = caregiverRepository.uploadKycDocument(caregiverId, fileUriString)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(res.message, res.cause)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
