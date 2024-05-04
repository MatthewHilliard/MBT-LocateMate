package com.example.mbt_locatemate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
class LeaderboardListAdapter (private val friendsList: List<Leaderboard>) :
    RecyclerView.Adapter<LeaderboardListAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView = view.findViewById(R.id.leaderboardRank)
        val username: TextView = view.findViewById(R.id.leaderboardUser)
        val score: TextView = view.findViewById(R.id.leaderboardScore)
        val pfpImage: ImageView = itemView.findViewById(R.id.pfp_leaderboard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val leaderboard = friendsList[position]
        Log.d("LeaderboardListAdapter", "Loading image from URL: ${leaderboard.pfpUrl}")
        holder.rank.text = leaderboard.username
        holder.username.text = leaderboard.username
        holder.score.text = String.format("%.2f km", leaderboard.average / 1000)

        Picasso.get().load(leaderboard.pfpUrl).into(holder.pfpImage)
    }

    override fun getItemCount() = friendsList.size
}