package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mbt_locatemate.Fragments.FriendsLeaderboardFragment
import com.example.mbt_locatemate.databinding.FragmentProfileBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: ProfilePostListAdapter
    private lateinit var profilePostRecyclerView: RecyclerView
    private lateinit var numPostsText: TextView
    private lateinit var numGuessesText: TextView
    private lateinit var leaderboardButton: ImageView

    private var username: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = Firebase.auth
        val user = auth.currentUser

        profilePostRecyclerView = view.findViewById(R.id.userPostsRecyclerView)
        numPostsText = view.findViewById(R.id.txtPostCount)
        numGuessesText = view.findViewById(R.id.txtGuessCount)

        layoutManager = GridLayoutManager(requireContext(), 2)
        profilePostRecyclerView.layoutManager = layoutManager

        //send post info to individual post fragment
        adapter = ProfilePostListAdapter(emptyList()){ post ->
            val bundle = Bundle().apply {
                putParcelable("post", post)
            }
            val individualPostFragment = IndividualPostFragment().apply {
                arguments = bundle
            }
            Log.d("OnClick", "Beginning fragment replacement")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, individualPostFragment)
                .addToBackStack(null)
                .commit()
        }
        profilePostRecyclerView.adapter = adapter

        val usernameText = view.findViewById<TextView>(R.id.txtUsername)
        val pfpImage = view.findViewById<ImageView>(R.id.imgProfile)
        if (user != null) {
            //get user info and populate username and user posts
            CoroutineScope(Dispatchers.IO).launch {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                username = document.getString("username")
                val pfpUrl = document.getString("pfp_url")
                usernameText.text = username
                Picasso.get().load(pfpUrl).into(pfpImage)
                loadUserPosts()
                }
            }

            db.collection("users").document(user.uid).collection("guesses").get().addOnSuccessListener { documents ->
                val numberOfGuesses = documents.size()
                Log.d("Firestore", "number of guesses: $numberOfGuesses")
                numGuessesText.text = numberOfGuesses.toString()
            }
        }

        val settingsButton = view.findViewById<ImageView>(R.id.settingsButton)

        settingsButton.setOnClickListener(){
            val settingsFragment = SettingsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment).commit()
        }

        leaderboardButton = view.findViewById(R.id.leaderboardButton)
        leaderboardButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val friendsLeaderboardFragment = FriendsLeaderboardFragment().apply {
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, friendsLeaderboardFragment)
                    .commit()
                        }
        }

        return view
    }

    //populate the cardview with user posts
    private fun loadUserPosts() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("posts")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { posts ->
                    val postList = mutableListOf<Post>()
                    for (document in posts) {
                        Log.d("ProfileFragment", "Post found")
                        val postId = document.getString("id") ?: UUID.randomUUID().toString()
                        val username = document.getString("username") ?: ""
                        val caption = document.getString("caption") ?: ""
                        val imgUrl = document.getString("img_url") ?: ""
                        val pfpUrl = document.getString("pfp_url") ?: ""
                        val latitude = document.getDouble("latitude") ?: 0.0
                        val longitude = document.getDouble("longitude") ?: 0.0
                        val timestamp = document.getLong("timestamp") ?: 0
                        val post = Post(postId, username, caption, imgUrl, pfpUrl, latitude, longitude, timestamp)
                        postList.add(post)
                    }
                    //sort newest on top
                    postList.sortByDescending { it.timestamp }
                    adapter.updatePosts(postList)
                    numPostsText.text = postList.size.toString()
                }
        }
    }
}