package com.carebridge.app.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.carebridge.app.databinding.ActivitySplashBinding
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.presentation.admin.AdminMainActivity
import com.carebridge.app.presentation.auth.AuthViewModel
import com.carebridge.app.presentation.auth.EmailVerificationActivity
import com.carebridge.app.presentation.auth.LoginActivity
import com.carebridge.app.presentation.caregiver.CaregiverMainActivity
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.presentation.family.FamilyMainActivity
import com.carebridge.app.utils.Resource

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                viewModel.checkCurrentUser()
            } catch (_: Exception) {
                navigateToLogin()
            }
        }, 1200)
    }

    override fun observeViewModel() {
        viewModel.currentUser.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    if (user == null) {
                        navigateToLogin()
                    } else if (!user.isEmailVerified) {
                        navigateToEmailVerification(user)
                    } else {
                        routeUserByRole(user)
                    }
                }
                is Resource.Error -> {
                    navigateToLogin()
                }
                is Resource.Loading -> {
                    // Loading state
                }
            }
        }
    }

    private fun routeUserByRole(user: User) {
        try {
            val targetIntent = when (user.role) {
                UserRole.FAMILY -> Intent(this, FamilyMainActivity::class.java)
                UserRole.CAREGIVER -> Intent(this, CaregiverMainActivity::class.java)
                UserRole.ADMIN -> Intent(this, AdminMainActivity::class.java)
            }
            startActivity(targetIntent)
            finish()
        } catch (_: Exception) {
            navigateToLogin()
        }
    }

    private fun navigateToEmailVerification(user: User) {
        try {
            val intent = Intent(this, EmailVerificationActivity::class.java).apply {
                putExtra("user_email", user.email)
            }
            startActivity(intent)
            finish()
        } catch (_: Exception) {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        try {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } catch (_: Exception) {}
    }
}
