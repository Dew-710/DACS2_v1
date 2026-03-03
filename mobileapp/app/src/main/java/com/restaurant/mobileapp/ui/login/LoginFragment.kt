package com.restaurant.mobileapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.restaurant.mobileapp.R
import com.restaurant.mobileapp.data.api.RegisterResponse
import com.restaurant.mobileapp.data.model.RegisterRequest
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import com.restaurant.mobileapp.databinding.FragmentLoginBinding
import com.restaurant.mobileapp.ui.viewmodel.AuthViewModel
import com.restaurant.mobileapp.ui.viewmodel.ViewModelFactory

class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(RestaurantRepository())
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.login(username, password)
        }
        
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Tên đăng nhập và mật khẩu là bắt buộc", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val request = RegisterRequest(
                username = username,
                password = password,
                fullName = if (fullName.isNotEmpty()) fullName else null,
                email = if (email.isNotEmpty()) email else null,
                phone = if (phone.isNotEmpty()) phone else null
            )
            
            viewModel.register(request)
        }
        
        observeViewModel()
    }
    
    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_menuFragment)
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Đăng nhập thất bại: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
        
        viewModel.registerResult.observe(viewLifecycleOwner) { result: Result<RegisterResponse> ->
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                },
                onFailure = {
                    Toast.makeText(context, "Đăng ký thất bại", Toast.LENGTH_LONG).show()
                }
            )
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnRegister.isEnabled = !isLoading
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
