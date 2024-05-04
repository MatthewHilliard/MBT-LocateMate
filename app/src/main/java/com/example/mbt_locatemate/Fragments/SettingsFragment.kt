package com.example.mbt_locatemate

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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
import android.util.Log
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val db = Firebase.firestore

    private var uri: Uri? = null

    private lateinit var prevUsername: String
    private var pfpUpdated = false
    private var pfpUrl: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        auth = Firebase.auth
        storage = Firebase.storage
        val user = auth.currentUser

        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                prevUsername = document.getString("username") ?: ""
            }
        }

        val profilePicture = view.findViewById<ImageView>(R.id.imgSettings)
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                pfpUrl = document.getString("pfp_url")
                Picasso.get().load(pfpUrl).into(profilePicture)
            }
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (pfpUrl?.equals(it) != true && it != null) {
                val orientation = getExifOrientation(it)
                val rotatedBitmap = rotateBitmap(it, orientation)
                profilePicture.setImageBitmap(rotatedBitmap)
                uri = it
                pfpUpdated = true
            }
        }

        profilePicture.setOnClickListener {
            pickImage.launch("image/*")
        }

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener() {
            Firebase.auth.signOut()
            val loginFragment = LoginFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loginFragment).commit()
        }

        val usernameInput = view.findViewById<TextInputEditText>(R.id.usernameEditText)

        val backButton = view.findViewById<ImageView>(R.id.settingsBackButton)
        backButton.setOnClickListener {
            val profileFragment = ProfileFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment).commit()
        }
        //save updates to username or pfp
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
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
                        if (pfpUpdated) {
                            if (user !== null) {
                                uri?.let {
                                    storage.reference.child("images/${user.uid}.jpg").putFile(it)
                                        .addOnSuccessListener { task ->
                                            task.metadata!!.reference!!.downloadUrl
                                                .addOnSuccessListener { url ->
                                                    val pfpUrl = url.toString()
                                                    db.collection("users")
                                                        .document(user.uid)
                                                        .update("pfp_url", pfpUrl)
                                                }
                                        }
                                }
                            }
                        }
                        if (usernameInput.text?.isNotEmpty() == true) {
                            if (user !== null) {
                                updatePostUsernames(prevUsername, usernameInput.text.toString())
                                updateFriendUsernames(prevUsername, usernameInput.text.toString())

                                db.collection("users")
                                    .document(user.uid)
                                    .update("username", usernameInput.text.toString())

                                prevUsername = usernameInput.text.toString()
                            }
                        }
                        pfpUpdated = false
                        Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
            return view
    }

    //pfp orientation
    private fun getExifOrientation(uri: Uri): Int {
        val inputStream = context?.contentResolver?.openInputStream(uri)
        val exif = inputStream?.use { ExifInterface(it) }
        return exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        ) ?: ExifInterface.ORIENTATION_UNDEFINED
    }

    private fun rotateBitmap(uri: Uri, orientation: Int): Bitmap {
        val inputStream = context?.contentResolver?.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun updatePostUsernames(prevUsername: String, newUsername: String){
        val user = auth.currentUser
        if(user != null){
                db.collection("posts")
                    .whereEqualTo("username", prevUsername)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val batch = db.batch()
                        querySnapshot.documents.forEach { postDoc ->
                            val postRef = db.collection("posts").document(postDoc.id)
                            batch.update(postRef, "username", newUsername)
                            Log.d("SettingsFragment", "We are here")
                        }

                        batch.commit()
            }
        }
    }

    //Utilized chat GPT for query assistance
    //update your username for every friend (in their documents)
    private fun updateFriendUsernames(prevUsername: String, newUsername: String){
        val user = auth.currentUser
        if (user != null) {
            db.collection("friends")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { userDoc ->
                        val userId = userDoc.id
                        val collections = listOf("incoming_requests", "outgoing_requests", "friend_usernames")

                        collections.forEach { collection ->
                            val collectionRef = db.collection("friends").document(userId).collection(collection)

                            collectionRef
                                .whereEqualTo("username", prevUsername)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    querySnapshot.documents.forEach { doc ->
                                        val docId = doc.id
                                        val docData = doc.data

                                        if (docData != null) {
                                            val newDocRef = collectionRef.document(newUsername)

                                            newDocRef.set(docData)
                                            collectionRef.document(docId).delete()
                                            newDocRef.update("username", newUsername)
                                        }
                                    }
                                }
                        }
                    }
                }
        }
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