package com.carebridge.app.presentation.caregiver

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.carebridge.app.R
import com.carebridge.app.databinding.ActivityCaregiverMainBinding
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.presentation.auth.LoginActivity
import com.carebridge.app.presentation.caregiver.adapters.CaregiverBookingsAdapter
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.presentation.family.BookingDetailActivity
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible

class CaregiverMainActivity : BaseActivity() {

    private lateinit var binding: ActivityCaregiverMainBinding
    private val viewModel: CaregiverViewModel by viewModels()

    private lateinit var bookingsAdapter: CaregiverBookingsAdapter
    private var currentUser: User? = null
    private var caregiverProfile: CaregiverProfile? = null

    private val selectDocLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val caregiverId = currentUser?.userId ?: return@let
            viewModel.uploadKycDocument(caregiverId, it.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCaregiverMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        // Setup Bookings Adapter
        bookingsAdapter = CaregiverBookingsAdapter(
            onItemClick = { booking ->
                val intent = Intent(this, BookingDetailActivity::class.java).apply {
                    putExtra(Constants.EXTRA_BOOKING_ID, booking.bookingId)
                }
                startActivity(intent)
            },
            onAcceptClick = { booking ->
                viewModel.updateBookingStatus(booking.bookingId, BookingStatus.ACCEPTED)
                showToast("Booking accepted!")
            },
            onRejectClick = { booking ->
                viewModel.updateBookingStatus(booking.bookingId, BookingStatus.REJECTED)
                showToast("Booking declined.")
            }
        )

        binding.rvCaregiverBookings.apply {
            layoutManager = LinearLayoutManager(this@CaregiverMainActivity)
            adapter = bookingsAdapter
        }

        binding.swipeRefreshBookings.setOnRefreshListener {
            currentUser?.userId?.let { uid ->
                loadBookings(uid)
            }
        }

        // Bottom Nav
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_caregiver_requests -> {
                    binding.layoutRequestsTab.visible()
                    binding.layoutProfileTab.gone()
                    true
                }
                R.id.nav_caregiver_profile -> {
                    binding.layoutRequestsTab.gone()
                    binding.layoutProfileTab.visible()
                    true
                }
                else -> false
            }
        }

        // Edit Profile Button
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, CaregiverProfileEditActivity::class.java).apply {
                putExtra("hourly_rate", caregiverProfile?.hourlyRate ?: 250.0)
                putExtra("experience", caregiverProfile?.experienceYears ?: 1.0)
                putExtra("address", caregiverProfile?.location?.address ?: "")
                putExtra("bio", caregiverProfile?.bio ?: "")
            }
            startActivity(intent)
        }

        // Upload KYC Button
        binding.btnUploadKyc.setOnClickListener {
            selectDocLauncher.launch("image/*")
        }

        // Logout
        binding.btnLogout.setOnClickListener { logout() }
        binding.btnCaregiverLogout.setOnClickListener { logout() }
    }

    override fun observeViewModel() {
        viewModel.currentUserProfile.observe(this) { resource ->
            if (resource is Resource.Success && resource.data != null) {
                currentUser = resource.data
                val uid = resource.data.userId

                loadBookings(uid)

                viewModel.getCaregiverProfile(uid).observe(this) { profileRes ->
                    if (profileRes is Resource.Success && profileRes.data != null) {
                        caregiverProfile = profileRes.data
                        bindProfile(profileRes.data)
                    }
                }
            }
        }

        viewModel.actionState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBarKyc.visible()
                is Resource.Success -> {
                    binding.progressBarKyc.gone()
                    showToast("KYC document uploaded! Application set to PENDING.")
                }
                is Resource.Error -> {
                    binding.progressBarKyc.gone()
                    showToast(resource.message)
                }
            }
        }
    }

    private fun loadBookings(caregiverId: String) {
        viewModel.getCaregiverBookings(caregiverId).observe(this) { resource ->
            binding.swipeRefreshBookings.isRefreshing = resource is Resource.Loading
            if (resource is Resource.Success) {
                bookingsAdapter.submitList(resource.data)
            }
        }
    }

    private fun bindProfile(profile: CaregiverProfile) {
        binding.tvCaregiverName.text = profile.fullName.ifBlank { currentUser?.fullName }
        binding.tvHourlyRate.text = "Hourly Rate: ₹%.0f / hr".format(profile.hourlyRate)
        binding.tvExperience.text = "Experience: %.0f years".format(profile.experienceYears)
        binding.tvBio.text = profile.bio.ifBlank { "No bio provided" }

        when (profile.verificationStatus) {
            VerificationStatus.VERIFIED -> {
                binding.tvKycStatusBadge.text = "KYC Status: VERIFIED"
                binding.tvKycStatusBadge.setTextColor(getColor(R.color.verified_green))
                binding.tvKycStatusBadge.setBackgroundColor(getColor(R.color.verified_bg))
            }
            VerificationStatus.REJECTED -> {
                binding.tvKycStatusBadge.text = "KYC Status: REJECTED"
                binding.tvKycStatusBadge.setTextColor(getColor(R.color.status_rejected))
                binding.tvKycStatusBadge.setBackgroundColor(getColor(R.color.status_rejected_bg))
            }
            VerificationStatus.PENDING -> {
                binding.tvKycStatusBadge.text = "KYC Status: PENDING REVIEW"
                binding.tvKycStatusBadge.setTextColor(getColor(R.color.status_pending))
                binding.tvKycStatusBadge.setBackgroundColor(getColor(R.color.status_pending_bg))
            }
        }
    }

    private fun logout() {
        viewModel.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
