package com.example.mbt_locatemate.Fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DividerItemDecoration
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
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        //find friend info of current user within the database, take the average of all their guesses for the leaderboard
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

                                            val average = if (size > 0) (sum / size) / 1000.0 else 0.0
                                            if(average == 0.0){
                                                val friend = Leaderboard(username, -1.0, pfpUrl, -1, false)
                                                friendList.add(friend)
                                            } else {
                                                val friend = Leaderboard(username, average, pfpUrl, -1, false)
                                                friendList.add(friend)
                                            }

                                            if (friendList.size == friendUsernames.size) {
                                                val currentUserDocument = db.collection("users").document(currentUserId)
                                                currentUserDocument.get().addOnSuccessListener { userDocument ->
                                                    val userUsername = userDocument.getString("username") ?: ""
                                                    val userPfpUrl = userDocument.getString("pfp_url") ?: ""
                                                    db.collection("users").document(currentUserId)
                                                        .collection("guesses")
                                                        .get()
                                                        .addOnSuccessListener { userGuesses ->
                                                            val size = userGuesses.size()
                                                            var sum = 0.0
                                                            for (guess in userGuesses) {
                                                                val guessDistance = guess.getDouble("distance") ?: 0.0
                                                                sum += guessDistance
                                                            }
                                                            val average = if (size > 0) (sum / size) / 1000.0 else 0.0

                                                            if(average == 0.0){
                                                                val currentUserLeaderboard = Leaderboard(userUsername, -1.0, userPfpUrl, -1, true)
                                                                friendList.add(currentUserLeaderboard)
                                                            } else {
                                                                val currentUserLeaderboard = Leaderboard(userUsername, average, userPfpUrl, -1, true)
                                                                friendList.add(currentUserLeaderboard)
                                                            }

                                                            val sortedEntries = friendList.sortedWith(compareBy { if (it.average == -1.0) Double.MAX_VALUE else it.average })
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
                                }
                            }
                    } else {
                        //user has no friends, just load their own average of all guesses
                        val friendList = mutableListOf<Leaderboard>()
                        val currentUserDocument = db.collection("users").document(currentUserId)
                        currentUserDocument.get().addOnSuccessListener { userDocument ->
                            val userUsername = userDocument.getString("username") ?: ""
                            val userPfpUrl = userDocument.getString("pfp_url") ?: ""
                            db.collection("users").document(currentUserId)
                                .collection("guesses")
                                .get()
                                .addOnSuccessListener { userGuesses ->
                                    val size = userGuesses.size()
                                    var sum = 0.0
                                    for (guess in userGuesses) {
                                        val guessDistance = guess.getDouble("distance") ?: 0.0
                                        sum += guessDistance
                                    }
                                    val average = if (size > 0) (sum / size) / 1000.0 else 0.0

                                    val currentUserLeaderboard = Leaderboard(userUsername, average, userPfpUrl, -1, true)
                                    friendList.add(currentUserLeaderboard)

                                    val sortedEntries =
                                        friendList.sortedByDescending { it.average }
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
        }

        val backButton = view.findViewById<ImageView>(R.id.leaderboardBackButton)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }
}