package com.example.splitease.ui.DetailedGroup.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.splitease.Models.UserDataModel
import com.example.splitease.R

class UsersAdapter (var userItemModel: MutableList<UserDataModel>)
    : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
    private lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userName = itemView.findViewById<TextView>(R.id.userName)
        val userBal = itemView.findViewById<TextView>(R.id.userBal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.adapter_users, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.userName.text = userItemModel[position].user_name
        holder.userBal.text = userItemModel[position].user_bal.toString()
    }

    override fun getItemCount(): Int {
        return userItemModel.size
    }

    fun updateList(temp: MutableList<UserDataModel>) {
        userItemModel = temp
        notifyDataSetChanged()
    }
}