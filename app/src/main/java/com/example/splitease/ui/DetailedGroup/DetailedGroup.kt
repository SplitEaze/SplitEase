package com.example.splitease.ui.DetailedGroup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitease.Models.TransactionsModel
import com.example.splitease.Models.UserDataModel
import com.example.splitease.R
import com.example.splitease.ui.AddUpdateTransaction.AddTransaction
import com.example.splitease.ui.DetailedGroup.Adapter.TransactionsAdapter
import com.example.splitease.ui.DetailedGroup.Adapter.UsersAdapter
import com.example.splitease.ui.AddUpdateTransaction.EditTransaction
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class DetailedGroup : AppCompatActivity(), TransactionsAdapter.ItemClickListener {

    private var userDataList: MutableList<UserDataModel> = ArrayList()
    private lateinit var userItemModel: MutableList<UserDataModel>
    private var transactionDataList: MutableList<TransactionsModel> = ArrayList()
    private lateinit var transactionItemModel : MutableList<TransactionsModel>
    val db = FirebaseFirestore.getInstance()
    var userAdaper: UsersAdapter?= null
    var transactionAdapter: TransactionsAdapter ?= null
    private var groupId = ""
    private var transactionIds = ArrayList<Any>()
    private var userIds = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_group)

        val bundle = intent.extras
        groupId = bundle?.getString("groupId").toString()

        setGroupDetails(groupId)
        findViewById<RecyclerView>(R.id.rvUsers)?.layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.rvTransactions)?.layoutManager = LinearLayoutManager(this)
        getGroupUsers()
        getAllGroupTransactions()

        findViewById<Button>(R.id.addExpenseBtn).setOnClickListener {
            val intent = Intent(this@DetailedGroup, AddTransaction::class.java)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }
    }

    private fun setGroupDetails(groupId: String) {

        try {
            db.collection("GroupData").document(groupId)
                .get().addOnSuccessListener {
                    findViewById<TextView>(R.id.heading).text = it.get("grp_name").toString()
                    findViewById<TextView>(R.id.totalOwedMoney).text = it.get("grp_total").toString()
                }
        }
        catch (e: Exception){
            System.err.print("Some Error Occurred")
        }
    }

    private fun getGroupUsers() {
        try {
            db.collection("GroupData").document(groupId)
                .get().addOnSuccessListener { it ->
                    userIds = (it.get("grp_users") as ArrayList<Any>)
                    for (tid in userIds){
                        db.collection("UserData").document(tid.toString())
                            .collection("users")
                            .get().addOnSuccessListener {
                                userItemModel = it.toObjects(UserDataModel::class.java)
                                userDataList.addAll(userItemModel)
                                setUserAdapter()
                            }
                    }
                }
        }
        catch (e: Exception){
            System.err.print("Some Error Occurred")
        }
    }

    private fun getAllGroupTransactions() {
        try {
            db.collection("GroupData").document(groupId)
                .get().addOnSuccessListener { it ->
                    transactionIds = (it.get("grp_transactions") as ArrayList<Any>)
                    for (tid in transactionIds){
                        db.collection("TransactionData").document(tid.toString())
                            .collection("trns")
                            .get().addOnSuccessListener {
                                transactionItemModel = it.toObjects(TransactionsModel::class.java)
                                transactionDataList.addAll(transactionItemModel)
                                setTransactionAdapter()
                            }
                    }
                }
        }
        catch (e: Exception){
            System.err.print("Some Error Occurred")
        }
    }

    private fun setTransactionAdapter() {
        if(transactionAdapter != null){
            transactionAdapter?.updateList(transactionDataList)
            findViewById<RecyclerView>(R.id.rvTransactions)?.adapter = transactionAdapter
        }
        else {
            transactionAdapter = TransactionsAdapter(transactionItemModel, this)
            findViewById<RecyclerView>(R.id.rvTransactions)?.adapter = transactionAdapter
        }
    }

    private fun setUserAdapter() {
        if(userAdaper != null){
            userAdaper?.updateList(userDataList)
            findViewById<RecyclerView>(R.id.rvUsers)?.adapter = userAdaper
        }
        else {
            userAdaper = UsersAdapter(userDataList)
            findViewById<RecyclerView>(R.id.rvUsers)?.adapter = userAdaper
        }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this@DetailedGroup, EditTransaction::class.java)
        intent.putExtra("trnId", transactionDataList[position].trn_id)
        startActivity(intent)
    }
}