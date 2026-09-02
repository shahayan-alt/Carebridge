package com.carebridge.app.domain.model

data class Booking(
    val bookingId: String = "",
    val familyId: String = "",
    val familyName: String = "",
    val caregiverId: String = "",
    val caregiverName: String = "",
    val scheduledDate: Long = 0L,
    val serviceNotes: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val checkInTimestamp: Long? = null,
    val checkOutTimestamp: Long? = null,
    val totalLoggedHours: Double = 0.0,
    val bill: BillDetails? = null
)
