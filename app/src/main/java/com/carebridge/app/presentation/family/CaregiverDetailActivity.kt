package com.carebridge.app.presentation.family

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carebridge.app.databinding.ActivityCaregiverDetailBinding
import com.carebridge.app.di.ServiceLocator
import com.carebridge.app.domain.model.CaregiverLocation
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.presentation.family.adapters.ReviewsAdapter
import com.carebridge.app.utils.Constants
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class CaregiverDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityCaregiverDetailBinding
    private val caregiverRepository = ServiceLocator.caregiverRepository
    private val reviewRepository = ServiceLocator.reviewRepository
    private lateinit var reviewsAdapter: ReviewsAdapter

    private var caregiverId: String = ""
    private var caregiverProfile: CaregiverProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCaregiverDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        caregiverId = intent.getStringExtra(Constants.EXTRA_CAREGIVER_ID) ?: ""
        if (caregiverId.isBlank()) {
            showToast("Caregiver not found")
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        reviewsAdapter = ReviewsAdapter()
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(this@CaregiverDetailActivity)
            adapter = reviewsAdapter
        }

        // Prepopulate with intent extras immediately
        val initialName = intent.getStringExtra("caregiver_name") ?: ""
        val initialRate = intent.getDoubleExtra("hourly_rate", 150.0)
        val initialExp = intent.getDoubleExtra("experience", 1.0)
        val initialBio = intent.getStringExtra("bio") ?: ""
        val initialAddress = intent.getStringExtra("address") ?: ""
        val initialStatus = intent.getStringExtra("is_verified")

        if (initialName.isNotBlank()) {
            caregiverProfile = CaregiverProfile(
                caregiverId = caregiverId,
                fullName = initialName,
                hourlyRate = initialRate,
                experienceYears = initialExp,
                bio = initialBio,
                location = CaregiverLocation(address = initialAddress),
                verificationStatus = VerificationStatus.fromString(initialStatus)
            )
            caregiverProfile?.let { displayCaregiver(it) }
        }

        binding.btnBookCaregiver.setOnClickListener {
            val intent = Intent(this, BookCaregiverActivity::class.java).apply {
                putExtra(Constants.EXTRA_CAREGIVER_ID, caregiverId)
                putExtra("caregiver_name", caregiverProfile?.fullName ?: initialName.ifBlank { "Caregiver" })
                putExtra("hourly_rate", caregiverProfile?.hourlyRate ?: initialRate)
            }
            startActivity(intent)
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            caregiverRepository.observeCaregiverProfile(caregiverId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    caregiverProfile = resource.data
                    displayCaregiver(resource.data)
                }
            }
        }

        lifecycleScope.launch {
            reviewRepository.observeCaregiverReviews(caregiverId).collect { resource ->
                if (resource is Resource.Success) {
                    reviewsAdapter.submitList(resource.data)
                }
            }
        }
    }

    private fun displayCaregiver(profile: CaregiverProfile) {
        binding.tvCaregiverName.text = profile.fullName
        binding.tvRateAndExperience.text = "₹%.0f / hour • %.0f+ years experience".format(
            profile.hourlyRate,
            profile.experienceYears
        )
        binding.tvLocation.text = "📍 ${profile.location.address.ifBlank { "Location details available upon booking" }}"
        binding.tvBio.text = profile.bio.ifBlank { "Professional caregiver committed to compassionate elder care." }

        if (profile.verificationStatus == VerificationStatus.VERIFIED) {
            binding.tvVerificationBadge.visible()
        } else {
            binding.tvVerificationBadge.gone()
        }

        if (profile.totalReviews > 0) {
            binding.tvRating.visible()
            binding.tvRating.text = "★ %.1f (%d reviews)".format(profile.averageRating, profile.totalReviews)
        } else {
            binding.tvRating.gone()
        }

        binding.chipGroupSkills.removeAllViews()
        profile.skills.forEach { skill ->
            val chip = Chip(this).apply {
                text = skill
                isClickable = false
            }
            binding.chipGroupSkills.addView(chip)
        }
    }
}
