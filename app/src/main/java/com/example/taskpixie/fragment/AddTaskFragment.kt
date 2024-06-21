package com.example.taskpixie.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.taskpixie.databinding.FragmentAddTaskBinding
import com.example.taskpixie.R
import java.util.Calendar

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    // Dummy data for project members
    private val projectMembers = mapOf(
        "Project 1" to listOf("Member 1", "Member 2", "Member 3"),
        "Project 2" to listOf("Member 4", "Member 5"),
        "Project 3" to listOf("Member 6", "Member 7", "Member 8"),
        "Project 4" to listOf("Member 9")
    )

    private var selectedProject: String? = null
    private val assignedMembers = mutableListOf<String>()

    // ==================== Lifecycles ==================== //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)

        setupProjectSpinner()
        setupAddMemberButton()
        setupDatePicker()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==================== Commons ==================== //

    private fun setupProjectSpinner() {
        val projectsAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.task_projects,
            android.R.layout.simple_spinner_item
        )
        projectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.taskProjectSpinner.adapter = projectsAdapter

        binding.taskProjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProject = parent?.getItemAtPosition(position).toString()
                if (selectedProject != getString(R.string.select_project)) {
                    binding.taskAssignedToLabel.visibility = View.VISIBLE
                    binding.taskAssignedToContainer.visibility = View.VISIBLE
                    binding.addMemberBtn.visibility = View.VISIBLE
                    binding.taskAssignedToContainer.removeAllViews() // Clear previous fields
                    assignedMembers.clear()
                    addMemberField()
                } else {
                    binding.taskAssignedToLabel.visibility = View.GONE
                    binding.taskAssignedToContainer.visibility = View.GONE
                    binding.addMemberBtn.visibility = View.GONE
                    assignedMembers.clear()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.taskAssignedToLabel.visibility = View.GONE
                binding.taskAssignedToContainer.visibility = View.GONE
                binding.addMemberBtn.visibility = View.GONE
                assignedMembers.clear()
            }
        }
    }

    // ==================== Assigned To's Handler

    private fun setupAddMemberButton() {
        binding.addMemberBtn.setOnClickListener {
            addMemberField()
        }
    }

    private fun addMemberField() {
        val members = projectMembers[selectedProject] ?: emptyList()
        val availableMembers = members.filter { it !in assignedMembers }
        val container = binding.taskAssignedToContainer

        if (availableMembers.isNotEmpty()) {
            val memberSpinner = Spinner(requireContext())
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availableMembers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            memberSpinner.adapter = adapter

            memberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedMember = parent?.getItemAtPosition(position).toString()
                    if (selectedMember !in assignedMembers) {
                        assignedMembers.add(selectedMember)
                        updateMemberFields()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No-op
                }
            }

            container.addView(memberSpinner)
        }

        updateAddMemberButtonVisibility(members)
    }

    private fun updateMemberFields() {
        val members = projectMembers[selectedProject] ?: emptyList()
        val container = binding.taskAssignedToContainer
        container.removeAllViews()
        assignedMembers.forEach { assignedMember ->
            val memberSpinner = Spinner(requireContext())
            val availableMembers = members.filter { it != assignedMember && it !in assignedMembers }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(assignedMember) + availableMembers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            memberSpinner.adapter = adapter
            memberSpinner.setSelection(0) // Set the assigned member as the selected item

            memberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedMember = parent?.getItemAtPosition(position).toString()
                    if (assignedMember != selectedMember) {
                        assignedMembers.remove(assignedMember)
                        assignedMembers.add(selectedMember)
                        updateMemberFields()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No-op
                }
            }

            container.addView(memberSpinner)
        }
        updateAddMemberButtonVisibility(members)
    }

    private fun updateAddMemberButtonVisibility(members: List<String>) {
        binding.addMemberBtn.visibility = if (assignedMembers.size < members.size) View.VISIBLE else View.GONE
    }

    // ==================== Due Date Handler ==================== //

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
}
