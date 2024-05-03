package com.example.mbt_locatemate

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ExploreFragment: Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: PostListAdapter
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var notification: ImageView

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private lateinit var segmentedButton: MaterialButtonToggleGroup
    private lateinit var friendPostsButton: Button
    private lateinit var explorePostsButton: Button

    private lateinit var friendsButton: ImageView
    private var friendRequestsCount = 0
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
                CoroutineScope(Dispatchers.IO).launch {
                    navigateToPostLeaderboardFragment(post)
                }
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
        friendPostsButton = view.findViewById(R.id.friendsButton)
        explorePostsButton = view.findViewById(R.id.exploreButton)
        segmentedButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.friendsButton -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            loadFriendPosts()
                            postRecyclerView.smoothScrollToPosition(0)
                            friendPostsButton.setBackgroundColor(resources.getColor(R.color.md_theme_secondaryContainer))
                            explorePostsButton.setBackgroundColor(resources.getColor(R.color.md_theme_surface))
                        }
                    }
                    R.id.exploreButton -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            loadPublicPosts()
                            postRecyclerView.smoothScrollToPosition(0)
                            explorePostsButton.setBackgroundColor(resources.getColor(R.color.md_theme_secondaryContainer))
                            friendPostsButton.setBackgroundColor(resources.getColor(R.color.md_theme_surface))
                        }
                    }
                }
            }
        }

        notification = view.findViewById(R.id.notification)
        CoroutineScope(Dispatchers.IO).launch {
            loadFriendRequestsCount()
        }

        segmentedButton.check(R.id.friendsButton)
        return view
    }
    private fun loadFriendRequestsCount() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("friends").document(userId)
                .collection("incoming_requests")
                .get()
                .addOnSuccessListener { snapshot ->
                    friendRequestsCount = snapshot.size()
                    if (friendRequestsCount > 0) {
                        //have pending friend requests
                        notification.visibility = View.VISIBLE
                    } else {
                        //do not have pending friend requests
                        notification.visibility = View.INVISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                }
        }
    }

    private fun navigateToPostLeaderboardFragment(post: Post) {
        val leaderboardFragment = PostLeaderboardFragment.newInstance(post)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, leaderboardFragment)
            .commit()
    }

    private fun openCommentsSheet(post: Post) {
        val bundle = Bundle().apply {
            putParcelable("post", post)
        }
        val commentsFragment = CommentsFragment().apply {
            arguments = bundle
        }
        commentsFragment.show(parentFragmentManager, "CommentsFragment")
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
                    .whereEqualTo("public", true)
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
