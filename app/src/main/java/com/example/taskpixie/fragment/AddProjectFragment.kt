package com.example.taskpixie.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.taskpixie.databinding.FragmentAddProjectBinding
import com.example.taskpixie.R

class AddProjectFragment : Fragment() {

    private var _binding: FragmentAddProjectBinding? = null
    private val binding get() = _binding!!

    // Dummy data for project members
    private val allMembers = listOf("Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Hank")
    private val assignedMembers = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProjectBinding.inflate(inflater, container, false)

        setupAutoCompleteTextView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAutoCompleteTextView() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allMembers)
        binding.autoCompleteTextView.setAdapter(adapter)

        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedMember = parent.getItemAtPosition(position).toString()
            if (selectedMember !in assignedMembers) {
                assignedMembers.add(selectedMember)
                addMemberView(selectedMember)
                binding.autoCompleteTextView.text.clear()
            }
        }
    }

    private fun addMemberView(member: String) {
        val memberView = LayoutInflater.from(requireContext()).inflate(R.layout.member_item, binding.projectAssignedToContainer, false)

        val memberNameTextView: TextView = memberView.findViewById(R.id.member_name)
        val removeMemberButton: ImageButton = memberView.findViewById(R.id.remove_member_btn)

        memberNameTextView.text = member
        removeMemberButton.setOnClickListener {
            assignedMembers.remove(member)
            binding.projectAssignedToContainer.removeView(memberView)
        }

        binding.projectAssignedToContainer.visibility = View.VISIBLE
        binding.projectAssignedToContainer.addView(memberView)
    }
}
