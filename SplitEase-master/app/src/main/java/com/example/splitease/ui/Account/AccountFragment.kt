package com.example.splitease.ui.Account

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.splitease.R
import com.example.splitease.ui.Startup.Signup
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<Button>(R.id.logout)?.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(requireActivity(), Signup::class.java))
            getActivity()?.getFragmentManager()?.popBackStack()
        }
    }
}