package com.carebridge.app.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.carebridge.app.databinding.ActivityEmailVerificationBinding
import com.carebridge.app.presentation.common.BaseActivity
import com.carebridge.app.presentation.splash.SplashActivity
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.showToast
import com.carebridge.app.utils.visible

class EmailVerificationActivity : BaseActivity() {

    private lateinit var binding: ActivityEmailVerificationBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        val userEmail = intent.getStringExtra("user_email") ?: "your email address"
        binding.tvUserEmail.text = userEmail

        binding.btnCheckVerification.setOnClickListener {
            binding.progressBar.visible()
            viewModel.reloadUser { isVerified ->
                binding.progressBar.gone()
                if (isVerified) {
                    showToast("Email verified successfully!")
                    startActivity(Intent(this, SplashActivity::class.java))
                    finish()
                } else {
                    showToast("Email not verified yet. Please check your inbox.")
                }
            }
        }

        binding.btnResendEmail.setOnClickListener {
            viewModel.resendVerificationEmail()
            showToast("Verification link sent! Please check your spam or inbox.")
        }

        binding.tvLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
