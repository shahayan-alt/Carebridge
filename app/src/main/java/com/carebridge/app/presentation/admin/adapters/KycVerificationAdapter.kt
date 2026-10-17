package com.carebridge.app.presentation.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.carebridge.app.databinding.ItemKycVerificationBinding
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.visible

class KycVerificationAdapter(
    private val onApproveClick: (CaregiverProfile) -> Unit,
    private val onRejectClick: (CaregiverProfile) -> Unit
) : RecyclerView.Adapter<KycVerificationAdapter.KycViewHolder>() {

    private val items = mutableListOf<CaregiverProfile>()

    fun submitList(newList: List<CaregiverProfile>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KycViewHolder {
        val binding = ItemKycVerificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KycViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KycViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class KycViewHolder(private val binding: ItemKycVerificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: CaregiverProfile) {
            binding.tvCaregiverName.text = profile.fullName.ifBlank { "Caregiver" }
            binding.tvExperienceRate.text = "₹%.0f/hr • %.0f years exp".format(profile.hourlyRate, profile.experienceYears)
            binding.tvBio.text = profile.bio.ifBlank { "No bio provided" }

            if (profile.kycDocumentUrl.isNotBlank()) {
                binding.tvDocumentStatus.text = "📄 KYC Document Attached (Review below)"
                binding.ivKycPreview.visible()
                Glide.with(binding.root.context)
                    .load(profile.kycDocumentUrl)
                    .into(binding.ivKycPreview)
            } else {
                binding.tvDocumentStatus.text = "⚠️ No Document Uploaded Yet"
                binding.ivKycPreview.gone()
            }

            binding.btnApproveKyc.setOnClickListener { onApproveClick(profile) }
            binding.btnRejectKyc.setOnClickListener { onRejectClick(profile) }
        }
    }
}
