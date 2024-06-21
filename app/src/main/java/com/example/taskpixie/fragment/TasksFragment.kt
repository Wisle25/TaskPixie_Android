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
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.Task
import com.example.taskpixie.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private lateinit var tasksAdapter: TasksAdapter

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        tasksAdapter = TasksAdapter()
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tasksRecyclerView.adapter = tasksAdapter

        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_tasks_to_addTaskFragment)
        }

        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            val userId = userPreferences.userId.first()
            if (!accessToken.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                fetchTasks("Bearer $accessToken", userId)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== Fetch Tasks ==================== //

    private fun fetchTasks(token: String, userId: String) {
        RetrofitClient.taskService.getTasksByUser(token, userId).enqueue(object : Callback<ApiResponse<List<Task>>> {
            override fun onResponse(call: Call<ApiResponse<List<Task>>>, response: Response<ApiResponse<List<Task>>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success") {
                        apiResponse.data?.let { tasks ->
                            tasksAdapter.submitList(tasks)
                        }
                    } else {
                        Toast.makeText(activity, apiResponse?.message ?: "Failed to fetch tasks!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch tasks!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Task>>>, t: Throwable) {
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
