package com.carebridge.app.domain.usecase

import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.SettlementStatus
import com.carebridge.app.domain.repository.BookingRepository
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

class CreateBookingUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(booking: Booking): Resource<String> {
        if (booking.familyId.isBlank() || booking.caregiverId.isBlank()) {
            return Resource.Error("Family ID and Caregiver ID are required")
        }
        if (booking.scheduledDate <= 0L) {
            return Resource.Error("Scheduled date is required")
        }
        return repository.createBooking(booking)
    }
}

class ObserveFamilyBookingsUseCase(private val repository: BookingRepository) {
    operator fun invoke(familyId: String): Flow<Resource<List<Booking>>> =
        repository.observeFamilyBookings(familyId)
}

class ObserveCaregiverBookingsUseCase(private val repository: BookingRepository) {
    operator fun invoke(caregiverId: String): Flow<Resource<List<Booking>>> =
        repository.observeCaregiverBookings(caregiverId)
}

class UpdateBookingStatusUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(bookingId: String, status: BookingStatus): Resource<Unit> =
        repository.updateBookingStatus(bookingId, status)
}

class CheckInCaregiverUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(bookingId: String, timestamp: Long = System.currentTimeMillis()): Resource<Unit> =
        repository.checkInCaregiver(bookingId, timestamp)
}

class CheckOutCaregiverUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(
        bookingId: String,
        checkInTime: Long,
        checkOutTime: Long = System.currentTimeMillis(),
        hourlyRate: Double
    ): Resource<Booking> =
        repository.checkOutCaregiver(bookingId, checkInTime, checkOutTime, hourlyRate)
}

class UpdateSettlementStatusUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(bookingId: String, settlementStatus: SettlementStatus): Resource<Unit> =
        repository.updateSettlementStatus(bookingId, settlementStatus)
}
