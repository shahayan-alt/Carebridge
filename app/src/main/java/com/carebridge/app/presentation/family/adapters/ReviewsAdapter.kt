package com.carebridge.app.presentation.family.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carebridge.app.databinding.ItemReviewBinding
import com.carebridge.app.domain.model.Review
import com.carebridge.app.utils.DateTimeUtils

class ReviewsAdapter : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    private val items = mutableListOf<Review>()

    fun submitList(newList: List<Review>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvFamilyName.text = review.familyName.ifBlank { "Family Member" }
            binding.tvRating.text = "★ ${review.rating.toInt()}/5"
            binding.tvComment.text = review.comment.ifBlank { "No written comment provided." }
            binding.tvDate.text = DateTimeUtils.formatDate(review.createdAt)
        }
    }
}
