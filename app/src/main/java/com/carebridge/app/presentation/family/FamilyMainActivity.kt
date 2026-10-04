package com.carebridge.app.presentation.family

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.carebridge.app.R
import com.carebridge.app.databinding.ActivityFamilyMainBinding
import com.carebridge.app.domain.model.User
import com.carebridge.app.presentation.auth.LoginActivity
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.presentation.family.adapters.CaregiverAdapter
import com.carebridge.app.presentation.family.adapters.FamilyBookingsAdapter
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible

class FamilyMainActivity : BaseActivity() {

    private lateinit var binding: ActivityFamilyMainBinding
    private val viewModel: FamilyViewModel by viewModels()

    private lateinit var caregiverAdapter: CaregiverAdapter
    private lateinit var bookingsAdapter: FamilyBookingsAdapter
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityFamilyMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        // Setup Caregiver RecyclerView
        caregiverAdapter = CaregiverAdapter { caregiver ->
            val intent = Intent(this, CaregiverDetailActivity::class.java).apply {
                putExtra(Constants.EXTRA_CAREGIVER_ID, caregiver.caregiverId)
                putExtra("caregiver_name", caregiver.fullName)
                putExtra("hourly_rate", caregiver.hourlyRate)
                putExtra("experience", caregiver.experienceYears)
                putExtra("bio", caregiver.bio)
                putExtra("address", caregiver.location.address)
                putExtra("is_verified", caregiver.verificationStatus.name)
            }
            startActivity(intent)
        }
        binding.rvCaregivers.apply {
            layoutManager = LinearLayoutManager(this@FamilyMainActivity)
            adapter = caregiverAdapter
        }

        // Setup Bookings RecyclerView
        bookingsAdapter = FamilyBookingsAdapter { booking ->
            val intent = Intent(this, BookingDetailActivity::class.java).apply {
                putExtra(Constants.EXTRA_BOOKING_ID, booking.bookingId)
            }
            startActivity(intent)
        }
        binding.rvFamilyBookings.apply {
            layoutManager = LinearLayoutManager(this@FamilyMainActivity)
            adapter = bookingsAdapter
        }

        // Search Input
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateFilters(query = s?.toString() ?: "")
                refreshCaregivers()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Chip Filter Group
        binding.chipGroupSkills.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            when (checkedId) {
                R.id.chipAll -> viewModel.updateFilters(skill = "", verified = false)
                R.id.chipVerified -> viewModel.updateFilters(verified = true)
                R.id.chipDementia -> viewModel.updateFilters(skill = "Dementia")
                R.id.chipNursing -> viewModel.updateFilters(skill = "Nursing")
                R.id.chipMobility -> viewModel.updateFilters(skill = "Mobility")
                else -> viewModel.updateFilters(skill = "", verified = false)
            }
            refreshCaregivers()
        }

        // Swipe Refresh
        binding.swipeRefreshCaregivers.setOnRefreshListener {
            refreshCaregivers()
        }

        // Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_family_search -> {
                    showSearchTab()
                    true
                }
                R.id.nav_family_bookings -> {
                    showBookingsTab()
                    true
                }
                R.id.nav_family_profile -> {
                    showProfileTab()
                    true
                }
                else -> false
            }
        }

        // Logout
        binding.btnLogout.setOnClickListener { logout() }
        binding.btnProfileLogout.setOnClickListener { logout() }
    }

    override fun observeViewModel() {
        viewModel.currentUserProfile.observe(this) { resource ->
            if (resource is Resource.Success && resource.data != null) {
                currentUser = resource.data
                binding.tvProfileName.text = currentUser?.fullName
                binding.tvProfileEmail.text = currentUser?.email
                binding.tvProfileRole.text = "Role: ${currentUser?.role?.name}"

                // Observe bookings for current family member
                currentUser?.userId?.let { familyId ->
                    viewModel.getFamilyBookings(familyId).observe(this) { bResource ->
                        if (bResource is Resource.Success) {
                            bookingsAdapter.submitList(bResource.data)
                        }
                    }
                }
            }
        }

        refreshCaregivers()
    }

    private fun refreshCaregivers() {
        viewModel.searchCaregivers().observe(this) { resource ->
            binding.swipeRefreshCaregivers.isRefreshing = resource is Resource.Loading
            if (resource is Resource.Success) {
                caregiverAdapter.submitList(resource.data)
            }
        }
    }

    private fun showSearchTab() {
        binding.layoutSearchTab.visible()
        binding.layoutBookingsTab.gone()
        binding.layoutProfileTab.gone()
    }

    private fun showBookingsTab() {
        binding.layoutSearchTab.gone()
        binding.layoutBookingsTab.visible()
        binding.layoutProfileTab.gone()
    }

    private fun showProfileTab() {
        binding.layoutSearchTab.gone()
        binding.layoutBookingsTab.gone()
        binding.layoutProfileTab.visible()
    }

    private fun logout() {
        viewModel.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
