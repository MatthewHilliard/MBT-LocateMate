package com.example.mbt_locatemate.Fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mbt_locatemate.CommentListAdapter
import com.example.mbt_locatemate.ExploreFragment
import com.example.mbt_locatemate.Friend
import com.example.mbt_locatemate.Leaderboard
import com.example.mbt_locatemate.LeaderboardListAdapter
import com.example.mbt_locatemate.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class FriendsLeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardListAdapter
    private lateinit var friends: List<Friend>
    private lateinit var sortedEntries: List<Leaderboard>

    private var leaderboardEntries: MutableList<Leaderboard> = mutableListOf()

    private var rank: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            friends = bundle.getParcelableArrayList<Friend>("friendsList") ?: emptyList()

            val db = FirebaseFirestore.getInstance()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            var fetchCount = 0

            friends.forEach { friend ->
                db.collection("users").document(friend.id).collection("guesses")
                    .get()
                    .addOnSuccessListener { documents ->
                        val distances = documents.mapNotNull { it.getDouble("distance") }
                        val score = distances.average().takeIf { !distances.isEmpty() }

                        leaderboardEntries.add(
                            Leaderboard(
                                rank = 0, // Will be assigned later
                                username = friend.username,
                                pfpUrl = friend.pfpUrl,
                                average = score,
                                isCurrentUser = friend.id == currentUserId
                            )
                        )
                        fetchCount++
                        if (fetchCount == friends.size) {
                            loadLeaderboard(leaderboardEntries)
                        }
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_friend_leaderboard, container, false)

        recyclerView = view.findViewById(R.id.leaderboard_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LeaderboardListAdapter(mutableListOf())
        recyclerView.adapter = adapter

        val backButton = view.findViewById<ImageView>(R.id.leaderboardBackButton)
        backButton.setOnClickListener() {
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exploreFragment).commit()
        }

        return view
    }

    private fun loadLeaderboard(leaderboardEntries: MutableList<Leaderboard>) {
        val sortedEntries = leaderboardEntries.sortedWith(compareBy(nullsLast<Double>()) { it.average })

        var rank = 1
        sortedEntries.forEach { leaderboard ->
            leaderboard.rank = if (leaderboard.average != null) rank else -1
            if (leaderboard.average != null) rank++
        }

        adapter = LeaderboardListAdapter(sortedEntries)
        recyclerView.adapter = adapter
    }

    /*
    companion object {
        const val ARG_POST = "post"

        fun newInstance(post: Post): PostLeaderboardFragment {
            val fragment = PostLeaderboardFragment()
            val args = Bundle().apply {
                putParcelable(ARG_POST, post)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
*/
}