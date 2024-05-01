package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class FriendsFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: FriendListAdapter
    private lateinit var friendRecyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var searchText: String
    private var onFriends = true
    private var onRequest = false
    private var onAdd = false

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

        tabLayout = view.findViewById(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> {
                            loadFriends()
                            onFriends = true
                            onRequest = false
                            onAdd = false
                            searchView.setQuery("", false)
                            searchView.clearFocus()
                            searchText = ""
                        }
                        1 -> {
                            loadFriendRequests()
                            onFriends = false
                            onRequest = true
                            onAdd = false
                            searchView.setQuery("", false)
                            searchView.clearFocus()
                            searchText = ""
                        }
                        2 -> {
                            loadAddFriends()
                            onFriends = false
                            onRequest = false
                            onAdd = true
                            searchView.setQuery("", false)
                            searchView.clearFocus()
                            searchText = ""
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        val backButton = view.findViewById<ImageView>(R.id.friendBackButton)
        backButton.setOnClickListener{
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exploreFragment).commit()
        }

        searchView = view.findViewById(R.id.friendSearch)
        searchView.clearFocus()
        searchText = ""
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if(onFriends){
                    if (query != null) {
                        searchText = query
                    }
                    loadFriends()
                } else if(onRequest){
                    if (query != null) {
                        searchText = query
                    }
                    loadFriendRequests()
                } else {
                    if (query != null) {
                        searchText = query
                    }
                    loadAddFriends()
                }
                return true
            }
        })

        loadFriends()
        return view
    }

    //Used Chat GPT to assist in the query
    private fun loadFriends() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            if (searchText.isEmpty()) {
                db.collection("friends").document(userId)
                    .collection("friend_usernames")
                    .get()
                    .addOnSuccessListener { friendsSnapshot ->
                        val friendUsernames = friendsSnapshot.documents.map { it.id }

                        if(friendUsernames.isNotEmpty()){
                            db.collection("users")
                                .whereIn("username", friendUsernames)
                                .get()
                                .addOnSuccessListener { documents ->
                                    val friendList = mutableListOf<Friend>()
                                    for (document in documents) {
                                        val id = document.getString("id") ?: ""
                                        val username = document.getString("username") ?: ""
                                        val pfpUrl = document.getString("pfp_url") ?: ""
                                        val friend = Friend(id, username, pfpUrl)
                                        friendList.add(friend)
                                    }
                                    adapter.updateFriends(friendList)
                                }
                        } else {
                            val friendList = mutableListOf<Friend>()
                            adapter.updateFriends(friendList)
                        }
                    }
            } else {
                db.collection("friends").document(userId)
                    .collection("friend_usernames")
                    .get()
                    .addOnSuccessListener { friendsSnapshot ->
                        val friendUsernames = friendsSnapshot.documents.map { it.id }

                        if (friendUsernames.isNotEmpty()) {
                            db.collection("users")
                                .whereIn("username", friendUsernames)
                                .whereGreaterThanOrEqualTo("username", searchText)
                                .whereLessThan("username", searchText + "\uf8ff")
                                .get()
                                .addOnSuccessListener { documents ->
                                    val friendList = mutableListOf<Friend>()
                                    for (document in documents) {
                                        val id = document.getString("id") ?: ""
                                        val username = document.getString("username") ?: ""
                                        val pfpUrl = document.getString("pfp_url") ?: ""
                                        val friend = Friend(id, username, pfpUrl)
                                        friendList.add(friend)
                                    }
                                    adapter.updateFriends(friendList)
                                }
                        } else {
                            val friendList = mutableListOf<Friend>()
                            adapter.updateFriends(friendList)
                        }
                    }
            }
        }
    }

    //Used ChatGPT to assist in the query
    private fun loadAddFriends() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            if (searchText.isEmpty()) {
                db.collection("friends").document(userId)
                    .collection("friend_usernames")
                    .get()
                    .addOnSuccessListener { friendsSnapshot ->
                        val friendUsernames =
                            friendsSnapshot.documents.map { it.id }.toMutableList()
                        if (friendUsernames.isEmpty()) {
                            friendUsernames.add("")
                        }
                        db.collection("friends").document(userId)
                            .collection("incoming_requests")
                            .get()
                            .addOnSuccessListener { requestsSnapshot ->
                                val requestUsernames =
                                    requestsSnapshot.documents.map { it.id }.toMutableList()
                                if (requestUsernames.isEmpty()) {
                                    requestUsernames.add("")
                                }
                                val combinedUsernames = friendUsernames + requestUsernames
                                db.collection("friends").document(userId)
                                    .collection("outgoing_requests")
                                    .get()
                                    .addOnSuccessListener { outgoingSnapshot ->
                                        val outgoingUsernames =
                                            outgoingSnapshot.documents.map { it.id }.toMutableList()
                                        if (outgoingUsernames.isEmpty()) {
                                            outgoingUsernames.add("")
                                        }
                                        val usedUsernames = combinedUsernames + outgoingUsernames
                                        db.collection("users")
                                            .whereNotIn("username", usedUsernames)
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                val friendList = mutableListOf<Friend>()
                                                for (document in documents) {
                                                    val id = document.getString("id") ?: ""
                                                    val username = document.getString("username") ?: ""
                                                    val pfpUrl = document.getString("pfp_url") ?: ""
                                                    val friend = Friend(id, username, pfpUrl)
                                                    if (id != userId) {
                                                        friendList.add(friend)
                                                    }
                                                }
                                                adapter.updateAddFriends(friendList)
                                            }
                                    }
                            }
                    }
            } else {
                db.collection("friends").document(userId)
                    .collection("friend_usernames")
                    .get()
                    .addOnSuccessListener { friendsSnapshot ->
                        val friendUsernames =
                            friendsSnapshot.documents.map { it.id }.toMutableList()
                        if (friendUsernames.isEmpty()) {
                            friendUsernames.add("")
                        }
                        db.collection("friends").document(userId)
                            .collection("incoming_requests")
                            .get()
                            .addOnSuccessListener { requestsSnapshot ->
                                val requestUsernames =
                                    requestsSnapshot.documents.map { it.id }.toMutableList()
                                if (requestUsernames.isEmpty()) {
                                    requestUsernames.add("")
                                }
                                val combinedUsernames = friendUsernames + requestUsernames
                                db.collection("friends").document(userId)
                                    .collection("outgoing_requests")
                                    .get()
                                    .addOnSuccessListener { outgoingSnapshot ->
                                        val outgoingUsernames =
                                            outgoingSnapshot.documents.map { it.id }.toMutableList()
                                        if (outgoingUsernames.isEmpty()) {
                                            outgoingUsernames.add("")
                                        }
                                        val usedUsernames = combinedUsernames + outgoingUsernames
                                        db.collection("users")
                                            .whereNotIn("username", usedUsernames)
                                            .whereGreaterThanOrEqualTo("username", searchText)
                                            .whereLessThan("username", searchText + "\uf8ff")
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                val friendList = mutableListOf<Friend>()
                                                for (document in documents) {
                                                    val id = document.getString("id") ?: ""
                                                    val username = document.getString("username") ?: ""
                                                    val pfpUrl = document.getString("pfp_url") ?: ""
                                                    val friend = Friend(id, username, pfpUrl)
                                                    if (id != userId) {
                                                        friendList.add(friend)
                                                    }
                                                }
                                                adapter.updateAddFriends(friendList)
                                            }
                                    }
                            }
                    }
            }
        }
    }

    //Used ChatGPT to assist in the query
    private fun loadFriendRequests() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            if (searchText.isEmpty()) {
                db.collection("friends").document(userId)
                    .collection("incoming_requests")
                    .get()
                    .addOnSuccessListener { friendsSnapshot ->
                        val friendRequests = friendsSnapshot.documents.map { it.id }.toMutableList()
                        if(friendRequests.isEmpty()){
                            friendRequests.add("")
                        }
                        db.collection("users")
                            .whereIn("username", friendRequests)
                            .get()
                            .addOnSuccessListener { documents ->
                                val requestList = mutableListOf<Friend>()
                                for (document in documents) {
                                    val id = document.getString("id") ?: ""
                                    val username = document.getString("username") ?: ""
                                    val pfpUrl = document.getString("pfp_url") ?: ""
                                    val friend = Friend(id, username, pfpUrl)
                                    if(id != userId){
                                        requestList.add(friend)
                                    }
                                }
                                adapter.updateRequestFriends(requestList)
                            }
                    }
            } else {
                db.collection("friends").document(userId)
                    .collection("incoming_requests")
                    .get()
                    .addOnSuccessListener { requestsSnapshot ->
                        val friendRequests =
                            requestsSnapshot.documents.map { it.id }.toMutableList()
                        if (friendRequests.isEmpty()) {
                            friendRequests.add("")
                        }
                        db.collection("users")
                            .whereIn("username", friendRequests)
                            .whereGreaterThanOrEqualTo("username", searchText)
                            .whereLessThan("username", searchText + "\uf8ff")
                            .get()
                            .addOnSuccessListener { documents ->
                                val requestList = mutableListOf<Friend>()
                                for (document in documents) {
                                    val id = document.getString("id") ?: ""
                                    val username = document.getString("username") ?: ""
                                    val pfpUrl = document.getString("pfp_url") ?: ""
                                    val friend = Friend(id, username, pfpUrl)
                                    if (id != userId) {
                                        requestList.add(friend)
                                    }
                                }
                                adapter.updateRequestFriends(requestList)
                            }
                    }
            }
        }
    }
}