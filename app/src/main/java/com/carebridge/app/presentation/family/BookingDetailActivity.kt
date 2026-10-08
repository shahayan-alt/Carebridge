package com.carebridge.app.presentation.family

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.carebridge.app.databinding.ActivityBookingDetailBinding
import com.carebridge.app.di.ServiceLocator
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.SettlementStatus
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.DateTimeUtils
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible
import kotlinx.coroutines.launch

class BookingDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private val bookingRepository = ServiceLocator.bookingRepository
    private val caregiverRepository = ServiceLocator.caregiverRepository
    private val reviewRepository = ServiceLocator.reviewRepository
    private val authRepository = ServiceLocator.authRepository

    private var bookingId: String = ""
    private var currentBooking: Booking? = null
    private var currentUserRole: UserRole = UserRole.FAMILY
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        bookingId = intent.getStringExtra(Constants.EXTRA_BOOKING_ID) ?: ""
        if (bookingId.isBlank()) {
            showToast("Invalid booking ID")
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Caregiver Check-In
        binding.btnCheckIn.setOnClickListener {
            val booking = currentBooking ?: return@setOnClickListener
            lifecycleScope.launch {
                binding.progressBar.visible()
                val res = bookingRepository.checkInCaregiver(booking.bookingId, System.currentTimeMillis())
                binding.progressBar.gone()
                if (res.isSuccess) {
                    showToast("Check-in successful! Session started.")
                } else {
                    showToast((res as Resource.Error).message)
                }
            }
        }

        // Caregiver Check-Out
        binding.btnCheckOut.setOnClickListener {
            val booking = currentBooking ?: return@setOnClickListener
            val checkInTime = booking.checkInTimestamp ?: return@setOnClickListener

            lifecycleScope.launch {
                binding.progressBar.visible()
                // Fetch caregiver profile to get exact hourly rate
                val profileRes = caregiverRepository.getCaregiverProfile(booking.caregiverId)
                val rate = profileRes.getOrNull()?.hourlyRate ?: 250.0

                val res = bookingRepository.checkOutCaregiver(
                    bookingId = booking.bookingId,
                    checkInTime = checkInTime,
                    checkOutTime = System.currentTimeMillis(),
                    hourlyRate = rate
                )
                binding.progressBar.gone()
                if (res.isSuccess) {
                    showToast("Check-out complete! Invoice generated.")
                } else {
                    showToast((res as Resource.Error).message)
                }
            }
        }

        // Mark direct settlement as settled
        binding.btnMarkSettled.setOnClickListener {
            lifecycleScope.launch {
                binding.progressBar.visible()
                bookingRepository.updateSettlementStatus(bookingId, SettlementStatus.SETTLED)
                binding.progressBar.gone()
                showToast("Payment settlement confirmed!")
            }
        }

        // Leave Review
        binding.btnLeaveReview.setOnClickListener {
            val booking = currentBooking ?: return@setOnClickListener
            LeaveReviewDialog(
                context = this,
                bookingId = booking.bookingId,
                familyId = booking.familyId,
                familyName = booking.familyName,
                caregiverId = booking.caregiverId
            ) { review ->
                lifecycleScope.launch {
                    val result = reviewRepository.submitReview(review)
                    if (result.isSuccess) {
                        showToast("Thank you for your feedback!")
                        binding.btnLeaveReview.gone()
                    } else {
                        showToast((result as Resource.Error).message)
                    }
                }
            }.show()
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            val userProfile = authRepository.getCurrentUserProfile().getOrNull()
            if (userProfile != null) {
                currentUserRole = userProfile.role
                currentUserId = userProfile.userId
            }

            bookingRepository.observeBookingById(bookingId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    currentBooking = resource.data
                    bindBookingDetails(resource.data)
                }
            }
        }
    }

    private fun bindBookingDetails(booking: Booking) {
        binding.tvBookingStatus.text = "Status: ${booking.status.name}"
        binding.tvParticipants.text = "Family: ${booking.familyName} • Caregiver: ${booking.caregiverName}"
        binding.tvScheduledDate.text = "Scheduled: ${DateTimeUtils.formatDateTime(booking.scheduledDate)}"
        binding.tvServiceNotes.text = "Notes: ${booking.serviceNotes.ifBlank { "None" }}"

        // Timestamps
        val checkInStr = if (booking.checkInTimestamp != null) {
            "Check-In: ${DateTimeUtils.formatDateTime(booking.checkInTimestamp)}"
        } else {
            "Check-In: Not Started"
        }
        val checkOutStr = if (booking.checkOutTimestamp != null) {
            "Check-Out: ${DateTimeUtils.formatDateTime(booking.checkOutTimestamp)}"
        } else {
            "Check-Out: Not Ended"
        }
        binding.tvCheckInTime.text = checkInStr
        binding.tvCheckOutTime.text = checkOutStr
        binding.tvTotalLoggedHours.text = "Total Logged: %.2f hrs".format(booking.totalLoggedHours)

        // Caregiver Action buttons
        val isCaregiver = currentUserRole == UserRole.CAREGIVER || currentUserId == booking.caregiverId
        if (isCaregiver) {
            when (booking.status) {
                BookingStatus.ACCEPTED -> {
                    binding.btnCheckIn.visible()
                    binding.btnCheckOut.gone()
                }
                BookingStatus.IN_PROGRESS -> {
                    binding.btnCheckIn.gone()
                    binding.btnCheckOut.visible()
                }
                else -> {
                    binding.btnCheckIn.gone()
                    binding.btnCheckOut.gone()
                }
            }
        } else {
            binding.btnCheckIn.gone()
            binding.btnCheckOut.gone()
        }

        // Invoice display
        val bill = booking.bill
        if (bill != null && (booking.status == BookingStatus.COMPLETED || bill.totalAmount > 0)) {
            binding.cardInvoice.visible()
            binding.tvInvoiceHours.text = "Logged Hours: %.2f hrs".format(bill.loggedHours)
            binding.tvInvoiceRate.text = "Hourly Rate: ₹%.2f".format(bill.hourlyRate)
            binding.tvInvoiceSubtotal.text = "Subtotal: ₹%.2f".format(bill.subtotal)
            binding.tvInvoiceFee.text = "Platform Fee (Flat): ₹%.2f".format(bill.platformFee)
            binding.tvInvoiceTotal.text = "Total Due: ₹%.2f".format(bill.totalAmount)
            binding.tvSettlementStatus.text = "Settlement: ${bill.settlementStatus.name}"

            // Family settlement and review buttons
            val isFamily = currentUserRole == UserRole.FAMILY || currentUserId == booking.familyId
            if (isFamily) {
                if (bill.settlementStatus == SettlementStatus.UNSETTLED) {
                    binding.btnMarkSettled.visible()
                } else {
                    binding.btnMarkSettled.gone()
                }
                binding.btnLeaveReview.visible()
            }
        } else {
            binding.cardInvoice.gone()
            binding.btnMarkSettled.gone()
            binding.btnLeaveReview.gone()
        }
    }
}
