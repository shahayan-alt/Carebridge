package com.carebridge.app.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.carebridge.app.databinding.ActivityRegisterBinding
import com.carebridge.app.domain.model.UserRole
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.utils.Resource
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString()

            val role = if (binding.rbCaregiver.isChecked) UserRole.CAREGIVER else UserRole.FAMILY

            if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                showToast("Please fill in all required fields")
                return@setOnClickListener
            }

            viewModel.register(fullName, email, password, phone, role)
        }

        binding.tvSignInPrompt.setOnClickListener {
            finish()
        }
    }

    override fun observeViewModel() {
        viewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnRegister.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.btnRegister.isEnabled = true
                    showToast("Account created! Please verify your email.")
                    val intent = Intent(this, EmailVerificationActivity::class.java).apply {
                        putExtra("user_email", resource.data.email)
                    }
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.btnRegister.isEnabled = true
                    showToast(resource.message)
                }
            }
        }
    }
}
