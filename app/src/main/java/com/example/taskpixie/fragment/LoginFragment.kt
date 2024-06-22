package com.example.taskpixie.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taskpixie.databinding.FragmentLoginBinding
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.LoginUserPayload
import com.example.taskpixie.R
import com.example.taskpixie.activity.MainActivity
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        // Bind Button
        binding.loginBtn.setOnClickListener {
            val (isValid, payload) = getInputValues()
            if (isValid) {
                handleLogin(payload)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== Input Handling ==================== //

    private fun getInputValues(): Pair<Boolean, LoginUserPayload> {
        val identity = binding.identityInput.text.toString()
        val password = binding.passwordInput.text.toString()

        var isValid = true

        if (identity.isEmpty()) {
            binding.identityInput.error = "Identity is required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordInput.error = "Password is required"
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(activity, "Input shouldn't be empty!", Toast.LENGTH_SHORT).show()
        }

        return Pair(isValid, LoginUserPayload(identity, password))
    }

    private fun handleLogin(payload: LoginUserPayload) {
        RetrofitClient.userService.loginUser(payload).enqueue(object :
            Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                val gson = Gson()
                val type = object : TypeToken<ApiResponse<String>>() {}.type

                val apiResponse: ApiResponse<String>? = if (response.isSuccessful) {
                    response.body()
                } else {
                    gson.fromJson(response.errorBody()?.charStream(), type)
                }

                if (apiResponse?.status == "success") {
                    Toast.makeText(activity, apiResponse.message, Toast.LENGTH_SHORT).show()
                    val accessToken = response.headers()["Authorization"]?.replace("Bearer ", "")
                    val refreshToken = response.headers()["X-Refresh-Token"]

                    if (accessToken != null && refreshToken != null) {
                        lifecycleScope.launch {
                            userPreferences.saveTokens(accessToken, refreshToken)
                            userPreferences.saveCredentials(payload.identity, payload.password)

                            fetchUserDetails(accessToken)

                            // Navigate to MainActivity
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish() // Finish LoginActivity
                        }
                    }
                } else {
                    displayErrorMessages(apiResponse?.message)
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Fetch user details after successful login and save user ID
     */
    private fun fetchUserDetails(token: String) {
        RetrofitClient.userService.getLoggedUser(token).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success") {
                        val user = apiResponse.data
                        user?.let {
                            lifecycleScope.launch {
                                userPreferences.saveUserId(it.id)
                                userPreferences.saveUsername(it.username)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {

            }
        })
    }

    private fun displayErrorMessages(message: String?) {
        message?.let {
            // Split general error message from the rest using newline
            val messages = it.split("\n", limit = 2)
            if (messages.isNotEmpty()) {
                // Display general error message
                Toast.makeText(activity, messages[0], Toast.LENGTH_SHORT).show()

                // Check for specific field errors
                if (messages.size > 1) {
                    val fieldErrors = messages[1].split(";")
                    for (fieldError in fieldErrors) {
                        val parts = fieldError.split(":").map { itStr -> itStr.trim() }
                        if (parts.size > 1) {
                            val field = parts[0]
                            val errorMessage = parts[1]
                            when (field) {
                                "Identity" -> binding.identityInput.error = errorMessage
                                "Password" -> binding.passwordInput.error = errorMessage
                            }
                        }
                    }
                }
            }
        }
    }
}
