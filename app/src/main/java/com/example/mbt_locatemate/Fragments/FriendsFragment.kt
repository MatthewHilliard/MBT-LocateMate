package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mbt_locatemate.Fragments.FriendsLeaderboardFragment
import com.google.android.gms.tasks.Tasks
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore

class FriendsFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: FriendListAdapter
    private lateinit var friendRecyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var searchText: String

    private lateinit var leaderboardButton: ImageView

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

        leaderboardButton = view.findViewById(R.id.leaderboardButton)
        leaderboardButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                fetchLeaderboardList(userId, object : DataReadyListener {
                    override fun onDataReady(friendsList: MutableList<Friend>) {
                        if (friendsList.isNotEmpty()) {
                            val friendsLeaderboardFragment = FriendsLeaderboardFragment().apply {
                                arguments = Bundle().apply {
                                    putParcelableArrayList("friendsList", ArrayList(friendsList))
                                }
                            }
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, friendsLeaderboardFragment)
                                .commit()
                        }
                    }

                    override fun onError(error: Exception) {
                        Log.e("FriendsFragment", "Error fetching data: ${error.message}")
                    }
                })
            }
        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //auth = Firebase.auth

        loadFriends()
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

    interface DataReadyListener {
        fun onDataReady(friendsList: MutableList<Friend>)
        fun onError(error: Exception)
    }
    fun fetchLeaderboardList(userId: String, listener: DataReadyListener) {
        val friendsList = mutableListOf<Friend>()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDocument ->
                //get friends fields from user document
                val id = userDocument.id
                val username = userDocument.getString("username") ?: "Unknown"
                val pfpUrl = userDocument.getString("pfpUrl") ?: ""

                val currentUser = Friend(id, username, pfpUrl)
                friendsList.add(currentUser)

                // fetch friends
                fetchFriends(userId, friendsList, listener)
            }
            .addOnFailureListener { e ->
                Log.e("FriendsFragment", "Error fetching user details", e)
            }
    }

    private fun fetchFriends(userId: String, friendsList: MutableList<Friend>, listener: DataReadyListener) {
        db.collection("users").document(userId).collection("friends").get()
            .addOnSuccessListener { friendsSnapshot ->
                val count = friendsSnapshot.documents.size
                if (count == 0) {
                    listener.onDataReady(friendsList)
                }

                var processedCount = 0
                for (friendRef in friendsSnapshot.documents) {
                    val friendId = friendRef.id
                    db.collection("users").document(friendId).get()
                        .addOnSuccessListener { friendDoc ->
                            val friendId = friendDoc.id
                            val friendUsername = friendDoc.getString("username") ?: "Unknown"
                            val friendPfpUrl = friendDoc.getString("pfpUrl") ?: ""

                            friendsList.add(Friend(friendId, friendUsername, friendPfpUrl))

                            processedCount++
                            if (processedCount == count) {
                                listener.onDataReady(friendsList)  //only call once all friends have been added
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FriendsFragment", "Error fetching friend details", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FriendsFragment", "Error fetching friends list", e)
            }
    }

}