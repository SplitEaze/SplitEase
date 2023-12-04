package com.example.splitease.ui.Login

import android.content.Intent
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.splitease.MainActivity
import com.example.splitease.R
import com.example.splitease.ui.Utilities.Constants
import com.example.splitease.ui.Utilities.SharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var user: FirebaseUser
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        auth = Firebase.auth
        val email = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val password = findViewById<EditText>(R.id.editTextTextPassword)

        email.addTextChangedListener {

            findViewById<Button>(R.id.button).isEnabled = !(it.isNullOrEmpty())
        }

        findViewById<Button>(R.id.button).setOnClickListener {

            if(email.text.isNullOrEmpty() || password.text.isNullOrEmpty()){
                if(email.text.isNullOrEmpty())
                    email.error = "Enter the email"
                else
                    password.error = "Enter the password"
            }

            else{
                auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            user = task.result.user!!
                            SharedPref(this@Login).setString(Constants.UUID, user.uid)
                            // Sign in success, update UI with the signed-in user's information
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(baseContext, "Please enter correct details",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}