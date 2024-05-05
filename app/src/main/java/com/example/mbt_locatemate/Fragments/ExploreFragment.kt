package com.example.mbt_locatemate

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
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
import kotlin.properties.Delegates

class ExploreFragment: Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: PostListAdapter
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var notification: ImageView
    private var savedPosition: Int = RecyclerView.NO_POSITION
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private lateinit var segmentedButton: MaterialButtonToggleGroup
    private lateinit var friendPostsButton: Button
    private lateinit var explorePostsButton: Button
    private var isExplore = false

    private lateinit var friendsButton: ImageView
    private var friendRequestsCount = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //line between posts for separation
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

        //navigation to guess, comment, and leaderboard fragments
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

        //create shared preferences to save the recyclerview


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
                    //load the friend or public posts based on segmented button
                    R.id.friendsButton -> {
                            isExplore = false
                            loadFriendPosts()
                            postRecyclerView.smoothScrollToPosition(0)
                            friendPostsButton.setBackgroundColor(resources.getColor(R.color.md_theme_secondaryContainer))
                            explorePostsButton.setBackgroundColor(resources.getColor(R.color.md_theme_surface))
                    }
                    R.id.exploreButton -> {
                            isExplore = true
                            loadPublicPosts()
                            postRecyclerView.smoothScrollToPosition(0)
                            explorePostsButton.setBackgroundColor(resources.getColor(R.color.md_theme_secondaryContainer))
                            friendPostsButton.setBackgroundColor(resources.getColor(R.color.md_theme_surface))
                    }
                }
            }
        }
        //if a friend request is pending, show a red dot on friends icon
        notification = view.findViewById(R.id.notification)
        loadFriendRequestsCount()

        segmentedButton.check(R.id.friendsButton)

        sharedPreferences = requireActivity().getSharedPreferences("RecyclerViewPosition", MODE_PRIVATE)
        loadPosition()
        return view
    }

    private fun loadPosition() {
        postRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (isExplore) {
            savedPosition = sharedPreferences.getInt("explorePosition", 0)
            Log.d("Position", "loading position $savedPosition")
            postRecyclerView.smoothScrollToPosition(savedPosition)
        } else {
            savedPosition = sharedPreferences.getInt("friendsPosition", 0)
            Log.d("Position", "loading position $savedPosition")
            postRecyclerView.smoothScrollToPosition(savedPosition)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        savePosition()
    }

    private fun savePosition() {
        savedPosition = (postRecyclerView.layoutManager as LinearLayoutManager?)?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
        val editor = sharedPreferences.edit()
        if (isExplore) {
            editor.putInt("explorePosition", savedPosition)
            editor.apply()
        } else {
            editor.putInt("friendsPosition", savedPosition)
            editor.apply()
        }
    }

    //    override fun onPause() {
//        super.onPause()
//        // Save the current position of the RecyclerView when navigating away
//        savedPosition = (postRecyclerView.layoutManager as LinearLayoutManager?)?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
//    }

    //check for any incoming requests and set the visibility of the dot
    private fun loadFriendRequestsCount() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
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
    }

    private fun navigateToPostLeaderboardFragment(post: Post) {
        CoroutineScope(Dispatchers.IO).launch {
            val leaderboardFragment = PostLeaderboardFragment.newInstance(post)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, leaderboardFragment)
                //allows us to go back to this fragment because it is in the back stack
                .addToBackStack(null)
                .commit()
        }
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
                    // check if guess already exists
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
                                // thev've already made a guess
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
            //get all friend posts and add them to the adapter list through the friends document for current user
            CoroutineScope(Dispatchers.IO).launch {
                db.collection("friends").document(userId)
                    .collection("friend_usernames")
                    .get()
                    .addOnSuccessListener { friendsSnapshot ->
                        val friendUsernames = friendsSnapshot.documents.map { it.id }

                        if (friendUsernames.isNotEmpty()) {
                            db.collection("posts")
                                .whereIn("username", friendUsernames)
                                .get()
                                .addOnSuccessListener { postsSnapshot ->
                                    val postList = mutableListOf<Post>()
                                    for (document in postsSnapshot) {
                                        val postId =
                                            document.getString("id") ?: UUID.randomUUID().toString()
                                        val username = document.getString("username") ?: ""
                                        val caption = document.getString("caption") ?: ""
                                        val imgUrl = document.getString("img_url") ?: ""
                                        val pfpUrl = document.getString("pfp_url") ?: ""
                                        val latitude = document.getDouble("latitude") ?: 0.0
                                        val longitude = document.getDouble("longitude") ?: 0.0
                                        val timestamp = document.getLong("timestamp") ?: 0
                                        val post = Post(
                                            postId,
                                            username,
                                            caption,
                                            imgUrl,
                                            pfpUrl,
                                            latitude,
                                            longitude,
                                            timestamp
                                        )
                                        postList.add(post)
                                    }
                                    //sort posts descending by timestamp (newest posts first)
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
    }

    //same functionality as load posts but only get posts in which the public attribute is set to true
    private fun loadPublicPosts() {
        val userId = auth.currentUser?.uid
        val userFriends = mutableListOf<String>()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
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
                                val postId =
                                    document.getString("id") ?: UUID.randomUUID().toString()
                                val username = document.getString("username") ?: ""
                                val caption = document.getString("caption") ?: ""
                                val imgUrl = document.getString("img_url") ?: ""
                                val pfpUrl = document.getString("pfp_url") ?: ""
                                val latitude = document.getDouble("latitude") ?: 0.0
                                val longitude = document.getDouble("longitude") ?: 0.0
                                val timestamp = document.getLong("timestamp") ?: 0
                                val post = Post(
                                    postId,
                                    username,
                                    caption,
                                    imgUrl,
                                    pfpUrl,
                                    latitude,
                                    longitude,
                                    timestamp
                                )
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
    }
