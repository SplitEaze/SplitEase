package com.example.splitease.ui.Groups

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitease.Models.UserDataModel
import com.example.splitease.R
import com.example.splitease.ui.DetailedGroup.DetailedGroup
import com.example.splitease.ui.Groups.Adapter.GroupsAdapter
import com.example.splitease.ui.Utilities.Constants
import com.example.splitease.ui.Utilities.SharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class GroupsFragment : Fragment(), GroupsAdapter.ItemClickListener {

    private var documentDataList: MutableList<UserDataModel> = ArrayList()
    private lateinit var userItemModel: MutableList<UserDataModel>
    val db = FirebaseFirestore.getInstance()
    var userAdaper: GroupsAdapter ?= null
    private lateinit var user: FirebaseUser
    private lateinit var auth: FirebaseAuth
    private var groupId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<RecyclerView>(R.id.rvGroups)?.layoutManager = LinearLayoutManager(context)
        getGroups()
        view?.findViewById<Button>(R.id.createGroupBtn)?.setOnClickListener {
            auth = Firebase.auth
            user = auth.currentUser!!
            val newGroupRef = db.collection("UserData").document(user.uid)
                .collection("groups").document()
            val intent = Intent(activity, CreateGroup::class.java)
            groupId = newGroupRef.id
            intent.putExtra("grpId", groupId)
            startActivity(intent)
        }
    }

    private fun getGroups() {
        try {
            db.collection("UserData").document(
                SharedPref(activity!!)
                .getString(Constants.UUID).toString()).collection("groups")
                .get().addOnSuccessListener {
                    userItemModel = it.toObjects(UserDataModel::class.java)
                    documentDataList.addAll(userItemModel)
                    setAdapter()
                }
        }
        catch (e: Exception){
            System.err.print("Some Error Occurred")
        }
    }

    private fun setAdapter() {
            userAdaper = GroupsAdapter(userItemModel, this)
            view?.findViewById<RecyclerView>(R.id.rvGroups)?.adapter = userAdaper
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(activity, DetailedGroup::class.java)
        intent.putExtra("groupId", documentDataList[position].grp_id)
        startActivity(intent)
    }
}