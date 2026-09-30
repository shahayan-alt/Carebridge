package com.carebridge.app.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.carebridge.app.databinding.ActivityLoginBinding
import com.carebridge.app.domain.model.User
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.presentation.admin.AdminMainActivity
import com.carebridge.app.presentation.caregiver.CaregiverMainActivity
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.presentation.family.FamilyMainActivity
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                showToast("Please enter email and password")
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        binding.tvSignUpPrompt.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isBlank()) {
                showToast("Enter your email address first")
                return@setOnClickListener
            }
            viewModel.sendPasswordReset(email)
            showToast("Password reset link sent to your email")
        }
    }

    override fun observeViewModel() {
        viewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.btnLogin.isEnabled = true
                    handleLoginSuccess(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.btnLogin.isEnabled = true
                    showToast(resource.message)
                }
            }
        }
    }

    private fun handleLoginSuccess(user: User) {
        if (!user.isEmailVerified) {
            val intent = Intent(this, EmailVerificationActivity::class.java).apply {
                putExtra("user_email", user.email)
            }
            startActivity(intent)
            finish()
            return
        }

        val target = when (user.role) {
            UserRole.FAMILY -> Intent(this, FamilyMainActivity::class.java)
            UserRole.CAREGIVER -> Intent(this, CaregiverMainActivity::class.java)
            UserRole.ADMIN -> Intent(this, AdminMainActivity::class.java)
        }
        startActivity(target)
        finish()
    }
}
