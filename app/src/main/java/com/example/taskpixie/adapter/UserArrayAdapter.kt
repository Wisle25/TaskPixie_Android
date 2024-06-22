package com.example.taskpixie.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.taskpixie.model.User

class UserArrayAdapter(context: Context, users: List<User>) : ArrayAdapter<User>(context, android.R.layout.simple_dropdown_item_1line, users) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position)?.username
        return view
    }
}