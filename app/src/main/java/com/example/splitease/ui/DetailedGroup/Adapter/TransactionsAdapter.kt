package com.example.splitease.ui.DetailedGroup.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.splitease.Models.TransactionsModel
import com.example.splitease.R
import com.example.splitease.ui.AddUpdateTransaction.AddTransaction
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.io.BufferedReader
import java.io.StringReader
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Calendar

class TransactionsAdapter (
    var transactionItemModel: MutableList<TransactionsModel>,
    var mClickListener: ItemClickListener,
    val groupId: String) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {
    private lateinit var context: Context
    val db = FirebaseFirestore.getInstance()
    private var transactionIds = ArrayList<Any>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val trnTitle = itemView.findViewById<TextView>(R.id.trnTitle)
        val trnAmt = itemView.findViewById<TextView>(R.id.trnAmt)
        val trnDate = itemView.findViewById<TextView>(R.id.trnDate)
        val editTrn = itemView.findViewById<ImageButton>(R.id.editTrn)
        val deleteTrn = itemView.findViewById<ImageButton>(R.id.deleteTrn)
        val trnImg = itemView.findViewById<ImageView>(R.id.trnImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.adapter_transactions, parent, false)
        return ViewHolder(viewHolder)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.trnAmt.text = transactionItemModel[position].trn_amt.toString()
        holder.trnTitle.text = transactionItemModel[position].trn_desc

        val reader = BufferedReader(StringReader(transactionItemModel[position].trn_desc))
        val descArray = reader.readLines().flatMap { it.split(" ") }.toTypedArray()

        for(word in descArray){
            if (word.lowercase() == "hotel"){
                holder.trnImg.setImageResource(R.drawable.hotel)
            } else if (word.lowercase() == "food"){
                holder.trnImg.setImageResource(R.drawable.food)
            } else if (word.lowercase() == "train"){
                holder.trnImg.setImageResource(R.drawable.train)
            } else{
                holder.trnImg.setImageResource(R.drawable.receipt)
            }
        }

        val sdf = SimpleDateFormat("dd MMM, EEE")
        holder.trnDate.setText(sdf.format(transactionItemModel[position].trn_date))

        db.collection("UserData").document(transactionItemModel[position].lender)
            .collection("users")
            .document(transactionItemModel[position].lender)
            .get().addOnSuccessListener {
                holder.trnAmt.text = it.data?.get("user_name").toString() + " paid "+ transactionItemModel[position].trn_amt.toString()
            }

        holder.itemView.setOnClickListener {
            mClickListener.onItemClick(position)
            holder.deleteTrn.setOnClickListener {
                showDialogBox(position)
            }
            holder.editTrn.setOnClickListener {
                val trn = transactionItemModel[position]
                val intent = Intent(context, AddTransaction::class.java)
                intent.putExtra("trnId", trn.trn_id)
                intent.putExtra("groupId", groupId)
                intent.putExtra("oldAmt", trn.trn_amt)
                intent.putExtra("oldLender", trn.lender)
                intent.putExtra("oldBorrowers", trn.borrowers)
                intent.putExtra("oldDesc", trn.trn_desc)
                intent.putExtra("mode", "edit")
                context.startActivity(intent)
            }

            if (holder.editTrn.visibility == View.VISIBLE){
                holder.editTrn.visibility = View.GONE
                holder.deleteTrn.visibility = View.GONE
            } else {
                holder.editTrn.visibility = View.VISIBLE
                holder.deleteTrn.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return transactionItemModel.size
    }

    fun updateList(temp: MutableList<TransactionsModel>) {
        transactionItemModel = temp
        notifyDataSetChanged()
    }

    interface ItemClickListener{
        fun onItemClick(position: Int)
    }

    private fun showDialogBox(position: Int) {
        val trn = transactionItemModel[position]
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete")
        builder.setMessage("Are you sure you want to delete this transaction?")
        builder.setPositiveButton("Yes"){
                dialog: DialogInterface?, which: Int ->

            try {
                //Delete transaction from the transactions db
                db.collection("TransactionData").document(trn.trn_id)
                    .delete()

                //Delete transaction from lender
                db.collection("UserData").document(trn.lender)
                    .collection("users").document(trn.lender)
                    .get().addOnSuccessListener { it ->
                        transactionIds = (it.get("user_trn") as ArrayList<Any>)
                        transactionIds.remove(trn.trn_id)
                        db.collection("UserData").document(trn.lender)
                            .collection("users").document(trn.lender)
                            .update("user_trn", transactionIds)
                        var Balance = it.get("user_bal")
                        //If Split is equal
                        Balance = (Balance.toString().toDouble() + ((trn.trn_amt)*trn.borrowers.count())/(trn.borrowers.count()+1))
                        Balance = Math.round(Balance*100.0)/100.0
                        db.collection("UserData").document(trn.lender)
                            .collection("users").document(trn.lender)
                            .update("user_bal", Balance)
                    }

                //Delete transaction from borrowers
                for (borrower in trn.borrowers) {
                    db.collection("UserData").document(borrower)
                        .collection("users").document(borrower)
                        .get().addOnSuccessListener { it ->
                            transactionIds = (it.get("user_trn") as ArrayList<Any>)
                            transactionIds.remove(trn.trn_id)
                            db.collection("UserData").document(borrower)
                                .collection("users").document(borrower)
                                .update("user_trn", transactionIds)
                            var Balanceb = it.get("user_bal")
                            //If Split is equal
                            Balanceb = (Balanceb.toString().toDouble() - (trn.trn_amt / (trn.borrowers.count() + 1)))
                            Balanceb = Math.round(Balanceb*100.0)/100.0
                            db.collection("UserData").document(borrower)
                                .collection("users").document(borrower)
                                .update("user_bal", Balanceb)
                        }
                }

                //Delete transaction from the group
                db.collection("GroupData").document(groupId)
                    .get().addOnSuccessListener { it ->
                        transactionIds = (it.get("grp_transactions") as ArrayList<Any>)
                        transactionIds.remove(trn.trn_id)
                        db.collection("GroupData").document(groupId)
                            .update("grp_transactions", transactionIds)
//                      Update the balance to the group
                        var grpBalance = it.get("grp_total")
                        grpBalance = (grpBalance.toString().toDouble() - trn.trn_amt)
                        grpBalance = Math.round(grpBalance*100.0)/100.0
                        db.collection("GroupData").document(groupId)
                            .update("grp_total", grpBalance)
                    }
            }
            catch (e: Exception){
                System.err.print("Some Error Occurred")
            }
            dialog?.dismiss()
        }
        builder.setNegativeButton("No"){
                dialog, which-> dialog.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}