package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.UUID

class FriendsFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: FriendListAdapter
    private lateinit var friendRecyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        friendRecyclerView = view.findViewById(R.id.friend_recycler_view)
        friendRecyclerView.addItemDecoration(
            DividerItemDecoration(
                friendRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        layoutManager = LinearLayoutManager(requireContext())
        friendRecyclerView.layoutManager = layoutManager

        auth = Firebase.auth

        adapter = FriendListAdapter(mutableListOf())
        friendRecyclerView.adapter = adapter

        val backButton = view.findViewById<ImageView>(R.id.friendBackButton)

        backButton.setOnClickListener(){
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exploreFragment).commit()
        }

        loadFriends("")
        return view
    }

    private fun loadFriends(search: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            if (search.isEmpty()) {
                db.collection("friends").document(userId)
                    .collection("friend_usernames")
                    .get()
                    .addOnSuccessListener { friendsSnapshot ->
                        val friendUsernames = friendsSnapshot.documents.map { it.id }

                        db.collection("users")
                            .whereIn("username", friendUsernames)
                            .get()
                            .addOnSuccessListener { documents ->
                                val friendList = mutableListOf<Friend>()
                                for (document in documents) {
                                    val username = document.getString("username") ?: ""
                                    val pfpUrl = document.getString("pfp_url") ?: ""
                                    val friend = Friend(username, pfpUrl)
                                    friendList.add(friend)
                                }
                                adapter.updateFriends(friendList)
                            }
                    }
            }
        }
    }
}