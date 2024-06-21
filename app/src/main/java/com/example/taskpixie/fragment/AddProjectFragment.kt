package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taskpixie.R
import com.example.taskpixie.databinding.FragmentAddProjectBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.Project
import com.example.taskpixie.model.ProjectPayload
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddProjectFragment : Fragment() {

    private var _binding: FragmentAddProjectBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProjectBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        binding.submitProjectBtn.setOnClickListener {
            val (isValid, payload) = getInputValues()
            if (isValid) {
                lifecycleScope.launch {
                    val accessToken = userPreferences.accessToken.first()
                    if (!accessToken.isNullOrEmpty()) {
                        addProject("Bearer $accessToken", payload)
                    }
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

    private fun getInputValues(): Pair<Boolean, ProjectPayload> {
        val name = binding.projectNameInput.text.toString()
        val description = binding.projectDescriptionInput.text.toString()

        var isValid = true

        if (name.isEmpty()) {
            binding.projectNameInput.error = "Project name is required"
            isValid = false
        }

        if (description.isEmpty()) {
            binding.projectDescriptionInput.error = "Project description is required"
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(activity, "Input shouldn't be empty!", Toast.LENGTH_SHORT).show()
        }

        return Pair(isValid, ProjectPayload(name, description))
    }

    private fun addProject(token: String, payload: ProjectPayload) {
        RetrofitClient.projectService.addProject(token, payload).enqueue(object : Callback<ApiResponse<Project>> {
            override fun onResponse(call: Call<ApiResponse<Project>>, response: Response<ApiResponse<Project>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Toast.makeText(activity, apiResponse?.message ?: "Project added successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate back to the projects list or another appropriate action
                    findNavController().navigate(R.id.navigation_projects)
                } else {
                    Toast.makeText(activity, "Failed to add project!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Project>>, t: Throwable) {
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
