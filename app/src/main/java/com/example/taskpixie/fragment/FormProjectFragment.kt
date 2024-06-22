package com.example.taskpixie.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taskpixie.R
import com.example.taskpixie.adapter.UserArrayAdapter
import com.example.taskpixie.databinding.FragmentFormProjectBinding
import com.example.taskpixie.datastore.UserPreferences
import com.example.taskpixie.http.RetrofitClient
import com.example.taskpixie.model.ProjectPayload
import com.example.taskpixie.model.User
import com.example.taskpixie.model.ApiResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FormProjectFragment : Fragment() {

    private var _binding: FragmentFormProjectBinding? = null
    private val binding get() = _binding!!

    private val assignedMembers = mutableListOf<User>()
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormProjectBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        setupAutoCompleteTextView()
        binding.submitProjectBtn.setOnClickListener { handleSubmitProject() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAutoCompleteTextView() {
        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    fetchUsersByUsername(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedUser = parent.getItemAtPosition(position) as User
            if (selectedUser !in assignedMembers) {
                assignedMembers.add(selectedUser)
                addMemberView(selectedUser)
                binding.autoCompleteTextView.text.clear()
            }
        }
    }

    private fun fetchUsersByUsername(username: String) {
        RetrofitClient.userService.searchUsersByUsername(username).enqueue(object : Callback<ApiResponse<List<User>>> {
            override fun onResponse(call: Call<ApiResponse<List<User>>>, response: Response<ApiResponse<List<User>>>) {
                if (response.isSuccessful) {
                    val users = response.body()?.data ?: emptyList()
                    val adapter = UserArrayAdapter(requireContext(), users)
                    binding.autoCompleteTextView.setAdapter(adapter)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("FormProjectFragment", "Error fetching users: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<User>>>, t: Throwable) {
                Log.e("FormProjectFragment", "Error fetching users", t)
            }
        })
    }

    private fun addMemberView(user: User) {
        val memberView = LayoutInflater.from(requireContext()).inflate(R.layout.member_item, binding.projectAssignedToContainer, false)

        val memberNameTextView: TextView = memberView.findViewById(R.id.member_name)
        val removeMemberButton: ImageButton = memberView.findViewById(R.id.remove_member_btn)

        memberNameTextView.text = user.username
        removeMemberButton.setOnClickListener {
            assignedMembers.remove(user)
            binding.projectAssignedToContainer.removeView(memberView)
        }

        binding.projectAssignedToContainer.visibility = View.VISIBLE
        binding.projectAssignedToContainer.addView(memberView)
    }

    private fun handleSubmitProject() {
        val title = binding.projectTitleInput.text.toString().trim()
        val detail = binding.projectDetailInput.text.toString().trim()
        val priority = binding.projectPrioritySpinner.selectedItem.toString()
        val status = binding.projectStatusSpinner.selectedItem.toString()

        if (title.isEmpty()) {
            binding.projectTitleInput.error = "Title is required"
            return
        }

        if (detail.isEmpty()) {
            binding.projectDetailInput.error = "Detail is required"
            return
        }

        val memberIds = assignedMembers.map { it.id }

        val payload = ProjectPayload(
            title = title,
            detail = detail,
            priority = priority,
            status = status,
            members = memberIds
        )

        lifecycleScope.launch {
            val token = userPreferences.accessToken.first() ?: return@launch
            RetrofitClient.projectService.addProject("Bearer $token", payload).enqueue(object : Callback<ApiResponse<String>> {
                override fun onResponse(
                    call: Call<ApiResponse<String>>,
                    response: Response<ApiResponse<String>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Project added successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_addProjectFragment_to_navigation_projects)
                    } else {
                        Log.e("FormProjectFragment", "Error adding project: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                    Log.e("FormProjectFragment", "Error adding project", t)
                }
            })
        }
    }
}
