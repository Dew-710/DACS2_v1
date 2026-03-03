package com.restaurant.mobileapp.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.restaurant.mobileapp.R
import com.restaurant.mobileapp.data.model.MenuItem
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import com.restaurant.mobileapp.databinding.FragmentMenuBinding
import com.restaurant.mobileapp.ui.viewmodel.MenuViewModel
import com.restaurant.mobileapp.ui.viewmodel.ViewModelFactory

class MenuFragment : Fragment() {
    
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MenuViewModel by viewModels {
        ViewModelFactory(RestaurantRepository())
    }
    
    private lateinit var menuAdapter: MenuAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        menuAdapter = MenuAdapter { menuItem ->
            // Handle item click - add to cart
            Toast.makeText(context, "Đã thêm ${menuItem.name} vào giỏ hàng", Toast.LENGTH_SHORT).show()
        }
        
        binding.rvMenu.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = menuAdapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.menuItems.observe(viewLifecycleOwner) { items ->
            menuAdapter.submitList(items)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

