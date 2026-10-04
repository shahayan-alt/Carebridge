package com.carebridge.app.presentation.family.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carebridge.app.R
import com.carebridge.app.databinding.ItemCaregiverCardBinding
import com.carebridge.app.domain.model.CaregiverProfile
import com.carebridge.app.domain.model.VerificationStatus
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.visible

class CaregiverAdapter(
    private val onItemClick: (CaregiverProfile) -> Unit
) : RecyclerView.Adapter<CaregiverAdapter.CaregiverViewHolder>() {

    private val items = mutableListOf<CaregiverProfile>()

    fun submitList(newList: List<CaregiverProfile>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaregiverViewHolder {
        val binding = ItemCaregiverCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaregiverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaregiverViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CaregiverViewHolder(private val binding: ItemCaregiverCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(caregiver: CaregiverProfile) {
            binding.tvCaregiverName.text = caregiver.fullName.ifBlank { "Caregiver" }
            binding.tvRate.text = "₹${caregiver.hourlyRate.toInt()}/hr"
            binding.tvSkills.text = caregiver.skills.joinToString(" • ")
            binding.tvLocation.text = caregiver.location.address.ifBlank { "Location available on request" }

            if (caregiver.verificationStatus == VerificationStatus.VERIFIED) {
                binding.tvVerificationBadge.visible()
            } else {
                binding.tvVerificationBadge.gone()
            }

            if (caregiver.totalReviews > 0) {
                binding.tvRating.visible()
                binding.tvRating.text = "★ %.1f (%d)".format(caregiver.averageRating, caregiver.totalReviews)
            } else {
                binding.tvRating.gone()
            }

            binding.root.setOnClickListener {
                onItemClick(caregiver)
            }
        }
    }
}
