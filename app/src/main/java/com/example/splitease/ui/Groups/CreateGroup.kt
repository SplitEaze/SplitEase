package com.example.splitease.ui.Groups

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.splitease.MainActivity
import com.example.splitease.R
import com.example.splitease.ui.Utilities.Constants
import com.example.splitease.ui.Utilities.SharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CreateGroup : AppCompatActivity() {


    private lateinit var user: FirebaseUser
    private lateinit var auth: FirebaseAuth
    val db = FirebaseFirestore.getInstance()
    private var grpId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        val groupName = findViewById<EditText>(R.id.grpName)
        val groupCategory = findViewById<EditText>(R.id.etGrpCategory)
        findViewById<Button>(R.id.doneBtn).setOnClickListener {
            val groupData = HashMap<String, Any>()
            auth = Firebase.auth
            user = auth.currentUser!!

            val bundle = intent.extras
            grpId = bundle?.getString("grpId").toString()

            groupData["grp_id"] = grpId
            groupData["grp_name"] = groupName.text.toString()
            groupData["grp_cat"] = groupCategory.text.toString()

            db.collection("UserData").document(user.uid)
                .collection("groups").document(grpId).set(groupData)

            val intent = Intent(this@CreateGroup, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}