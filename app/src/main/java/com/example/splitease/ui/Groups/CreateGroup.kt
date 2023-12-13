package com.example.splitease.ui.Groups

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.splitease.MainActivity
import com.example.splitease.R
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
        WindowCompat.setDecorFitsSystemWindows(window, false)      //Make UI Full Screen
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView)

        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())      // Hide the system bars.

        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())      // Show the system bars.
        windowInsetsController?.isAppearanceLightNavigationBars = true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //remove night mode
        supportActionBar?.hide()

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

            //Store group details in particular user db
            db.collection("UserData").document(user.uid)
                .collection("groups").document(grpId).set(groupData)

            val detailedGroupData = HashMap<String, Any>()

            detailedGroupData["grp_id"] = grpId
            detailedGroupData["grp_name"] = groupName.text.toString()
            detailedGroupData["grp_cat"] = groupCategory.text.toString()
            detailedGroupData["grp_total"] = 00.00
            detailedGroupData["grp_transactions"] = arrayListOf<String>()
            detailedGroupData["grp_users"] = arrayListOf<String>(user.uid)

            //Store all details in the group db
            db.collection("GroupData").document(grpId)
                .set(detailedGroupData)

            val intent = Intent(this@CreateGroup, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}