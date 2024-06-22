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
import com.example.taskpixie.adapter.TasksAdapter
import com.example.taskpixie.databinding.FragmentTasksBinding
import com.example.taskpixie.R
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.PreviewTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    private lateinit var todoAdapter: TasksAdapter
    private lateinit var inProgressAdapter: TasksAdapter
    private lateinit var completedAdapter: TasksAdapter
    private lateinit var missingAdapter: TasksAdapter

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        setupRecyclerViews()
        setupToggleButtons()
        getLoggedUser()

        fetchTasks()

        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_tasks_to_formTaskFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== Setup Methods ==================== //

    private fun setupRecyclerViews() {
        todoAdapter = TasksAdapter {
            navigateToTaskDetail(it.id)
        }
        inProgressAdapter = TasksAdapter {
            navigateToTaskDetail(it.id)
        }
        completedAdapter = TasksAdapter {
            navigateToTaskDetail(it.id)
        }
        missingAdapter = TasksAdapter {
            navigateToTaskDetail(it.id)
        }

        binding.todoRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }

        binding.inProgressRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = inProgressAdapter
        }

        binding.completedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = completedAdapter
        }

        binding.missingRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = missingAdapter
        }

        // Set initial visibility to GONE
        binding.todoRecyclerView.visibility = View.GONE
        binding.inProgressRecyclerView.visibility = View.GONE
        binding.completedRecyclerView.visibility = View.GONE
        binding.missingRecyclerView.visibility = View.GONE
    }

    private fun setupToggleButtons() {
        binding.completedBtn.setOnClickListener {
            toggleVisibility(binding.completedRecyclerView)
        }
        binding.inProgressBtn.setOnClickListener {
            toggleVisibility(binding.inProgressRecyclerView)
        }
        binding.todoBtn.setOnClickListener {
            toggleVisibility(binding.todoRecyclerView)
        }
        binding.missingBtn.setOnClickListener {
            toggleVisibility(binding.missingRecyclerView)
        }
    }

    private fun toggleVisibility(recyclerView: View) {
        recyclerView.visibility = if (recyclerView.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun fetchTasks() {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.taskService.getTasks("Bearer $token").enqueue(object : Callback<ApiResponse<List<PreviewTask>>> {
                override fun onResponse(call: Call<ApiResponse<List<PreviewTask>>>, response: Response<ApiResponse<List<PreviewTask>>>) {
                    if (response.isSuccessful) {
                        val tasks = response.body()?.data ?: emptyList()
                        updateTaskLists(tasks)
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch tasks", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<PreviewTask>>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateTaskLists(tasks: List<PreviewTask>) {
        val todoTasks = tasks.filter { it.status == "To Do" }
        val inProgressTasks = tasks.filter { it.status == "In Progress" }
        val completedTasks = tasks.filter { it.status == "Completed" }
        val missingTasks = tasks.filter { it.status == "Missing" }

        todoAdapter.submitList(todoTasks)
        inProgressAdapter.submitList(inProgressTasks)
        completedAdapter.submitList(completedTasks)
        missingAdapter.submitList(missingTasks)
    }

    private fun navigateToTaskDetail(taskId: String) {
        val bundle = Bundle().apply {
            putString("task_id", taskId)
        }
        findNavController().navigate(R.id.action_navigation_tasks_to_taskDetailFragment, bundle)
    }

    // ==================== User ==================== //

    private fun getLoggedUser() {
        lifecycleScope.launch {
            val username = userPreferences.username.first()

            binding.welcomeUser.text = getString(R.string.greets_user, username)
        }
    }
}
