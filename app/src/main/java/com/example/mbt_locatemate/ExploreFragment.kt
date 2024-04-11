package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        postRecyclerView = view.findViewById(R.id.post_recycler_view)
        layoutManager = LinearLayoutManager(requireContext())
        postRecyclerView.layoutManager = layoutManager

        auth = Firebase.auth

        adapter = PostListAdapter(mutableListOf())
        postRecyclerView.adapter = adapter

        segmentedButton = view.findViewById(R.id.segmentedButton)

        segmentedButton.addOnButtonCheckedListener { segmentedButton, checkedId, isChecked ->
            when (checkedId) {
                R.id.friendsButton -> {

                }
                R.id.exploreButton -> {

                }
            }
        }

        loadPosts()

        return view
    }
    private fun generateDummyPosts(): List<Post> {
        val dummyList = mutableListOf<Post>()
        for (i in 1..10) {
            val username = "@user$i"
            val caption = "This is post number $i"
            dummyList.add(Post(UUID.randomUUID(), username, caption))
        }
        return dummyList
    }

    private fun loadPosts() {
        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val postList = mutableListOf<Post>()
                for (document in documents) {
                    Log.d("ExploreFragment", "We are creating this post")
                    val username = document.getString("username") ?: ""
                    val caption = document.getString("caption") ?: ""
                    val post = Post(UUID.randomUUID(), username, caption)
                    postList.add(post)
                }
                adapter.updatePosts(postList)
            }
            .addOnFailureListener { exception ->
                Log.e("ExploreFragment", "Error fetching posts", exception)
            }
    }
}