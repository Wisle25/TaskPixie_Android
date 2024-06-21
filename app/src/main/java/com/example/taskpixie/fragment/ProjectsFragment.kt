package com.example.taskpixie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taskpixie.adapter.ProjectsAdapter
import com.example.taskpixie.databinding.FragmentProjectsBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.R
import com.example.taskpixie.model.PreviewProject

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
        loadDummyData()

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
        projectsAdapter = ProjectsAdapter {
            findNavController().navigate(R.id.action_navigation_projects_to_projectDetailFragment)
        }
        binding.projectsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = projectsAdapter
        }
    }

    private fun loadDummyData() {
        val dummyProjects = listOf(
            PreviewProject(id = "1", name = "Project 1"),
            PreviewProject(id = "2", name = "Project 2"),
            PreviewProject(id = "3", name = "Project 3"),
            PreviewProject(id = "4", name = "Project 4"),
            PreviewProject(id = "5", name = "Project 5"),
            PreviewProject(id = "6", name = "Project 6"),
            PreviewProject(id = "7", name = "Project 7"),
            PreviewProject(id = "8", name = "Project 8"),
            PreviewProject(id = "9", name = "Project 9")
        )
        projectsAdapter.submitList(dummyProjects)
    }
}
