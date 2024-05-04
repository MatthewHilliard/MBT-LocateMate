package com.example.mbt_locatemate.Fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mbt_locatemate.ExploreFragment
import com.example.mbt_locatemate.Leaderboard
import com.example.mbt_locatemate.LeaderboardListAdapter
import com.example.mbt_locatemate.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsLeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_friend_leaderboard, container, false)

        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        recyclerView = view.findViewById(R.id.leaderboard_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LeaderboardListAdapter(mutableListOf())
        recyclerView.adapter = adapter

        if (currentUserId != null) {
            db.collection("friends").document(currentUserId)
                .collection("friend_usernames")
                .get()
                .addOnSuccessListener { friendsSnapshot ->
                    val friendUsernames = friendsSnapshot.documents.map { it.id }
                    if (friendUsernames.isNotEmpty()) {
                        db.collection("users")
                            .whereIn("username", friendUsernames)
                            .get()
                            .addOnSuccessListener { documents ->
                                val friendList = mutableListOf<Leaderboard>()
                                for (document in documents) {
                                    val id = document.getString("id") ?: ""
                                    val username = document.getString("username") ?: ""
                                    val pfpUrl = document.getString("pfp_url") ?: ""

                                    db.collection("users").document(id)
                                        .collection("guesses")
                                        .get()
                                        .addOnSuccessListener { friendGuesses ->
                                            val size = friendGuesses.size()
                                            var sum = 0.0
                                            for (guess in friendGuesses) {
                                                val guessDistance =
                                                    guess.getDouble("distance") ?: 0.0
                                                sum += guessDistance
                                            }

                                            val average = if (size > 0) sum / size else 0.0
                                            val friend = Leaderboard(username, average, pfpUrl, -1, false)
                                            friendList.add(friend)

                                            if (friendList.size == friendUsernames.size) {
                                                val sortedEntries = friendList.sortedByDescending { it.average }
                                                var rank = 1
                                                sortedEntries.forEach { leaderboard ->
                                                    leaderboard.rank = rank
                                                    rank++
                                                }
                                                adapter.updateLeaderboard(sortedEntries)
                                            }
                                        }
                                }
                            }
                    } else {
                        val friendList = mutableListOf<Leaderboard>()
//                        db.collection("users").document(currentUserId).get().addOnSuccessListener { user ->
//                            val username = user.getString("username") ?: ""
//                            val pfpUrl = user.getString("pfp_url") ?: ""
//                            val
//                        }
                        adapter.updateLeaderboard(friendList)
                    }
                }
        }

        val backButton = view.findViewById<ImageView>(R.id.leaderboardBackButton)
        backButton.setOnClickListener() {
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exploreFragment).commit()
        }

        return view
    }
}