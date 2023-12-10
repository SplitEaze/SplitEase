package com.example.splitease.ui.AddUpdateTransaction

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.splitease.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.util.Date

class AddTransaction : AppCompatActivity(){

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    val db = Firebase.firestore
    private var groupId = ""
    private var transactionIds = ArrayList<Any>()
    private var users = ArrayList<Any>()
    private var userNameArray = ArrayList<Any>()
    private var user_name : Any? = null
    private var selectedUser = ""
    private var userIds = ArrayList<Any>()
    private var numberOfBorrowers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        auth = Firebase.auth

        val desc = findViewById<EditText>(R.id.Desc)
        val amt = findViewById<EditText>(R.id.Amt)
        val bundle = intent.extras
        groupId = bundle?.getString("groupId").toString()
        //Add transaction to the user
        selectLenderUser()

        findViewById<Button>(R.id.saveTrnBtn).setOnClickListener {
            if (desc.text.isNullOrEmpty()){
                desc.error = "Enter the description"
            } else if (amt.text.isNullOrEmpty()){
                amt.error = "Enter the amount"
            } else{
                //Add transaction to transactionData, Particular user's and group total

                //Get only the borrowers list
                val borrowers = arrayListOf<Any>()
                userIds.filterTo(borrowers) { it != selectedUser }


                val trnData = HashMap<String, Any>()

                trnData["trn_desc"] = desc.text.toString()
                trnData["trn_amt"] = amt.text.toString().toDouble()
                trnData["trn_date"] = Date()
                trnData["lender"] = selectedUser
                trnData["borrowers"] = borrowers

                //Create the document to get the Transaction ID
                val newTransactionRef = db.collection("TransactionData").document()

                //Add transaction to the TransactionData
                newTransactionRef.collection("trns")
                    .document(newTransactionRef.id)
                    .set(trnData)

                //Add transaction to the group
                addTransactionToGroup(newTransactionRef.id, amt)

                //Add transaction to the lender user
                addTransactionToLenderUser(selectedUser, borrowers, amt)

                //Add transaction to the borrower user
                addTransactionToBorrowerUser(borrowers, amt)

                finish()
            }
        }
    }

    private fun addTransactionToBorrowerUser(borrowers: ArrayList<Any>, amt: EditText?) {
        //Splitting the amount equally

        try {
            for (borrower in borrowers) {
                db.collection("UserData").document(borrower.toString())
                    .collection("users").document(borrower.toString())
                    .get().addOnSuccessListener { it ->
                        var Balance = it.get("user_bal")
                        //If Split is equal
                        Balance = (Balance.toString().toFloat() + (amt?.text.toString()
                            .toFloat() / (numberOfBorrowers + 1)))
                        db.collection("UserData").document(borrower.toString())
                            .collection("users").document(borrower.toString())
                            .update("user_bal", Balance)
                    }
            }
        }
        catch (e: Exception){
            System.err.print("Some Error Occurred")
        }
    }

    private fun addTransactionToLenderUser(
        selectedUser: String,
        borrowers: ArrayList<Any>,
        amt: EditText
    ) {
        try {
            numberOfBorrowers = borrowers.count()
            db.collection("UserData").document(selectedUser)
                .collection("users").document(selectedUser)
                .get().addOnSuccessListener { it ->
                    var Balance = it.get("user_bal")
                    //If Split is equal

                    Balance = (Balance.toString().toFloat() - ((amt.text.toString().toFloat())*numberOfBorrowers)/(numberOfBorrowers+1))
                    db.collection("UserData").document(selectedUser)
                        .collection("users").document(selectedUser)
                        .update("user_bal", Balance)
                }
        }
        catch (e: Exception){
            System.err.print("Some Error Occurred")
        }
    }

    private fun selectLenderUser() {
        try {
            val lenderUserSpinner = findViewById<Spinner>(R.id.lenderUser)
            db.collection("GroupData").document(groupId)
                .get().addOnSuccessListener { it ->
                    users = (it.get("grp_users") as ArrayList<Any>)
                    for (uId in users){
                        userIds.add(uId)
                        db.collection("UserData").document(uId.toString())
                            .collection("users")
                            .document(uId.toString())
                            .get().addOnSuccessListener{
                                user_name = it.data?.get("user_name")
                                userNameArray.add(user_name.toString())
                                ArrayAdapter(this@AddTransaction, android.R.layout.simple_spinner_item, userNameArray)
                                    .also {adapter ->
                                        // Specify the layout to use when the list of choices appears.
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                        // Apply the adapter to the spinner.
                                        lenderUserSpinner.adapter = adapter
                                        lenderUserSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                                            override fun onItemSelected(
                                                p0: AdapterView<*>?,
                                                p1: View?,
                                                position: Int,
                                                p3: Long
                                            ) {
                                                //This variable contains the UID of the selected user
                                                selectedUser = users[position].toString()
                                            }

                                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                                findViewById<Button>(R.id.saveTrnBtn)?.isClickable = false
                                            }
                                        }
                                    }
                            }
                    }
                }
        }
        catch (e: Exception){
            System.err.print("Some Error Occurred")
        }
    }

    private fun addTransactionToGroup(id: String, amt: EditText) {
        try {
            db.collection("GroupData").document(groupId)
                .get().addOnSuccessListener { it ->
                    transactionIds = (it.get("grp_transactions") as ArrayList<Any>)
                    transactionIds.add(id)
                    db.collection("GroupData").document(groupId)
                        .update("grp_transactions", transactionIds)

                    //Update the balance to the group
                    var grpBalance = it.get("grp_total")
                    grpBalance = (grpBalance.toString().toFloat() + amt.text.toString().toFloat())
                    db.collection("GroupData").document(groupId)
                        .update("grp_total", grpBalance)
                }
        }
        catch (e: Exception){
            System.err.print("Some Error Occurred")
        }
    }
}