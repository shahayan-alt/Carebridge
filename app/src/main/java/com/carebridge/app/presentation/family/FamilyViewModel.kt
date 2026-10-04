package com.carebridge.app.presentation.family

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.carebridge.app.di.ServiceLocator
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.Review
import com.carebridge.app.domain.model.SettlementStatus
import com.carebridge.app.domain.model.User
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class FamilyViewModel : ViewModel() {

    private val authRepository = ServiceLocator.authRepository
    private val caregiverRepository = ServiceLocator.caregiverRepository
    private val bookingRepository = ServiceLocator.bookingRepository
    private val reviewRepository = ServiceLocator.reviewRepository

    val currentUserProfile: LiveData<Resource<User?>> = authRepository.observeCurrentUserProfile().asLiveData()

    // Search query and filters
    private val searchQuery = MutableStateFlow("")
    private val selectedSkill = MutableStateFlow("")
    private val verifiedOnly = MutableStateFlow(false)

    val caregiversList: LiveData<Resource<List<CaregiverProfile>>> =
        searchCaregivers()

    private val _bookingActionState = MutableLiveData<Resource<String>>()
    val bookingActionState: LiveData<Resource<String>> = _bookingActionState

    private val _reviewActionState = MutableLiveData<Resource<Unit>>()
    val reviewActionState: LiveData<Resource<Unit>> = _reviewActionState

    fun searchCaregivers(): LiveData<Resource<List<CaregiverProfile>>> {
        return caregiverRepository.searchCaregivers(
            query = searchQuery.value,
            skillFilter = selectedSkill.value,
            onlyVerified = verifiedOnly.value
        ).asLiveData()
    }

    fun updateFilters(query: String = searchQuery.value, skill: String = selectedSkill.value, verified: Boolean = verifiedOnly.value) {
        searchQuery.value = query
        selectedSkill.value = skill
        verifiedOnly.value = verified
    }

    fun getFamilyBookings(familyId: String): LiveData<Resource<List<Booking>>> {
        return bookingRepository.observeFamilyBookings(familyId).asLiveData()
    }

    fun createBooking(booking: Booking) {
        _bookingActionState.value = Resource.Loading
        viewModelScope.launch {
            _bookingActionState.value = bookingRepository.createBooking(booking)
        }
    }

    fun markSettlement(bookingId: String, settlementStatus: SettlementStatus) {
        viewModelScope.launch {
            bookingRepository.updateSettlementStatus(bookingId, settlementStatus)
        }
    }

    fun submitReview(review: Review) {
        _reviewActionState.value = Resource.Loading
        viewModelScope.launch {
            _reviewActionState.value = reviewRepository.submitReview(review)
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
