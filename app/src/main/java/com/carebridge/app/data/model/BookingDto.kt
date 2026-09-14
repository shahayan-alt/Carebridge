package com.carebridge.app.data.model

import com.carebridge.app.domain.model.BillDetails
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.SettlementStatus
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class BillDto(
    val hourlyRate: Double = 0.0,
    val loggedHours: Double = 0.0,
    val subtotal: Double = 0.0,
    val platformFee: Double = 50.0,
    val totalAmount: Double = 0.0,
    val settlementStatus: String = "UNSETTLED"
) {
    fun toDomain() = BillDetails(
        hourlyRate = hourlyRate,
        loggedHours = loggedHours,
        subtotal = subtotal,
        platformFee = platformFee,
        totalAmount = totalAmount,
        settlementStatus = SettlementStatus.fromString(settlementStatus)
    )

    companion object {
        fun fromDomain(b: BillDetails) = BillDto(
            hourlyRate = b.hourlyRate,
            loggedHours = b.loggedHours,
            subtotal = b.subtotal,
            platformFee = b.platformFee,
            totalAmount = b.totalAmount,
            settlementStatus = b.settlementStatus.name
        )
    }
}

@IgnoreExtraProperties
data class BookingDto(
    @DocumentId
    val bookingId: String = "",
    val familyId: String = "",
    val familyName: String = "",
    val caregiverId: String = "",
    val caregiverName: String = "",
    val scheduledDate: Long = 0L,
    val serviceNotes: String = "",
    val status: String = "PENDING",
    val checkInTimestamp: Long? = null,
    val checkOutTimestamp: Long? = null,
    val totalLoggedHours: Double = 0.0,
    val bill: BillDto? = null
) {
    fun toDomain(): Booking {
        return Booking(
            bookingId = bookingId,
            familyId = familyId,
            familyName = familyName,
            caregiverId = caregiverId,
            caregiverName = caregiverName,
            scheduledDate = scheduledDate,
            serviceNotes = serviceNotes,
            status = BookingStatus.fromString(status),
            checkInTimestamp = checkInTimestamp,
            checkOutTimestamp = checkOutTimestamp,
            totalLoggedHours = totalLoggedHours,
            bill = bill?.toDomain()
        )
    }

    companion object {
        fun fromDomain(b: Booking): BookingDto {
            return BookingDto(
                bookingId = b.bookingId,
                familyId = b.familyId,
                familyName = b.familyName,
                caregiverId = b.caregiverId,
                caregiverName = b.caregiverName,
                scheduledDate = b.scheduledDate,
                serviceNotes = b.serviceNotes,
                status = b.status.name,
                checkInTimestamp = b.checkInTimestamp,
                checkOutTimestamp = b.checkOutTimestamp,
                totalLoggedHours = b.totalLoggedHours,
                bill = b.bill?.let { BillDto.fromDomain(it) }
            )
        }
    }
}
