package com.restaurant.mobileapp.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.restaurant.mobileapp.data.model.Order
import com.restaurant.mobileapp.databinding.ItemOrderBinding
import java.math.BigDecimal

class OrdersAdapter : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(order: Order) {
            binding.tvOrderId.text = "Đơn hàng #${order.id}"
            binding.tvOrderStatus.text = order.status ?: "N/A"
            binding.tvOrderTotal.text = formatPrice(order.totalAmount)
            binding.tvOrderTime.text = order.orderTime ?: ""
            
            val itemsCount = order.orderItems?.size ?: 0
            binding.tvOrderItemsCount.text = "$itemsCount món"
        }
        
        private fun formatPrice(price: BigDecimal?): String {
            return if (price != null) {
                "${price.toLong()} VNĐ"
            } else {
                "0 VNĐ"
            }
        }
    }
    
    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}

