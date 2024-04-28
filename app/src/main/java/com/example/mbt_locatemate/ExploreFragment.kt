package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.UUID
import kotlin.concurrent.timerTask

class ExploreFragment: Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: PostListAdapter
    private lateinit var postRecyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private lateinit var segmentedButton: MaterialButtonToggleGroup
    private lateinit var friendsButton: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        postRecyclerView = view.findViewById(R.id.post_recycler_view)
        postRecyclerView.addItemDecoration(
            DividerItemDecoration(
                postRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        layoutManager = LinearLayoutManager(requireContext())
        postRecyclerView.layoutManager = layoutManager

        auth = Firebase.auth

        adapter = PostListAdapter(mutableListOf()).apply {
            onGuessClickListener = { post ->

                navigateToMapGuessFragment(post)
            }

            onCommentsClickListener = { post ->
                openCommentsSheet(post)
            }

            onLeaderboardClickListener = { post ->
                navigateToPostLeaderboardFragment(post)
            }
        }
        postRecyclerView.adapter = adapter

        friendsButton = view.findViewById(R.id.friendsIcon)
        friendsButton.setOnClickListener(){
            val friendsFragment = FriendsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, friendsFragment).commit()
        }

        segmentedButton = view.findViewById(R.id.segmentedButton)
        segmentedButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.friendsButton -> {
                        loadFriendPosts()
                        postRecyclerView.smoothScrollToPosition(0)
                    }
                    R.id.exploreButton -> {
                        loadPublicPosts()
                        postRecyclerView.smoothScrollToPosition(0)
                    }
                }
            }
        }

        segmentedButton.check(R.id.friendsButton)
        return view
    }

    private fun navigateToPostLeaderboardFragment(post: Post) {
        val leaderboardFragment = PostLeaderboardFragment.newInstance(post.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, leaderboardFragment)
            .commit()
    }

    private fun openCommentsSheet(post: Post) {
        val bottomSheetFragment = CommentsFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun navigateToMapGuessFragment(post: Post) {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId != null) {
            //fetch username using userid
            val userRef = Firebase.firestore.collection("users").document(currentUserId)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                val username = documentSnapshot.getString("username")
                if (username != null) {
                    // check if guess alrd exists
                    val postRef = Firebase.firestore.collection("posts").document(post.id.toString())
                    postRef.collection("guesses").whereEqualTo("user", username).get()
                        .addOnSuccessListener { queryDocumentSnapshots ->
                            if (queryDocumentSnapshots.isEmpty) {
                                // no guess, navigate to map
                                val guessFragment = MapGuessFragment.newInstance(post)
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, guessFragment)
                                    .addToBackStack(null)
                                    .commit()
                            } else {
                                // theve alrd made guess
                                Toast.makeText(requireContext(), "You have already made a guess on this post!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            // error checking guess
                            Log.e("TAG", "Error fetching user details", e)
                        }
                }
            }.addOnFailureListener { exception ->
                Log.e("TAG", "Error fetching user details", exception)
            }
        }
    }

    private fun loadFriendPosts() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("friends").document(userId)
                .collection("friend_usernames")
                .get()
                .addOnSuccessListener { friendsSnapshot ->
                    val friendUsernames = friendsSnapshot.documents.map { it.id }

                    if(friendUsernames.isNotEmpty()) {
                        db.collection("posts")
                            .whereIn("username", friendUsernames)
                            .get()
                            .addOnSuccessListener { postsSnapshot ->
                                val postList = mutableListOf<Post>()
                                for (document in postsSnapshot) {
                                    val postId = document.getString("id") ?: UUID.randomUUID().toString()
                                    val username = document.getString("username") ?: ""
                                    val caption = document.getString("caption") ?: ""
                                    val imgUrl = document.getString("img_url") ?: ""
                                    val pfpUrl = document.getString("pfp_url") ?: ""
                                    val latitude = document.getDouble("latitude") ?: 0.0
                                    val longitude = document.getDouble("longitude") ?: 0.0
                                    val location = LatLng(latitude, longitude)
                                    val timestamp = document.getLong("timestamp") ?: 0
                                    val post = Post(postId, username, caption, imgUrl, pfpUrl, location, timestamp)
                                    postList.add(post)
                                }
                                postList.sortByDescending { it.timestamp }
                                adapter.updatePosts(postList)
                            }
                    } else {
                        val postList = mutableListOf<Post>()
                        adapter.updatePosts(postList)
                    }
                }
        }
    }

    private fun loadPublicPosts() {
        val userId = auth.currentUser?.uid
        val userFriends = mutableListOf<String>()
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { currUser ->
                val userUsername = currUser.getString("username")
                db.collection("friends").document(userId)
                    .collection("friend_usernames")
                    .get()
                    .addOnSuccessListener { friendsSnapshot ->
                        userFriends.addAll(friendsSnapshot.documents.map { it.id })
                    }
                db.collection("posts")
                    .get()
                    .addOnSuccessListener { documents ->
                        val postList = mutableListOf<Post>()
                        for (document in documents) {
                            val postId = document.getString("id") ?: UUID.randomUUID().toString()
                            val username = document.getString("username") ?: ""
                            val caption = document.getString("caption") ?: ""
                            val imgUrl = document.getString("img_url") ?: ""
                            val pfpUrl = document.getString("pfp_url") ?: ""
                            val latitude = document.getDouble("latitude") ?: 0.0
                            val longitude = document.getDouble("longitude") ?: 0.0
                            val location = LatLng(latitude, longitude)
                            val timestamp = document.getLong("timestamp") ?: 0
                            val post = Post(postId, username, caption, imgUrl, pfpUrl, location, timestamp)
                            if (username != userUsername && username !in userFriends) {
                                postList.add(post)
                            }
                        }
                        postList.sortByDescending { it.timestamp }
                        adapter.updatePosts(postList)

                            }
                    }
            }
        }
    }
