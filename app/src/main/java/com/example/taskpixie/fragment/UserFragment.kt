package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.taskpixie.databinding.FragmentUserBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.User
import com.example.taskpixie.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        // Fetch and display user profile
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (!accessToken.isNullOrEmpty()) {
                fetchUserProfile("Bearer $accessToken")
            }
        }

        // Bind
        binding.editProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_userFragment_to_editProfileFragment)
        }

        // ...
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== Fetch User Profile ==================== //

    private fun fetchUserProfile(token: String) {
        RetrofitClient.userService.getLoggedUser(token).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success") {
                        val user = apiResponse.data
                        user?.let {
                            binding.userUsername.text = it.username
                            binding.userEmail.text = it.email

                            // Load avatar with Glide
                            Glide.with(this@UserFragment)
                                .load(RetrofitClient.MINIO_URL + it.avatarLink)
                                .placeholder(R.drawable.anonym)
                                .error(R.drawable.anonym)
                                .into(binding.userAvatar)
                        }
                    } else {
                        Toast.makeText(activity, apiResponse?.message ?: "Failed to fetch user profile!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch user profile!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
