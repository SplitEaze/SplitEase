package com.example.splitease.ui.DetailedGroup.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.splitease.Models.TransactionsModel
import com.example.splitease.R

class TransactionsAdapter (var transactionItemModel: MutableList<TransactionsModel>)
    : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {
    private lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val trnAmt = itemView.findViewById<TextView>(R.id.trnAmt)
        val totalSpends = itemView.findViewById<TextView>(R.id.totalSpends)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.adapter_transactions, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("hereiam", "getTransactionsIds: "+transactionItemModel[position].trn_amt)
        holder.trnAmt.text = transactionItemModel[position].trn_amt.toString()
//        holder.totalSpends.text = transactionItemModel[position].grp_total.toString()
    }

    override fun getItemCount(): Int {
        return transactionItemModel.size
    }

    fun updateList(temp: MutableList<TransactionsModel>) {
        transactionItemModel = temp
        notifyDataSetChanged()
    }
}