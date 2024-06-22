package com.example.taskpixie.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taskpixie.databinding.FragmentTaskDetailBinding
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
import java.text.SimpleDateFormat
import java.util.Locale

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private var taskId: String? = null
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        taskId = arguments?.getString("task_id")

        taskId?.let {
            fetchTaskDetail(it)
        } ?: run {
            Toast.makeText(requireContext(), "Task ID not found", Toast.LENGTH_SHORT).show()
        }

        binding.updateTaskBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TASK_ID", taskId)
            }
            Log.d("TaskDetail", "taskID Detail: $taskId")
            findNavController().navigate(R.id.action_taskDetailFragment_to_formTaskFragment, bundle)
        }
        binding.deleteTaskBtn.setOnClickListener {
            taskId?.let { id -> deleteTask(id) }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTaskDetail(taskId: String) {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.taskService.getTaskDetail(taskId, "Bearer $token").enqueue(object : Callback<ApiResponse<Task>> {
                override fun onResponse(call: Call<ApiResponse<Task>>, response: Response<ApiResponse<Task>>) {
                    if (response.isSuccessful) {
                        val task = response.body()?.data
                        task?.let { updateUI(it) } ?: run {
                            Toast.makeText(requireContext(), "Task not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch task details", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Task>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun deleteTask(taskId: String) {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.taskService.deleteTask(taskId, "Bearer $token").enqueue(object : Callback<ApiResponse<String>> {
                override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateUI(task: Task) {
        binding.taskTitle.text = task.title
        binding.taskStatus.text = task.status
        binding.taskPriority.text = task.priority
        binding.taskCreatedAt.text = formatDate(task.createdAt)
        binding.taskUpdatedAt.text = formatDate(task.updatedAt)
        binding.taskDescription.text = task.description
        binding.taskDetail.text = displayOrNone(task.detail)
        binding.taskProject.text = displayOrNone(task.project)
        binding.taskAssignedTo.text = task.assignedTo?.joinToString(", ") ?: "None"

        when (task.status) {
            "To Do" -> binding.taskStatus.setBackgroundColor(Color.BLUE)
            "In Progress" -> binding.taskStatus.setBackgroundColor(Color.parseColor("#FFA500"))
            "Completed" -> binding.taskStatus.setBackgroundColor(Color.GREEN)
            "Canceled" -> binding.taskStatus.setBackgroundColor(Color.RED)
            "Missing" -> binding.taskStatus.setBackgroundColor(Color.GRAY)
        }
    }

    private fun displayOrNone(value: String?): String {
        return if (value.isNullOrEmpty()) "None" else value
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date ?: "")
    }
}
