package com.carebridge.app.domain.repository

import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.SettlementStatus
import com.carebridge.app.utils.Resource
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun createBooking(booking: Booking): Resource<String>
    suspend fun getBookingById(bookingId: String): Resource<Booking?>
    fun observeBookingById(bookingId: String): Flow<Resource<Booking?>>
    
    fun observeFamilyBookings(familyId: String): Flow<Resource<List<Booking>>>
    fun observeCaregiverBookings(caregiverId: String): Flow<Resource<List<Booking>>>
    
    suspend fun updateBookingStatus(bookingId: String, newStatus: BookingStatus): Resource<Unit>
    suspend fun checkInCaregiver(bookingId: String, timestamp: Long): Resource<Unit>
    suspend fun checkOutCaregiver(
        bookingId: String,
        checkInTime: Long,
        checkOutTime: Long,
        hourlyRate: Double
    ): Resource<Booking>
    
    suspend fun updateSettlementStatus(bookingId: String, settlementStatus: SettlementStatus): Resource<Unit>
}
