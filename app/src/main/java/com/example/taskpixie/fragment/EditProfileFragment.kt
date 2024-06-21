package com.example.taskpixie.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.taskpixie.R
import com.example.taskpixie.databinding.FragmentEditProfileBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private var selectedImageUri: Uri? = null
    private lateinit var userId: String

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        // Fetch and display current user's profile
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (!accessToken.isNullOrEmpty()) {
                fetchUserProfile("Bearer $accessToken")
            }
        }

        binding.userAvatar.setOnClickListener {
            selectImage()
        }

        binding.editProfileBtn.setOnClickListener {
            lifecycleScope.launch {
                val accessToken = userPreferences.accessToken.first()
                if (!accessToken.isNullOrEmpty() && userId.isNotEmpty()) {
                    val payload = getInputValues()
                    updateUserProfile("Bearer $accessToken", userId, payload)
                }
            }
        }

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
                            userId = it.id
                            binding.usernameInput.setText(it.username)
                            binding.emailInput.setText(it.email)
                            val avatarUrl = if (it.avatarLink.isNotEmpty()) {
                                RetrofitClient.MINIO_URL + it.avatarLink
                            } else {
                                ""
                            }
                            Log.d("EditProfile", "Avatar URL: $avatarUrl")
                            Glide.with(this@EditProfileFragment)
                                .load(avatarUrl)
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

    // ==================== Input Handling ==================== //

    private fun getInputValues(): Map<String, String> {
        val username = binding.usernameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        return mapOf(
            "username" to username,
            "email" to email,
            "password" to password,
            "confirmPassword" to confirmPassword
        )
    }

    private fun updateUserProfile(token: String, userId: String, payload: Map<String, String>) {
        val username = (payload["username"] ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val email = (payload["email"] ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val password = (payload["password"] ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val confirmPassword = (payload["confirmPassword"] ?: "").toRequestBody("text/plain".toMediaTypeOrNull())

        val avatar = selectedImageUri?.let { uri ->
            val file = getFileFromUri(requireContext(), uri)
            file.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("avatar", it.name, requestFile)
            }
        }

        RetrofitClient.userService.updateUser(token, userId, username, email, password, confirmPassword, avatar).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Toast.makeText(activity, apiResponse?.message ?: "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Failed to update profile!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                Log.d("EditProfile", "Error: ${t.message}")
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ==================== File Selection ==================== //

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        } else {
            Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displaySelectedImage(uri: Uri) {
        binding.userAvatar.visibility = View.VISIBLE
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.anonym)
            .error(R.drawable.anonym)
            .into(binding.userAvatar)
    }

    // ==================== Utility Function ==================== //

    private fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "tempFile")
        file.createNewFile()
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()

        return file
    }
}
