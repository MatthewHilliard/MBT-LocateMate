package com.example.mbt_locatemate

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class LeaderboardListAdapter (private var friendsList: List<Leaderboard>) : RecyclerView.Adapter<LeaderboardListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friendsList[position])
    }

    override fun getItemCount() = friendsList.size
    fun updateLeaderboard(newLeaderboard: List<Leaderboard>) {
        friendsList = newLeaderboard
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rank: TextView = itemView.findViewById(R.id.leaderboardRank)
        private val username: TextView = itemView.findViewById(R.id.leaderboardUser)
        private val score: TextView = itemView.findViewById(R.id.leaderboardScore)
        private val pfpImage: ImageView = itemView.findViewById(R.id.pfp_leaderboard)
        private val medalImageView: ImageView = itemView.findViewById(R.id.medalImageView)
        fun bind(leaderboard: Leaderboard) {
            rank.text = leaderboard.rank.toString()
            username.text = leaderboard.username
            //display if user has no guesses
            if(leaderboard.average == -1.0){
                score.text = "Has no guesses"
            } else{
                //format guess numbers to have commas and two decimal places
                val formatter = DecimalFormat("#,###.##")
                val formattedNumber = formatter.format(leaderboard.average)
                score.text = formattedNumber.toString() + " km"
            }
            Picasso.get().load(leaderboard.pfpUrl).into(pfpImage)

          //set gold silver and bronze medals for ranks 1-3
            when (leaderboard.rank) {
                1 -> {
                    medalImageView.setImageResource(R.drawable.ic_gold_medal)
                    medalImageView.visibility = View.VISIBLE
                }

                2 -> {
                    medalImageView.setImageResource(R.drawable.ic_silver_medal)
                    medalImageView.visibility = View.VISIBLE
                }

                3 -> {
                    medalImageView.setImageResource(R.drawable.ic_bronze_medal)
                    medalImageView.visibility = View.VISIBLE
                }

                else -> medalImageView.visibility = View.GONE
            }
       
        //highlight the current user's view
            if (leaderboard.isCurrentUser) {
                itemView.setBackgroundResource(R.color.md_theme_surfaceContainerHigh)
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }
}