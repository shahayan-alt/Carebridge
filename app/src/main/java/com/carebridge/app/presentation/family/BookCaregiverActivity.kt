package com.carebridge.app.presentation.family

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.viewModels
import com.carebridge.app.databinding.ActivityBookCaregiverBinding
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.User
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.DateTimeUtils
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class BookCaregiverActivity : BaseActivity() {

    private lateinit var binding: ActivityBookCaregiverBinding
    private val viewModel: FamilyViewModel by viewModels()

    private var caregiverId: String = ""
    private var caregiverName: String = ""
    private var hourlyRate: Double = 0.0
    private var currentUser: User? = null

    private val selectedCalendar = Calendar.getInstance()
    private var isDateTimeSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBookCaregiverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        caregiverId = intent.getStringExtra(Constants.EXTRA_CAREGIVER_ID) ?: ""
        caregiverName = intent.getStringExtra("caregiver_name") ?: "Caregiver"
        hourlyRate = intent.getDoubleExtra("hourly_rate", 0.0)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvCaregiverInfo.text = "Booking with $caregiverName"
        binding.tvRateInfo.text = "Caregiver Rate: ₹%.0f / hour".format(hourlyRate)

        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }

        binding.btnSubmitBooking.setOnClickListener {
            if (!isDateTimeSelected) {
                showToast("Please select visit date and time")
                return@setOnClickListener
            }

            val notes = binding.etNotes.text.toString().trim()
            val authUser = FirebaseAuth.getInstance().currentUser
            val currentUid = currentUser?.userId ?: authUser?.uid ?: ""
            
            if (currentUid.isBlank()) {
                showToast("Please sign in to book a visit")
                return@setOnClickListener
            }

            val currentName = currentUser?.fullName?.ifBlank { null }
                ?: authUser?.displayName?.ifBlank { null }
                ?: authUser?.email?.substringBefore("@")
                ?: "Family Client"

            val booking = Booking(
                familyId = currentUid,
                familyName = currentName,
                caregiverId = caregiverId,
                caregiverName = caregiverName,
                scheduledDate = selectedCalendar.timeInMillis,
                serviceNotes = notes,
                status = BookingStatus.PENDING
            )

            viewModel.createBooking(booking)
        }
    }

    override fun observeViewModel() {
        viewModel.currentUserProfile.observe(this) { resource ->
            if (resource is Resource.Success) {
                currentUser = resource.data
            }
        }

        viewModel.bookingActionState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSubmitBooking.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.btnSubmitBooking.isEnabled = true
                    showToast("Booking request sent successfully!")
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.btnSubmitBooking.isEnabled = true
                    val errorMsg = if (resource.message.contains("PERMISSION_DENIED", ignoreCase = true)) {
                        "Permission Denied: Please update Firestore Database Rules in your Firebase Console."
                    } else {
                        resource.message
                    }
                    showToast(errorMsg)
                }
            }
        }
    }

    private fun showDatePicker() {
        val current = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateTimeDisplay()
            },
            current.get(Calendar.YEAR),
            current.get(Calendar.MONTH),
            current.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    private fun showTimePicker() {
        val current = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar.set(Calendar.MINUTE, minute)
                selectedCalendar.set(Calendar.SECOND, 0)
                isDateTimeSelected = true
                updateDateTimeDisplay()
            },
            current.get(Calendar.HOUR_OF_DAY),
            current.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateTimeDisplay() {
        binding.tvSelectedDateTime.text = "Selected: ${DateTimeUtils.formatDateTime(selectedCalendar.timeInMillis)}"
        binding.tvSelectedDateTime.visible()
    }
}
