package com.example.taskpixie.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskpixie.databinding.TaskItemBinding
import com.example.taskpixie.model.PreviewTask

class TasksAdapter(
    private val clickListener: (PreviewTask) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    private var tasks: List<PreviewTask> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newTasks: List<PreviewTask>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], clickListener)
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: PreviewTask, clickListener: (PreviewTask) -> Unit) {
            binding.taskTitle.text = task.title
            binding.taskPriority.text = task.priority
            binding.taskDescription.text = task.description
            binding.taskProject.text = task.project

            // Binding On Click
            binding.root.setOnClickListener {
                clickListener(task)
            }
        }
    }
}
