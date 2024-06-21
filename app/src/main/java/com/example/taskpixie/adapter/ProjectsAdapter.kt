package com.example.taskpixie.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.taskpixie.R
import com.example.taskpixie.databinding.ProjectItemBinding
import com.example.taskpixie.model.PreviewProject

class ProjectsAdapter(
    private val clickListener: (PreviewProject) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    private var projects: List<PreviewProject> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newProjects: List<PreviewProject>) {
        projects = newProjects
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ProjectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(projects[position], clickListener)
    }

    override fun getItemCount(): Int = projects.size

    class ProjectViewHolder(private val binding: ProjectItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(project: PreviewProject, clickListener: (PreviewProject) -> Unit) {
            binding.projectName.text = project.name

            // Set the drawable size programmatically
            val drawable: Drawable? = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_project)
            drawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it)
                val width = 96  // Width in pixels
                val height = 96  // Height in pixels
                wrappedDrawable.setBounds(0, 0, width, height)
                binding.projectName.setCompoundDrawables(null, wrappedDrawable, null, null)
            }

            binding.root.setOnClickListener {
                clickListener(project)
            }
        }
    }
}
