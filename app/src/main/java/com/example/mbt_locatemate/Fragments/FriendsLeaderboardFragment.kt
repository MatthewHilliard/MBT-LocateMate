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
import com.example.mbt_locatemate.Leaderboard
import com.example.mbt_locatemate.LeaderboardListAdapter
import com.example.mbt_locatemate.R
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
            val friends = bundle.getParcelableArrayList<Friend>("friendsList")

            friends?.let { friendList ->
                val db = FirebaseFirestore.getInstance()
                var fetchCount = 0

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
                            fetchCount++
                            if (fetchCount == friendList.size) {
                                loadLeaderboard(leaderboardEntries)
                            }
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
        val sortedEntries = leaderboardEntries.sortedBy { it.average }

        //assign ranks
        sortedEntries.forEachIndexed { index, leaderboard ->
            leaderboard.rank = index + 1
        }

        adapter = LeaderboardListAdapter(sortedEntries)
        recyclerView.adapter = adapter
    }

    /*
    override fun onBindViewHolder(holder: LeaderboardListAdapter.LeaderboardViewHolder, position: Int) {
        val leaderboard = sortedEntries[position]
        holder.rank.text = leaderboard.rank.toString()
        holder.username.text = leaderboard.username
        holder.score.text = String.format("%.2f km", leaderboard.average)
        Glide.with(holder.imageView.context).load(leaderboard.pfpUrl).into(holder.imageView)

        when (position) {
            0 -> holder.medalView.setImageResource(R.drawable.ic_medal_gold)
            1 -> holder.medalView.setImageResource(R.drawable.ic_medal_silver)
            2 -> holder.medalView.setImageResource(R.drawable.ic_medal_bronze)
            else -> holder.medalView.visibility = View.GONE  // Hide the medal view for other positions
        }
    }

    */

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