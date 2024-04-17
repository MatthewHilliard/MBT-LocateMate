package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.UUID

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

        adapter = PostListAdapter(mutableListOf())
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
                        loadAllPosts()
                        postRecyclerView.smoothScrollToPosition(0)
                    }
                }
            }
        }

        segmentedButton.check(R.id.friendsButton)
        return view
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
                                    val username = document.getString("username") ?: ""
                                    val caption = document.getString("caption") ?: ""
                                    val imgUrl = document.getString("img_url") ?: ""
                                    val pfpUrl = document.getString("pfp_url") ?: ""
                                    val post = Post(UUID.randomUUID(), username, caption, imgUrl, pfpUrl)
                                    postList.add(post)
                                }
                                adapter.updatePosts(postList)
                            }
                    } else {
                        val postList = mutableListOf<Post>()
                        adapter.updatePosts(postList)
                    }
                }
        }
    }

    private fun loadAllPosts() {
        //TODO this is super inefficent tbh and uses a ton of API calls so perhaps rework the database a bit to make fewer calls
        //yeah actually the own users post sometimes gets rendered so its def a bit bugged because of excessive api calls
        //i think we should figure out how to make global variables upon app start of basic user info like username and pfp
        val userId = auth.currentUser?.uid
        var userUsername: String? = null
        val userFriends = mutableListOf<String>()
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                userUsername = document.getString("username")
            }
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
                        val username = document.getString("username") ?: ""
                        val caption = document.getString("caption") ?: ""
                        val imgUrl = document.getString("img_url") ?: ""
                        val pfpUrl = document.getString("pfp_url") ?: ""
                        val post = Post(UUID.randomUUID(), username, caption, imgUrl, pfpUrl)
                        if (username != userUsername && username !in userFriends) {
                            postList.add(post)
                        }
                    }
                    adapter.updatePosts(postList)
                }
        }
    }
}