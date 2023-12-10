package com.example.splitease.ui.AddUpdateTransaction

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.splitease.R

class EditTransaction : AppCompatActivity() {

    private var trnId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        val bundle = intent.extras
        trnId = bundle?.getString("trnId").toString()
    }
}