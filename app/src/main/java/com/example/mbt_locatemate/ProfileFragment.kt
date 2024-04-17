package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.mbt_locatemate.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso

class ProfileFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: ProfilePostListAdapter
    private lateinit var profilePostRecyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = Firebase.auth
        val user = auth.currentUser

        profilePostRecyclerView = view.findViewById(R.id.userPostsRecyclerView)

        val usernameText = view.findViewById<TextView>(R.id.txtUsername)
        val pfpImage = view.findViewById<ImageView>(R.id.imgProfile)
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                val username = document.getString("username")
                val pfpUrl = document.getString("pfp_url")
                usernameText.text = username
                Picasso.get().load(pfpUrl).into(pfpImage)
            }
        }

        val settingsButton = view.findViewById<ImageView>(R.id.settingsButton)

        settingsButton.setOnClickListener(){
            val settingsFragment = SettingsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment).commit()
        }

        return view
    }
}