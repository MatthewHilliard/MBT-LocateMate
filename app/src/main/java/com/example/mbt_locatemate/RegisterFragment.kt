package com.example.mbt_locatemate

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val db = Firebase.firestore

    private var uri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        auth = Firebase.auth
        storage = Firebase.storage

        val registerButton = view.findViewById<Button>(R.id.registerButton)
        val pickImageButton = view.findViewById<Button>(R.id.pickImageButton)
        val profilePicture = view.findViewById<ImageView>(R.id.profilePicture)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            profilePicture.setImageURI(it)
            if(it != null){
                uri = it
            }
        }

        pickImageButton.setOnClickListener(){
            pickImage.launch("image/*")
        }

        registerButton.setOnClickListener(){
            CoroutineScope(Dispatchers.IO).launch {
                val usernameExists = checkIfUsernameExists(usernameInput.text.toString())
                withContext(Dispatchers.Main) {
                    if (usernameExists) {
                        Toast.makeText(
                            requireContext(),
                            "Username already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val user = auth.currentUser
                        if (user !== null) {
                            //TODO: Make it so that pfp crops into circle
                            uri?.let {
                                storage.reference.child("images/${user.uid}.jpg").putFile(it)
                                    .addOnSuccessListener { task->
                                        task.metadata!!.reference!!.downloadUrl
                                            .addOnSuccessListener { url ->
                                                val pfpUrl = url.toString()
                                                val userInfo = hashMapOf(
                                                    "id" to user.uid,
                                                    "username" to usernameInput.text.toString(),
                                                    "email" to user.email,
                                                    "pfp_url" to pfpUrl
                                                )

                                                db.collection("users").document(user.uid)
                                                    .set(userInfo)
                                                    .addOnSuccessListener {
                                                        Log.d(ContentValues.TAG, "New user document added")
                                                        (activity as MainActivity).showBottomNavBar(true)
                                                        (activity as MainActivity).bottomNavBar.selectedItemId =
                                                            R.id.exploreTab
                                                    }.addOnFailureListener { e ->
                                                        Log.e(ContentValues.TAG, "Error adding new user document", e)
                                                    }
                                            }
                                    }
                            }
                        }
                    }
                }
            }
        }

        return view
    }

    private suspend fun checkIfUsernameExists(username: String): Boolean {
        return try {
            val querySnapshot = db.collection("users").whereEqualTo("username", username).get().await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}