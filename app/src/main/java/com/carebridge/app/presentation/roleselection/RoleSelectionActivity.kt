package com.carebridge.app.presentation.roleselection

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.carebridge.app.databinding.ActivityRoleSelectionBinding
import com.carebridge.app.di.ServiceLocator
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.presentation.caregiver.CaregiverMainActivity
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.presentation.family.FamilyMainActivity
import com.carebridge.app.utils.showToast
import kotlinx.coroutines.launch

class RoleSelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding
    private val authRepository = ServiceLocator.authRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        binding.cardFamily.setOnClickListener {
            assignRole(UserRole.FAMILY)
        }

        binding.cardCaregiver.setOnClickListener {
            assignRole(UserRole.CAREGIVER)
        }
    }

    private fun assignRole(role: UserRole) {
        lifecycleScope.launch {
            val userResult = authRepository.getCurrentUserProfile()
            val user = userResult.getOrNull()
            if (user != null) {
                val updatedUser = user.copy(role = role)
                authRepository.updateUserProfile(updatedUser)
                if (role == UserRole.FAMILY) {
                    startActivity(Intent(this@RoleSelectionActivity, FamilyMainActivity::class.java))
                } else {
                    startActivity(Intent(this@RoleSelectionActivity, CaregiverMainActivity::class.java))
                }
                finish()
            } else {
                showToast("Failed to fetch user profile")
            }
        }
    }
}
