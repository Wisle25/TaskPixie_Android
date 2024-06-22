package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskpixie.R
import com.example.taskpixie.adapter.TasksAdapter
import com.example.taskpixie.databinding.FragmentProjectDetailBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.Project
import com.example.taskpixie.model.PreviewTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var userPreferences: UserPreferences
    private var projectId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        projectId = arguments?.getString("projectId")

        setupRecyclerView()
        fetchProjectDetail()
        setupDeleteButton()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        tasksAdapter = TasksAdapter { _ ->
            findNavController().navigate(R.id.action_projectDetailFragment_to_taskDetailFragment)
        }

        binding.projectTasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tasksAdapter
        }
    }

    private fun fetchProjectDetail() {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            projectId?.let { id ->
                RetrofitClient.projectService.getProjectDetails("Bearer $token", id).enqueue(object : Callback<ApiResponse<Project>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Project>>,
                        response: Response<ApiResponse<Project>>
                    ) {
                        if (response.isSuccessful) {
                            val project = response.body()?.data
                            project?.let { updateUI(it) }
                        } else {
                            Toast.makeText(requireContext(), "Error fetching project detail: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Project>>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error fetching project detail", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun updateUI(project: Project) {
        binding.projectTitle.text = project.title
        binding.projectDetail.text = project.detail
        binding.projectPriority.text = project.priority
        binding.projectStatus.text = project.status
        binding.projectCreatedAt.text = formatDate(project.createdAt)
        binding.projectUpdatedAt.text = formatDate(project.updatedAt)
        binding.projectAssignedTo.text = project.members.joinToString(", ")

        val tasks = project.tasks?.map {
            PreviewTask(
                id = it.id,
                title = it.title,
                description = it.description,
                priority = it.priority,
                status = it.status,
                project = project.title
            )
        }

        if (tasks != null) {
            tasksAdapter.submitList(tasks)
        }
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date ?: "")
    }

    private fun setupDeleteButton() {
        binding.deleteProjectBtn.setOnClickListener {
            projectId?.let { id ->
                lifecycleScope.launch {
                    val token = userPreferences.accessToken.first() ?: return@launch
                    RetrofitClient.projectService.deleteProject("Bearer $token", id).enqueue(object : Callback<ApiResponse<Unit>> {
                        override fun onResponse(call: Call<ApiResponse<Unit>>, response: Response<ApiResponse<Unit>>) {
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Project deleted successfully!", Toast.LENGTH_SHORT).show()
                                findNavController().navigateUp() // Navigate back to the previous screen
                            } else {
                                Toast.makeText(requireContext(), "Error deleting project: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                            Toast.makeText(requireContext(), "Error deleting project", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }
}
