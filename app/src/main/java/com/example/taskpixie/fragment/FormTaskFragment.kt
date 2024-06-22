package com.example.taskpixie.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taskpixie.R
import com.example.taskpixie.databinding.FragmentFormTaskBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.PreviewProject
import com.example.taskpixie.model.Task
import com.example.taskpixie.model.TaskPayload
import com.example.taskpixie.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class FormTaskFragment : Fragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    private var selectedProject: PreviewProject? = null
    private val projectMembers = mutableListOf<User>()
    private var selectedMemberId: String? = null
    private val projects = mutableListOf<PreviewProject>()
    private var taskId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        setupAddMemberButton()
        setupDatePicker()

        taskId = arguments?.getString("TASK_ID")
        Log.d("FormTaskFragment", "taskId: $taskId")

        binding.submitTaskBtn.setOnClickListener {
            submitTask()
        }

        fetchProjects()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchProjects() {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.projectService.getProjects("Bearer $token").enqueue(object : Callback<ApiResponse<List<PreviewProject>>> {
                override fun onResponse(call: Call<ApiResponse<List<PreviewProject>>>, response: Response<ApiResponse<List<PreviewProject>>>) {
                    if (response.isSuccessful) {
                        projects.clear()
                        projects.addAll(response.body()?.data ?: emptyList())
                        setupProjectSpinner()
                        taskId?.let { fetchTaskDetail(it) } // Fetch task details after loading projects
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch projects", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<PreviewProject>>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupProjectSpinner() {
        val projectNames = projects.map { it.title }.toMutableList()
        projectNames.add(0, getString(R.string.select_project))

        val projectsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            projectNames
        )
        projectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.taskProjectSpinner.adapter = projectsAdapter

        binding.taskProjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProject = if (position > 0) projects[position - 1] else null
                if (selectedProject != null) {
                    binding.taskAssignedToLabel.visibility = View.VISIBLE
                    binding.taskAssignedToContainer.visibility = View.VISIBLE
                    binding.addMemberBtn.visibility = View.VISIBLE
                    binding.taskAssignedToContainer.removeAllViews()
                    selectedMemberId = null
                    fetchProjectMembers(selectedProject!!.id)
                } else {
                    binding.taskAssignedToLabel.visibility = View.GONE
                    binding.taskAssignedToContainer.visibility = View.GONE
                    binding.addMemberBtn.visibility = View.GONE
                    selectedMemberId = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.taskAssignedToLabel.visibility = View.GONE
                binding.taskAssignedToContainer.visibility = View.GONE
                binding.addMemberBtn.visibility = View.GONE
                selectedMemberId = null
            }
        }
    }

    private fun fetchProjectMembers(projectId: String) {
        lifecycleScope.launch {
            RetrofitClient.projectService.getProjectMembers(projectId).enqueue(object : Callback<ApiResponse<List<User>>> {
                override fun onResponse(call: Call<ApiResponse<List<User>>>, response: Response<ApiResponse<List<User>>>) {
                    if (response.isSuccessful) {
                        val users = response.body()?.data ?: emptyList()
                        projectMembers.clear()
                        projectMembers.addAll(users)
                        setupMemberSpinner()
                        // Set member spinner selection after members are loaded
                        selectedMemberId?.let {
                            val memberPosition = projectMembers.indexOfFirst { member -> member.id == it }
                            if (memberPosition != -1) {
                                (binding.taskAssignedToContainer.getChildAt(0) as Spinner).setSelection(memberPosition)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch project members", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<User>>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchTaskDetail(taskId: String) {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.taskService.getTaskDetail(taskId, "Bearer $token").enqueue(object : Callback<ApiResponse<Task>> {
                override fun onResponse(call: Call<ApiResponse<Task>>, response: Response<ApiResponse<Task>>) {
                    if (response.isSuccessful) {
                        val task = response.body()?.data
                        task?.let { populateTaskDetails(it) } ?: run {
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

    private fun populateTaskDetails(task: Task) {
        binding.taskTitleInput.setText(task.title)
        binding.taskDescriptionInput.setText(task.description)
        binding.taskDetailInput.setText(task.detail)
        binding.taskPrioritySpinner.setSelection(resources.getStringArray(R.array.task_priorities).indexOf(task.priority))
        binding.taskDueDateInput.setText(task.dueDate?.substring(0, 10)) // Format to YYYY-MM-DD
        binding.taskStatusSpinner.setSelection(resources.getStringArray(R.array.task_statuses).indexOf(task.status))
        selectedProject = projects.find { it.id == task.projectId }
        selectedMemberId = task.assignedTo?.firstOrNull()

        // Set project spinner selection
        selectedProject?.let {
            val projectPosition = projects.indexOf(it) + 1
            binding.taskProjectSpinner.setSelection(projectPosition)
        }

        // Fetch project members again to ensure the members are available before setting up the spinner
        selectedProject?.id?.let {
            fetchProjectMembers(it)
        }
    }

    private fun setupMemberSpinner() {
        val memberNames = projectMembers.map { it.username }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, memberNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.taskAssignedToContainer.removeAllViews()
        val memberSpinner = Spinner(requireContext())
        memberSpinner.adapter = adapter

        memberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMemberId = projectMembers[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.taskAssignedToContainer.addView(memberSpinner)

        // Set member spinner selection if a member is already assigned
        selectedMemberId?.let {
            val memberPosition = projectMembers.indexOfFirst { member -> member.id == it }
            if (memberPosition != -1) {
                memberSpinner.setSelection(memberPosition)
            }
        }
    }

    private fun setupAddMemberButton() {
        binding.addMemberBtn.setOnClickListener {
            addMemberField()
        }
    }

    private fun addMemberField() {
        val memberNames = projectMembers.map { it.username }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, memberNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val memberSpinner = Spinner(requireContext())
        memberSpinner.adapter = adapter

        memberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMemberId = projectMembers[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.taskAssignedToContainer.addView(memberSpinner)
    }

    private fun setupDatePicker() {
        binding.taskDueDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                binding.taskDueDateInput.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.show()
        }
    }

    private fun submitTask() {
        val title = binding.taskTitleInput.text.toString()
        val description = binding.taskDescriptionInput.text.toString()
        val detail = binding.taskDetailInput.text.toString()
        val priority = binding.taskPrioritySpinner.selectedItem.toString()
        val dueDate = binding.taskDueDateInput.text.toString()
        val status = binding.taskStatusSpinner.selectedItem.toString()
        val projectId = selectedProject?.id
        val assignedTo = selectedMemberId?.let { listOf(it) }

        if (projectId != null && assignedTo.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please assign members to the project", Toast.LENGTH_SHORT).show()
            return
        }

        val payload = TaskPayload(
            title = title,
            description = description,
            detail = detail,
            priority = priority,
            dueDate = dueDate,
            status = status,
            projectId = projectId,
            assignedTo = assignedTo
        )

        if (taskId == null) {
            addTask(payload)
        } else {
            updateTask(taskId!!, payload)
        }
    }

    private fun addTask(payload: TaskPayload) {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.taskService.addTask("Bearer $token", payload).enqueue(object : Callback<ApiResponse<String>> {
                override fun onResponse(
                    call: Call<ApiResponse<String>>,
                    response: Response<ApiResponse<String>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Task added successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to add task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateTask(taskId: String, payload: TaskPayload) {
        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.taskService.updateTask("Bearer $token", taskId, payload).enqueue(object : Callback<ApiResponse<String>> {
                override fun onResponse(
                    call: Call<ApiResponse<String>>,
                    response: Response<ApiResponse<String>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
