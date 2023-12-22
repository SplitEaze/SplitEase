package com.example.splitease.ui.DetailedGroup

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitease.Models.TransactionsModel
import com.example.splitease.Models.UserDataModel
import com.example.splitease.R
import com.example.splitease.ui.AddUpdateTransaction.AddTransaction
import com.example.splitease.ui.DetailedGroup.Adapter.TransactionsAdapter
import com.example.splitease.ui.DetailedGroup.Adapter.UsersAdapter
import com.example.splitease.ui.Login.Login
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.Exception
import java.util.Date

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
        WindowCompat.setDecorFitsSystemWindows(window, false)      //Make UI Full Screen
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView)

        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())      // Hide the system bars.

        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())      // Show the system bars.
        windowInsetsController?.isAppearanceLightNavigationBars = true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //remove night mode
        supportActionBar?.hide()

        val bundle = intent.extras
        groupId = bundle?.getString("groupId").toString()
        setGroupDetails(groupId)
        findViewById<RecyclerView>(R.id.rvUsers)?.layoutManager = LinearLayoutManager(this@DetailedGroup)
        findViewById<RecyclerView>(R.id.rvTransactions)?.layoutManager = LinearLayoutManager(this@DetailedGroup)
        getGroupUsers()
        getAllGroupTransactions()

        findViewById<Button>(R.id.addExpenseBtn).setOnClickListener {
            val intent = Intent(this@DetailedGroup, AddTransaction::class.java)
            intent.putExtra("groupId", groupId)
            intent.putExtra("mode", "add")
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

                    if (transactionIds.size == 0){
                        findViewById<TextView>(R.id.noExTv).visibility = View.VISIBLE
                        findViewById<RecyclerView>(R.id.rvTransactions).visibility = View.GONE
                    }
                    else {
                        transactionIds.reverse()
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
            transactionAdapter = TransactionsAdapter(transactionItemModel, this, groupId)
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

    }
}
