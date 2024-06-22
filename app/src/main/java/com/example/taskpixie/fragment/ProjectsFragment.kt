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
import com.example.taskpixie.R
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.PreviewProject
import com.example.taskpixie.model.ApiResponse
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
        fetchProjects()

        // Binding
        binding.fabAddProject.setOnClickListener {
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
        projectsAdapter = ProjectsAdapter { projectId ->
            val bundle = Bundle().apply {
                putString("projectId", projectId)
            }
            findNavController().navigate(R.id.action_navigation_projects_to_projectDetailFragment, bundle)
        }
        binding.projectsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = projectsAdapter
        }
    }

    private fun fetchProjects() {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.projectService.getProjects("Bearer $token").enqueue(object : Callback<ApiResponse<List<PreviewProject>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<PreviewProject>>>,
                    response: Response<ApiResponse<List<PreviewProject>>>
                ) {
                    if (response.isSuccessful) {
                        val projects = response.body()?.data ?: emptyList()
                        projectsAdapter.submitList(projects)
                    } else {
                        Toast.makeText(requireContext(), "Error fetching projects: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<PreviewProject>>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error fetching projects", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
