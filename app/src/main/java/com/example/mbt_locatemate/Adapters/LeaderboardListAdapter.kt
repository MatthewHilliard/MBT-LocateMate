package com.example.mbt_locatemate

import android.graphics.Color
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
        val medalImageView: ImageView = itemView.findViewById(R.id.medalImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val leaderboard = friendsList[position]

        Log.d("LeaderboardListAdapter", "Loading image from URL: ${leaderboard.pfpUrl}")

        holder.rank.text = if (leaderboard.rank == -1) "" else leaderboard.rank.toString()

        holder.username.text = leaderboard.username

        holder.score.text = if (leaderboard.average != null) {
            String.format("%.2f km", leaderboard.average / 1000)
        } else {
            "No guesses yet!"
        }

        if (leaderboard.pfpUrl.isNotEmpty()) {
            Picasso.get().load(leaderboard.pfpUrl).into(holder.pfpImage)
        } else {
            holder.pfpImage.setImageResource(R.drawable.vacation_test)
        }

        when (leaderboard.rank) {
            1 -> {
                holder.medalImageView.setImageResource(R.drawable.ic_gold_medal)
                holder.medalImageView.visibility = View.VISIBLE
            }
            2 -> {
                holder.medalImageView.setImageResource(R.drawable.ic_silver_medal)
                holder.medalImageView.visibility = View.VISIBLE
            }
            3 -> {
                holder.medalImageView.setImageResource(R.drawable.ic_bronze_medal)
                holder.medalImageView.visibility = View.VISIBLE
            }
            else -> holder.medalImageView.visibility = View.GONE
        }

        //highlight the current user's view
        if (leaderboard.isCurrentUser) {
            holder.itemView.setBackgroundColor(Color.parseColor("#409440D3"))
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }


    override fun getItemCount() = friendsList.size
}