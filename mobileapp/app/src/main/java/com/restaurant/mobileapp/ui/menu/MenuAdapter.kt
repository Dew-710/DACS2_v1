package com.restaurant.mobileapp.ui.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.restaurant.mobileapp.data.model.MenuItem
import com.restaurant.mobileapp.databinding.ItemMenuBinding
import java.math.BigDecimal

class MenuAdapter(
    private val onItemClick: (MenuItem) -> Unit
) : ListAdapter<MenuItem, MenuAdapter.MenuViewHolder>(MenuItemDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuViewHolder(binding, onItemClick)
    }
    
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class MenuViewHolder(
        private val binding: ItemMenuBinding,
        private val onItemClick: (MenuItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: MenuItem) {
            binding.tvMenuItemName.text = item.name
            binding.tvMenuItemPrice.text = formatPrice(item.price)
            binding.tvMenuItemDescription.text = item.description ?: ""
            
            // Load image with Glide
            item.imageUrl?.let { imageUrl ->
                Glide.with(binding.root)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivMenuItemImage)
            } ?: run {
                binding.ivMenuItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
        
        private fun formatPrice(price: BigDecimal?): String {
            return if (price != null) {
                "${price.toLong()} VNĐ"
            } else {
                "Liên hệ"
            }
        }
    }
    
    class MenuItemDiffCallback : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
            return oldItem == newItem
        }
    }
}

