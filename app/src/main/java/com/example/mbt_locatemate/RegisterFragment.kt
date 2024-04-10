package com.example.mbt_locatemate

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        auth = Firebase.auth

        val registerButton = view.findViewById<Button>(R.id.registerButton)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)

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
                            val userInfo = hashMapOf(
                                "id" to user.uid,
                                "username" to usernameInput.text.toString()
                            )
                            db.collection("users").document(usernameInput.text.toString())
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