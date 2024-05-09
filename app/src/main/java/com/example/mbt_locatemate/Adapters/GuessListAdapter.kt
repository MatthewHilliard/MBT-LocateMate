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

class GuessListAdapter(private var guesses: List<Guess>) : RecyclerView.Adapter<GuessListAdapter.ViewHolder>() {
    var onGuessClickListener: ((Post) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewRank: TextView = view.findViewById(R.id.leaderboardRank)
        val textViewUsername: TextView = view.findViewById(R.id.guess_user)
        val textViewDistance: TextView = view.findViewById(R.id.guess_distance)
        val pfpImage: ImageView = itemView.findViewById(R.id.pfp_guess)
        val medalImageView: ImageView = itemView.findViewById(R.id.medalImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_guess, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guess = guesses[position]
        Log.d("GuessListAdapter", "Loading image from URL: ${guess.pfpUrl}")
        holder.textViewRank.text = guess.rank.toString()
        holder.textViewUsername.text = guess.username
        holder.textViewDistance.text = String.format("%.2f km", guess.distance / 1000)

        Picasso.get().load(guess.pfpUrl).into(holder.pfpImage)

        //load medal image view if in top 3 guesses
        when (guess.rank) {
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

        //highlight current user guess
        if (guess.isCurrentUser) {
            holder.itemView.setBackgroundResource(R.color.md_theme_surfaceContainerHigh)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount() = guesses.size

    fun setGuesses(guesses: List<Guess>) {
        this.guesses = guesses
        notifyDataSetChanged()
    }
}