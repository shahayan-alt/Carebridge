package com.carebridge.app.presentation.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.carebridge.app.R
import com.carebridge.app.databinding.ActivityAdminMainBinding
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.presentation.admin.adapters.KycVerificationAdapter
import com.carebridge.app.presentation.admin.adapters.UsersManagementAdapter
import com.carebridge.app.presentation.auth.LoginActivity
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible

class AdminMainActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private val viewModel: AdminViewModel by viewModels()

    private lateinit var kycAdapter: KycVerificationAdapter
    private lateinit var usersAdapter: UsersManagementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        // Setup KYC Adapter
        kycAdapter = KycVerificationAdapter(
            onApproveClick = { profile ->
                viewModel.updateCaregiverVerification(profile.caregiverId, VerificationStatus.VERIFIED)
                showToast("Caregiver ${profile.fullName} approved and verified!")
            },
            onRejectClick = { profile ->
                viewModel.updateCaregiverVerification(profile.caregiverId, VerificationStatus.REJECTED)
                showToast("Caregiver ${profile.fullName} application rejected.")
            }
        )
        binding.rvKycApplications.apply {
            layoutManager = LinearLayoutManager(this@AdminMainActivity)
            adapter = kycAdapter
        }

        // Setup Users Adapter
        usersAdapter = UsersManagementAdapter { user, isActive ->
            viewModel.updateUserActiveState(user.userId, isActive)
            val stateStr = if (isActive) "activated" else "suspended"
            showToast("User ${user.fullName} $stateStr")
        }
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(this@AdminMainActivity)
            adapter = usersAdapter
        }

        // Swipe Refresh
        binding.swipeRefreshKyc.setOnRefreshListener {
            binding.swipeRefreshKyc.isRefreshing = false
        }

        // Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_kyc -> {
                    binding.layoutKycTab.visible()
                    binding.layoutUsersTab.gone()
                    true
                }
                R.id.nav_admin_users -> {
                    binding.layoutKycTab.gone()
                    binding.layoutUsersTab.visible()
                    true
                }
                else -> false
            }
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun observeViewModel() {
        viewModel.pendingCaregivers.observe(this) { resource ->
            if (resource is Resource.Success) {
                kycAdapter.submitList(resource.data)
            }
        }

        viewModel.allUsers.observe(this) { resource ->
            if (resource is Resource.Success) {
                usersAdapter.submitList(resource.data)
            }
        }
    }
}
