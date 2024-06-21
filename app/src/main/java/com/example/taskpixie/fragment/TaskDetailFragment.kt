package com.example.taskpixie.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.taskpixie.databinding.FragmentTaskDetailBinding

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)

        // Dummy data
        val taskTitle = "Task Title"
        val taskStatus = "To Do"
        val taskPriority = "High"
        val taskCreatedAt = "2024-01-01"
        val taskUpdatedAt = "2024-01-05"
        val taskDescription = "This is a description of the task."
        val taskDetail = "These are more detailed information about the task."
        val taskProject = "Project 1"
        val taskAssignedTo = "Member 1, Member 2"

        // Set the data to the views
        binding.taskTitle.text = taskTitle
        binding.taskStatus.text = taskStatus
        binding.taskPriority.text = taskPriority
        binding.taskCreatedAt.text = taskCreatedAt
        binding.taskUpdatedAt.text = taskUpdatedAt
        binding.taskDescription.text = taskDescription
        binding.taskDetail.text = taskDetail
        binding.taskProject.text = taskProject
        binding.taskAssignedTo.text = taskAssignedTo

        // Set the background color based on status
        when (taskStatus) {
            "To Do" -> binding.taskStatus.setBackgroundColor(Color.BLUE)
            "In Progress" -> binding.taskStatus.setBackgroundColor(Color.parseColor("#FFA500"))
            "Completed" -> binding.taskStatus.setBackgroundColor(Color.GREEN)
            "Canceled" -> binding.taskStatus.setBackgroundColor(Color.RED)
            "Missing" -> binding.taskStatus.setBackgroundColor(Color.GRAY)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
