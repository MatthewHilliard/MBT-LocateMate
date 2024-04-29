package com.example.mbt_locatemate

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.identity.SignInClient
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var oneTapClient: SignInClient? = null
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        (activity as MainActivity).showBottomNavBar(false)

        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(requireContext())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        val loginButton = view.findViewById<SignInButton>(R.id.loginButton)

        loginButton.setOnClickListener() {
            CoroutineScope(Dispatchers.Main).launch {
                loginUser(view)
            }
        }

        return view
    }

    private suspend fun loginUser(view: View) {
        val result = oneTapClient?.beginSignIn(signInRequest)?.await()
        val intentSenderRequest = IntentSenderRequest.Builder(result!!.pendingIntent).build()
        activityResultLauncher.launch(intentSenderRequest)
    }

    private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient!!.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if(idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential).addOnCompleteListener{
                            if (it.isSuccessful) {
                                val user = auth.currentUser
                                if (user !== null) {
                                    val uid = user.uid
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val userExists = checkIfUserExists(uid)
                                        withContext(Dispatchers.Main) {
                                            if (!userExists) {
                                                val registerFragment = RegisterFragment()
                                                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, registerFragment).commit()
                                            } else {
                                                (activity as MainActivity).showBottomNavBar(true)
                                                (activity as MainActivity).bottomNavBar.selectedItemId = R.id.exploreTab
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: ApiException){
                    e.printStackTrace()
                }
            }
        }

    private suspend fun checkIfUserExists(uid: String): Boolean {
        return try {
            val querySnapshot = db.collection("users").whereEqualTo("id", uid).get().await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}