package com.carebridge.app.data.repository

import com.carebridge.app.data.model.BillDto
import com.carebridge.app.data.model.BookingDto
import com.carebridge.app.data.model.toBookingDomain
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.SettlementStatus
import com.carebridge.app.domain.repository.BookingRepository
import com.carebridge.app.domain.usecase.BillCalculationUseCase
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BookingRepositoryImpl(
    private val firestore: FirebaseFirestore
) : BookingRepository {

    override suspend fun createBooking(booking: Booking): Resource<String> {
        return try {
            val docRef = firestore.collection(Constants.COLLECTION_BOOKINGS).document()
            val finalBooking = booking.copy(bookingId = docRef.id)
            val dto = BookingDto.fromDomain(finalBooking)
            docRef.set(dto).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create booking", e)
        }
    }

    override suspend fun getBookingById(bookingId: String): Resource<Booking?> {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_BOOKINGS).document(bookingId).get().await()
            Resource.Success(doc.toBookingDomain())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get booking", e)
        }
    }

    override fun observeBookingById(bookingId: String): Flow<Resource<Booking?>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_BOOKINGS).document(bookingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading booking", error))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snapshot?.toBookingDomain()))
            }

        awaitClose { listener.remove() }
    }

    override fun observeFamilyBookings(familyId: String): Flow<Resource<List<Booking>>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_BOOKINGS)
            .whereEqualTo("familyId", familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading family bookings", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toBookingDomain() } ?: emptyList()
                trySend(Resource.Success(list))
            }

        awaitClose { listener.remove() }
    }

    override fun observeCaregiverBookings(caregiverId: String): Flow<Resource<List<Booking>>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_BOOKINGS)
            .whereEqualTo("caregiverId", caregiverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading caregiver bookings", error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toBookingDomain() } ?: emptyList()
                trySend(Resource.Success(list))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateBookingStatus(bookingId: String, newStatus: BookingStatus): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_BOOKINGS)
                .document(bookingId)
                .update("status", newStatus.name)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update booking status", e)
        }
    }

    override suspend fun checkInCaregiver(bookingId: String, timestamp: Long): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_BOOKINGS)
                .document(bookingId)
                .update(
                    mapOf(
                        "checkInTimestamp" to timestamp,
                        "status" to BookingStatus.IN_PROGRESS.name
                    )
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Check-in failed", e)
        }
    }

    override suspend fun checkOutCaregiver(
        bookingId: String,
        checkInTime: Long,
        checkOutTime: Long,
        hourlyRate: Double
    ): Resource<Booking> {
        return try {
            val bookingDoc = firestore.collection(Constants.COLLECTION_BOOKINGS).document(bookingId).get().await()
            val existingBooking = bookingDoc.toBookingDomain()
                ?: return Resource.Error("Booking not found")

            val bill = BillCalculationUseCase.calculate(
                checkInTimestamp = checkInTime,
                checkOutTimestamp = checkOutTime,
                hourlyRate = hourlyRate
            )

            val updatedBooking = existingBooking.copy(
                checkInTimestamp = checkInTime,
                checkOutTimestamp = checkOutTime,
                totalLoggedHours = bill.loggedHours,
                status = BookingStatus.COMPLETED,
                bill = bill
            )

            val updates = mapOf(
                "checkInTimestamp" to checkInTime,
                "checkOutTimestamp" to checkOutTime,
                "totalLoggedHours" to bill.loggedHours,
                "status" to BookingStatus.COMPLETED.name,
                "bill" to BillDto.fromDomain(bill)
            )

            firestore.collection(Constants.COLLECTION_BOOKINGS)
                .document(bookingId)
                .update(updates)
                .await()

            Resource.Success(updatedBooking)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Check-out failed", e)
        }
    }

    override suspend fun updateSettlementStatus(
        bookingId: String,
        settlementStatus: SettlementStatus
    ): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_BOOKINGS)
                .document(bookingId)
                .update("bill.settlementStatus", settlementStatus.name)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update settlement status", e)
        }
    }
}
