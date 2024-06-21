package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taskpixie.adapter.ProjectsAdapter
import com.example.taskpixie.databinding.FragmentProjectsBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.Project
import com.example.taskpixie.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private lateinit var projectsAdapter: ProjectsAdapter

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        setupRecyclerView()

        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (!accessToken.isNullOrEmpty()) {
                fetchProjects("Bearer $accessToken")
            }
        }

        // Binding
        binding.fabAddProject.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_projects_to_addProjectFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== Setup RecyclerView ==================== //

    private fun setupRecyclerView() {
        projectsAdapter = ProjectsAdapter()
        binding.projectsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = projectsAdapter
        }
    }

    // ==================== Fetch Projects ==================== //

    private fun fetchProjects(token: String) {
        RetrofitClient.projectService.getProjects(token).enqueue(object : Callback<ApiResponse<List<Project>>> {
            override fun onResponse(call: Call<ApiResponse<List<Project>>>, response: Response<ApiResponse<List<Project>>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success") {
                        val projects = apiResponse.data ?: emptyList()
                        projectsAdapter.submitList(projects)
                    } else {
                        Toast.makeText(activity, apiResponse?.message ?: "Failed to fetch projects!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch projects!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Project>>>, t: Throwable) {
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
