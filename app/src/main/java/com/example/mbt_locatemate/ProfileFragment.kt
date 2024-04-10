package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ProfileFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = Firebase.auth
        val user = auth.currentUser

        val usernameText = view.findViewById<TextView>(R.id.txtUsername)
        if (user != null) {
            //NOTE - current retrival of things is quite slow since it has to go through the cloud to retrieve a single thing
            //There are ways to locally store values once we retrieve them for the first time that we should look at later
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                val username = document.getString("username")
                usernameText.text = username
            }
        }

        val settingsButton = view.findViewById<Button>(R.id.settingsButton)

        settingsButton.setOnClickListener(){
            val settingsFragment = SettingsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment).commit()
        }

        return view
    }
}