package com.carebridge.app.presentation.caregiver.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.carebridge.app.R
import com.carebridge.app.databinding.ItemBookingCaregiverBinding
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.utils.DateTimeUtils
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.visible

class CaregiverBookingsAdapter(
    private val onItemClick: (Booking) -> Unit,
    private val onAcceptClick: (Booking) -> Unit,
    private val onRejectClick: (Booking) -> Unit
) : RecyclerView.Adapter<CaregiverBookingsAdapter.BookingViewHolder>() {

    private val items = mutableListOf<Booking>()

    fun submitList(newList: List<Booking>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingCaregiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BookingViewHolder(private val binding: ItemBookingCaregiverBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            val context = binding.root.context
            binding.tvFamilyName.text = booking.familyName.ifBlank { "Client Booking" }
            binding.tvScheduledDate.text = "📅 ${DateTimeUtils.formatDateTime(booking.scheduledDate)}"
            binding.tvServiceNotes.text = booking.serviceNotes.ifBlank { "No special notes." }
            binding.tvStatus.text = booking.status.name

            val statusColor = when (booking.status) {
                BookingStatus.PENDING -> ContextCompat.getColor(context, R.color.status_pending)
                BookingStatus.ACCEPTED -> ContextCompat.getColor(context, R.color.status_accepted)
                BookingStatus.IN_PROGRESS -> ContextCompat.getColor(context, R.color.status_in_progress)
                BookingStatus.COMPLETED -> ContextCompat.getColor(context, R.color.status_completed)
                BookingStatus.REJECTED, BookingStatus.CANCELLED -> ContextCompat.getColor(context, R.color.status_rejected)
            }
            binding.tvStatus.setTextColor(statusColor)

            if (booking.status == BookingStatus.PENDING) {
                binding.layoutActionButtons.visible()
                binding.btnAccept.setOnClickListener { onAcceptClick(booking) }
                binding.btnReject.setOnClickListener { onRejectClick(booking) }
            } else {
                binding.layoutActionButtons.gone()
            }

            binding.root.setOnClickListener {
                onItemClick(booking)
            }
        }
    }
}
