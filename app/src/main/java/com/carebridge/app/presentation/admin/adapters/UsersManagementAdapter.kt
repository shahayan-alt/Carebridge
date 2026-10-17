package com.carebridge.app.presentation.admin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carebridge.app.databinding.ItemUserManagementBinding
import com.carebridge.app.domain.model.User

class UsersManagementAdapter(
    private val onToggleActive: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UsersManagementAdapter.UserViewHolder>() {

    private val items = mutableListOf<User>()

    fun submitList(newList: List<User>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class UserViewHolder(private val binding: ItemUserManagementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvUserName.text = user.fullName.ifBlank { "User" }
            binding.tvUserEmailAndRole.text = "${user.email} • ${user.role.name}"
            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.isChecked = user.isActive

            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggleActive(user, isChecked)
            }
        }
    }
}
