package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskpixie.R
import com.example.taskpixie.adapter.TasksAdapter
import com.example.taskpixie.databinding.FragmentProjectDetailBinding
import com.example.taskpixie.model.PreviewTask

class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksAdapter: TasksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)

        setupRecyclerView()
        loadDummyData()

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

    private fun loadDummyData() {
        val tasks = listOf(
            PreviewTask(id = "1", title = "Task 1", description = "Description 1", priority = "Low", status = "To Do", project = "Project 1"),
            PreviewTask(id = "2", title = "Task 2", description = "Description 2", priority = "High", status = "In Progress", project = "Project 1"),
            PreviewTask(id = "3", title = "Task 3", description = "Description 3", priority = "Medium", status = "Completed", project = "Project 1"),
            PreviewTask(id = "4", title = "Task 4", description = "Description 4", priority = "High", status = "Missing", project = "Project 1")
        )

        tasksAdapter.submitList(tasks)
    }
}
