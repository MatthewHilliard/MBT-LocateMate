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
import com.example.mbt_locatemate.ExploreFragment
import com.example.mbt_locatemate.Friend
import com.example.mbt_locatemate.Guess
import com.example.mbt_locatemate.GuessListAdapter
import com.example.mbt_locatemate.Leaderboard
import com.example.mbt_locatemate.LeaderboardListAdapter
import com.example.mbt_locatemate.MapGuessFragment
import com.example.mbt_locatemate.Post
import com.example.mbt_locatemate.PostLeaderboardFragment
import com.example.mbt_locatemate.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class FriendsLeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardListAdapter
    private lateinit var friends: List<Friend>
    private lateinit var leaderboardEntries: MutableList<Leaderboard>

    private var rank: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            val friends = bundle.getParcelableArrayList<Friend>("friendsList")
            friends?.let { friendList ->
                val db = FirebaseFirestore.getInstance()

                for (friend in friendList) {
                    db.collection("users").document(friend.id).collection("guesses")
                        .get()
                        .addOnSuccessListener { documents ->
                            val distances = documents.mapNotNull { it.getDouble("distance") }
                            val score = distances.average().takeIf { !distances.isEmpty() } ?: 0.0

                            leaderboardEntries.add(
                                Leaderboard(
                                    rank = 0,
                                    username = friend.username,
                                    pfpUrl = friend.pfpUrl,
                                    average = score
                                )
                            )
                        }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_post_leaderboard, container, false)

        recyclerView = view.findViewById(R.id.guess_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LeaderboardListAdapter(mutableListOf())
        recyclerView.adapter = adapter

        loadLeaderboard(leaderboardEntries)

        val backButton = view.findViewById<ImageView>(R.id.leaderboardBackButton)
        backButton.setOnClickListener(){
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exploreFragment).commit()
        }

        return view
    }

    private fun loadLeaderboard(leaderboardEntries: MutableList<Leaderboard>) {
        val sortedEntries = leaderboardEntries.sortedBy { it.average }

        //assign ranks
        sortedEntries.forEachIndexed { index, leaderboard ->
            leaderboard.rank = index + 1
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
 */