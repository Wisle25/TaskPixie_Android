package com.example.taskpixie.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.taskpixie.R
import com.example.taskpixie.databinding.ProjectItemBinding
import com.example.taskpixie.model.Project

class ProjectsAdapter : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    private val projects: MutableList<Project> = mutableListOf()

    fun submitList(newProjects: List<Project>) {
        projects.clear()
        projects.addAll(newProjects)
        notifyDataSetChanged()
    }

    fun addProject(project: Project) {
        projects.add(project)
        notifyItemInserted(projects.size - 1)
    }

    fun updateProject(position: Int, project: Project) {
        projects[position] = project
        notifyItemChanged(position)
    }

    fun removeProject(position: Int) {
        projects.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ProjectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(projects[position])
    }

    override fun getItemCount(): Int = projects.size

    class ProjectViewHolder(private val binding: ProjectItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(project: Project) {
            binding.projectName.text = project.name

            // Set the drawable size programmatically
            val drawable: Drawable? = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_projects)
            drawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it)
                val width = 96  // Width in pixels
                val height = 96  // Height in pixels
                wrappedDrawable.setBounds(0, 0, width, height)
                binding.projectName.setCompoundDrawables(null, wrappedDrawable, null, null)
            }
        }
    }
}
