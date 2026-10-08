package com.carebridge.app.presentation.family

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.carebridge.app.databinding.DialogLeaveReviewBinding
import com.carebridge.app.domain.model.Review
import com.carebridge.app.utils.showToast

class LeaveReviewDialog(
    context: Context,
    private val bookingId: String,
    private val familyId: String,
    private val familyName: String,
    private val caregiverId: String,
    private val onSubmit: (Review) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogLeaveReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogLeaveReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBar.rating.toDouble().coerceIn(1.0, 5.0)
            val comment = binding.etComment.text.toString().trim()

            val review = Review(
                bookingId = bookingId,
                familyId = familyId,
                familyName = familyName,
                caregiverId = caregiverId,
                rating = rating,
                comment = comment,
                createdAt = System.currentTimeMillis()
            )

            onSubmit(review)
            dismiss()
        }
    }
}
