package com.carebridge.app.presentation.family.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.carebridge.app.R
import com.carebridge.app.databinding.ItemBookingFamilyBinding
import com.carebridge.app.domain.model.Booking
import com.carebridge.app.domain.model.BookingStatus
import com.carebridge.app.utils.DateTimeUtils
import com.carebridge.app.utils.gone
import com.carebridge.app.utils.visible

class FamilyBookingsAdapter(
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<FamilyBookingsAdapter.BookingViewHolder>() {

    private val items = mutableListOf<Booking>()

    fun submitList(newList: List<Booking>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingFamilyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BookingViewHolder(private val binding: ItemBookingFamilyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            val context = binding.root.context
            binding.tvCaregiverName.text = booking.caregiverName.ifBlank { "Caregiver Visit" }
            binding.tvScheduledDate.text = "📅 ${DateTimeUtils.formatDateTime(booking.scheduledDate)}"
            binding.tvStatus.text = booking.status.name

            val statusColor = when (booking.status) {
                BookingStatus.PENDING -> ContextCompat.getColor(context, R.color.status_pending)
                BookingStatus.ACCEPTED -> ContextCompat.getColor(context, R.color.status_accepted)
                BookingStatus.IN_PROGRESS -> ContextCompat.getColor(context, R.color.status_in_progress)
                BookingStatus.COMPLETED -> ContextCompat.getColor(context, R.color.status_completed)
                BookingStatus.REJECTED, BookingStatus.CANCELLED -> ContextCompat.getColor(context, R.color.status_rejected)
            }
            binding.tvStatus.setTextColor(statusColor)

            if (booking.bill != null) {
                binding.tvBillSummary.visible()
                binding.tvBillSummary.text = "Total: ₹%.2f (%s)".format(
                    booking.bill.totalAmount,
                    booking.bill.settlementStatus.name
                )
            } else {
                binding.tvBillSummary.gone()
            }

            binding.root.setOnClickListener {
                onItemClick(booking)
            }
        }
    }
}
