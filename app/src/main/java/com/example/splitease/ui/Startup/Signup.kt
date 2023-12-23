package com.example.splitease.ui.Startup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        WindowCompat.setDecorFitsSystemWindows(window, false)      //Make UI Full Screen
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView)

        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())      // Hide the system bars.

        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())      // Show the system bars.
        windowInsetsController?.isAppearanceLightNavigationBars = true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //remove night mode
        supportActionBar?.hide()

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val name = findViewById<EditText>(R.id.etName)

        findViewById<Button>(R.id.loginBtn).setOnClickListener {
            val intent = Intent(this@Signup, Login::class.java)
            startActivity(intent)
            finish()
        }

        auth = Firebase.auth

        email.addTextChangedListener {
            password.addTextChangedListener {
                name.addTextChangedListener {
                    findViewById<Button>(R.id.button).isEnabled = !(it.isNullOrEmpty())
                }
            }
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            if(email.text.isNullOrEmpty() || password.text.isNullOrEmpty()){
                if(email.text.isNullOrEmpty())
                    email.error = "Enter the email"
                else if (password.text.isNullOrEmpty())
                    password.error = "Enter the password"
                else
                    name.error = "Enter your full name"
            }

            else {
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = Firebase.auth.currentUser

                            val profileUpdates = userProfileChangeRequest {
                                displayName = name.text.toString()
                            }

                            user!!.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@Signup, "Name Updated", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            val userData = hashMapOf(
                                "user_id" to user.uid,
                                "user_name" to name.text.toString(),
                                "user_bal" to 0,
                                "user_trn" to ArrayList<String>()
                            )

                            db.collection("UserData").document(user.uid)
                                .collection("users").document(user.uid).set(userData)

                            //Create groups collection
                            db.collection("UserData").document(user.uid)
                                .collection("groups")

                            SharedPref(this@Signup).setString(Constants.UUID, user.uid)
                            SharedPref(this@Signup).setString(Constants.NAME, name.text.toString())

                            startActivity(Intent(this@Signup, MainActivity::class.java))
                            finish()

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }

        }
    }
}