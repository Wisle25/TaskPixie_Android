package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskpixie.adapter.TasksAdapter
import com.example.taskpixie.databinding.FragmentTasksBinding
import com.example.taskpixie.R
import com.example.taskpixie.model.PreviewTask

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var todoAdapter: TasksAdapter
    private lateinit var inProgressAdapter: TasksAdapter
    private lateinit var completedAdapter: TasksAdapter
    private lateinit var missingAdapter: TasksAdapter

    private lateinit var tasks: List<PreviewTask>

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)

        setupRecyclerViews()
        setupToggleButtons()

        // Use dummy data for now
        loadDummyData()

        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_tasks_to_addTaskFragment)
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
            findNavController().navigate(R.id.action_navigation_tasks_to_taskDetailFragment)
        }
        inProgressAdapter = TasksAdapter {
            findNavController().navigate(R.id.action_navigation_tasks_to_taskDetailFragment)
        }
        completedAdapter = TasksAdapter {
            findNavController().navigate(R.id.action_navigation_tasks_to_taskDetailFragment)
        }
        missingAdapter = TasksAdapter {
            findNavController().navigate(R.id.action_navigation_tasks_to_taskDetailFragment)
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

    private fun loadDummyData() {
        tasks = listOf(
            PreviewTask(id = "1", title = "Task 1", description = "Description 1", status = "To Do", priority = "Low", project = "Project 1"),
            PreviewTask(id = "2", title = "Task 2", description = "Description 2", status = "In Progress", priority = "High", project = "Project 2"),
            PreviewTask(id = "3", title = "Task 3", description = "Description 3", status = "Completed", priority = "Medium", project = "Project 3"),
            PreviewTask(id = "4", title = "Task 4", description = "Description 4", status = "Missing", priority = "High", project = "Project 4"),
            PreviewTask(id = "5", title = "Task 5", description = "Description 5", status = "To Do", priority = "Low", project = "Project 1"),
            PreviewTask(id = "6", title = "Task 6", description = "Description 6", status = "In Progress", priority = "High", project = "Project 2"),
            PreviewTask(id = "7", title = "Task 7", description = "Description 7", status = "Completed", priority = "Medium", project = "Project 3"),
            PreviewTask(id = "8", title = "Task 8", description = "Description 8", status = "Missing", priority = "High", project = "Project 4")
        )

        val todoTasks = tasks.filter { it.status == "To Do" }
        val inProgressTasks = tasks.filter { it.status == "In Progress" }
        val completedTasks = tasks.filter { it.status == "Completed" }
        val missingTasks = tasks.filter { it.status == "Missing" }

        todoAdapter.submitList(todoTasks)
        inProgressAdapter.submitList(inProgressTasks)
        completedAdapter.submitList(completedTasks)
        missingAdapter.submitList(missingTasks)
    }
}
