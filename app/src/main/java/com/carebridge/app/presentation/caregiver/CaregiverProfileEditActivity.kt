package com.carebridge.app.presentation.caregiver

import android.os.Bundle
import androidx.activity.viewModels
import com.carebridge.app.databinding.ActivityCaregiverProfileEditBinding
import com.carebridge.app.domain.model.CaregiverLocation
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible
import com.google.firebase.auth.FirebaseAuth

class CaregiverProfileEditActivity : BaseActivity() {

    private lateinit var binding: ActivityCaregiverProfileEditBinding
    private val viewModel: CaregiverViewModel by viewModels()

    private var currentProfile: CaregiverProfile? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCaregiverProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        val rate = intent.getDoubleExtra("hourly_rate", 250.0)
        val exp = intent.getDoubleExtra("experience", 1.0)
        val address = intent.getStringExtra("address") ?: ""
        val bio = intent.getStringExtra("bio") ?: ""

        binding.etHourlyRate.setText(rate.toInt().toString())
        binding.etExperience.setText(exp.toInt().toString())
        binding.etAddress.setText(address)
        binding.etBio.setText(bio)

        binding.btnSaveProfile.setOnClickListener {
            val updatedRate = binding.etHourlyRate.text.toString().toDoubleOrNull() ?: 250.0
            val updatedExp = binding.etExperience.text.toString().toDoubleOrNull() ?: 1.0
            val updatedAddress = binding.etAddress.text.toString().trim()
            val updatedBio = binding.etBio.text.toString().trim()

            val authUser = FirebaseAuth.getInstance().currentUser
            val uid = currentProfile?.caregiverId ?: currentUser?.userId ?: authUser?.uid ?: ""
            val name = currentProfile?.fullName?.ifBlank { null }
                ?: currentUser?.fullName?.ifBlank { null }
                ?: authUser?.displayName?.ifBlank { null }
                ?: "Caregiver"

            if (uid.isBlank()) {
                showToast("Please log in to update profile")
                return@setOnClickListener
            }

            val updated = currentProfile?.copy(
                hourlyRate = updatedRate,
                experienceYears = updatedExp,
                location = CaregiverLocation(address = updatedAddress),
                bio = updatedBio
            ) ?: CaregiverProfile(
                caregiverId = uid,
                fullName = name,
                hourlyRate = updatedRate,
                experienceYears = updatedExp,
                bio = updatedBio,
                location = CaregiverLocation(address = updatedAddress),
                verificationStatus = VerificationStatus.PENDING
            )

            viewModel.updateProfile(updated)
        }
    }

    override fun observeViewModel() {
        viewModel.currentUserProfile.observe(this) { userRes ->
            if (userRes is Resource.Success && userRes.data != null) {
                currentUser = userRes.data
                viewModel.getCaregiverProfile(userRes.data.userId).observe(this) { profileRes ->
                    if (profileRes is Resource.Success && profileRes.data != null) {
                        currentProfile = profileRes.data
                        if (binding.etBio.text.isNullOrBlank() && profileRes.data.bio.isNotBlank()) {
                            binding.etBio.setText(profileRes.data.bio)
                        }
                    }
                }
            }
        }

        viewModel.actionState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSaveProfile.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.btnSaveProfile.isEnabled = true
                    showToast("Profile updated successfully!")
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.btnSaveProfile.isEnabled = true
                    showToast(resource.message)
                }
            }
        }
    }
}
