package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import android.net.Uri
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val db = Firebase.firestore

    private var uri: Uri? = null

    private var pfpUpdated = false
    private var usernameUpdated = false
    private var pfpUrl: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        auth = Firebase.auth
        val user = auth.currentUser

        val profilePicture = view.findViewById<ImageView>(R.id.imgSettings)
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                pfpUrl = document.getString("pfp_url")
                Picasso.get().load(pfpUrl).into(profilePicture)
            }
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            if(pfpUrl?.equals(it) != true){
                profilePicture.setImageURI(it)
                if(it != null){
                    uri = it
                    pfpUpdated = true
                }
            }
        }

        profilePicture.setOnClickListener{
            pickImage.launch("image/*")
        }

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener(){
            Firebase.auth.signOut()
            val loginFragment = LoginFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loginFragment).commit()
        }

        val backButton = view.findViewById<ImageView>(R.id.settingsBackButton)
        backButton.setOnClickListener{
            val profileFragment = ProfileFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment).commit()
        }

        val saveButton = view.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            if(pfpUpdated){

            }
            if(usernameUpdated){

            }
            pfpUpdated = false
            usernameUpdated = false
        }

        return view
    }
}