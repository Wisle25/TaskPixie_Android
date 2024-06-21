package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taskpixie.databinding.FragmentRegisterBinding
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.RegisterUserPayload
import com.example.taskpixie.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Bind Button
        binding.registerBtn.setOnClickListener {
            val (isValid, payload) = getInputValues()
            if (isValid) {
                handleRegister(payload)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== Input Handling ==================== //

    private fun getInputValues(): Pair<Boolean, RegisterUserPayload> {
        val username = binding.usernameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        var isValid = true

        if (username.isEmpty()) {
            binding.usernameInput.error = "Username is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailInput.error = "Email is required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordInput.error = "Password is required"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInput.error = "Confirm Password is required"
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(activity, "Input shouldn't be empty!", Toast.LENGTH_SHORT).show()
        }

        return Pair(isValid, RegisterUserPayload(username, email, password, confirmPassword))
    }

    private fun handleRegister(payload: RegisterUserPayload) {
        RetrofitClient.userService.registerUser(payload).enqueue(object :
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
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                } else {
                    displayErrorMessages(apiResponse?.message)
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                                "Email" -> binding.emailInput.error = errorMessage
                                "Username" -> binding.usernameInput.error = errorMessage
                                "Password" -> binding.passwordInput.error = errorMessage
                                "ConfirmPassword" -> binding.confirmPasswordInput.error = errorMessage
                            }
                        }
                    }
                }
            }
        }
    }
}
