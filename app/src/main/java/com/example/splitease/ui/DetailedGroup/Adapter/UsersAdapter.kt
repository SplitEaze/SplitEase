package com.example.splitease.ui.DetailedGroup.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.splitease.Models.UserDataModel
import com.example.splitease.R
import kotlin.math.abs

class UsersAdapter (var userItemModel: MutableList<UserDataModel>)
    : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
    private lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userBal = itemView.findViewById<TextView>(R.id.userBal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.adapter_users, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.userBal.text = "${userItemModel[position].user_name} : ₹${userItemModel[position].user_bal} overall"
        if (userItemModel[position].user_bal.toString().toFloat() < 0.0){
            holder.userBal.text = "${userItemModel[position].user_name}. is owed ₹${abs(userItemModel[position].user_bal)} overall"
        } else if (userItemModel[position].user_bal.toString().toDouble() == 0.0){
            holder.userBal.visibility = View.GONE
        } else {
            holder.userBal.text = "${userItemModel[position].user_name}. owes ₹${abs(userItemModel[position].user_bal)}"
        }
    }

    override fun getItemCount(): Int {
        return userItemModel.size
    }

    fun updateList(temp: MutableList<UserDataModel>) {
        userItemModel = temp
        notifyDataSetChanged()
    }
}