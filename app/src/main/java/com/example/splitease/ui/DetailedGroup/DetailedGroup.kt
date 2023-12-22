package com.example.splitease.ui.DetailedGroup

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
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

//        findViewById<ImageButton>(R.id.deleteTrn)?.setOnClickListener {
////            showDialogBox(transactionDataList[position])
//        }

        findViewById<ImageButton>(R.id.editTrn)?.setOnClickListener {
            val intent = Intent(this@DetailedGroup, AddTransaction::class.java)
            intent.putExtra("trnId", transactionDataList[position].trn_id)
            intent.putExtra("groupId", groupId)
            intent.putExtra("oldAmt", transactionDataList[position].trn_amt)
            intent.putExtra("oldLender", transactionDataList[position].lender)
            intent.putExtra("oldBorrowers", transactionDataList[position].borrowers)
            intent.putExtra("oldDesc", transactionDataList[position].trn_desc)
            intent.putExtra("mode", "edit")
            startActivity(intent)
        }
    }

//    private fun showDialogBox(trn: TransactionsModel) {
//        val builder = AlertDialog.Builder(this@DetailedGroup)
//        builder.setTitle("Delete")
//        builder.setMessage("Are you sure you want to delete this transaction?")
//        builder.setPositiveButton("Yes"){
//            dialog: DialogInterface?, which: Int ->
//
//            try {
//                //Delete transaction from the transactions db
//                db.collection("TransactionData").document(trn.trn_id)
//                    .delete()
//
//                //Delete transaction from lender
//                db.collection("UserData").document(trn.lender)
//                    .collection("users").document(trn.lender)
//                    .get().addOnSuccessListener { it ->
//                        transactionIds = (it.get("user_trn") as ArrayList<Any>)
//                        transactionIds.remove(trn.trn_id)
//                        db.collection("UserData").document(trn.lender)
//                            .collection("users").document(trn.lender)
//                            .update("user_trn", transactionIds)
//                        var Balance = it.get("user_bal")
//                        //If Split is equal
//                        Balance = (Balance.toString().toDouble() + ((trn.trn_amt)*trn.borrowers.count())/(trn.borrowers.count()+1))
//                        Balance = Math.round(Balance*100.0)/100.0
//                        db.collection("UserData").document(trn.lender)
//                            .collection("users").document(trn.lender)
//                            .update("user_bal", Balance)
//                    }
//
//                //Delete transaction from borrowers
//                for (borrower in trn.borrowers) {
//                    db.collection("UserData").document(borrower)
//                        .collection("users").document(borrower)
//                        .get().addOnSuccessListener { it ->
//                            transactionIds = (it.get("user_trn") as ArrayList<Any>)
//                            transactionIds.remove(trn.trn_id)
//                            db.collection("UserData").document(borrower)
//                                .collection("users").document(borrower)
//                                .update("user_trn", transactionIds)
//                            var Balanceb = it.get("user_bal")
//                            //If Split is equal
//                            Balanceb = (Balanceb.toString().toDouble() - (trn.trn_amt / (trn.borrowers.count() + 1)))
//                            Balanceb = Math.round(Balanceb*100.0)/100.0
//                            db.collection("UserData").document(borrower)
//                                .collection("users").document(borrower)
//                                .update("user_bal", Balanceb)
//                        }
//                }
//
//                //Delete transaction from the group
//                db.collection("GroupData").document(groupId)
//                    .get().addOnSuccessListener { it ->
//                        transactionIds = (it.get("grp_transactions") as ArrayList<Any>)
//                        transactionIds.remove(trn.trn_id)
//                        db.collection("GroupData").document(groupId)
//                            .update("grp_transactions", transactionIds)
////                      Update the balance to the group
//                        var grpBalance = it.get("grp_total")
//                        grpBalance = (grpBalance.toString().toDouble() - trn.trn_amt)
//                        grpBalance = Math.round(grpBalance*100.0)/100.0
//                        db.collection("GroupData").document(groupId)
//                            .update("grp_total", grpBalance)
//                    }
//            }
//            catch (e: Exception){
//                System.err.print("Some Error Occurred")
//            }
//            dialog?.dismiss()
//        }
//        builder.setNegativeButton("No"){
//                dialog, which-> dialog.dismiss()
//        }
//        val alertDialog: AlertDialog = builder.create()
//        alertDialog.setCancelable(false)
//        alertDialog.show()
//    }
}
