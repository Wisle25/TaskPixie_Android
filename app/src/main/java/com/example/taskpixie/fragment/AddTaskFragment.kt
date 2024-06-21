package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.taskpixie.databinding.FragmentAddTaskBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.TaskPayload
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        binding.submitTaskBtn.setOnClickListener {
            lifecycleScope.launch {
                val accessToken = userPreferences.accessToken.first()
                if (!accessToken.isNullOrEmpty()) {
                    val payload = getInputValues()
                    addTask("Bearer $accessToken", payload)
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== Input Handling ==================== //

    private fun getInputValues(): TaskPayload {
        val title = binding.taskTitleInput.text.toString()
        val description = binding.taskDescriptionInput.text.toString()

        return TaskPayload(title, description)
    }

    private fun addTask(token: String, payload: TaskPayload) {
        RetrofitClient.taskService.addTask(token, payload).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success") {
                        Toast.makeText(activity, apiResponse.message, Toast.LENGTH_SHORT).show()
                        // Navigate back to the task list or handle UI updates
                    } else {
                        Toast.makeText(activity, apiResponse?.message ?: "Failed to add task!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Failed to add task!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
