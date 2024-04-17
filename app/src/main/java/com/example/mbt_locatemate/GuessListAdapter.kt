package com.example.mbt_locatemate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuessListAdapter(private val guesses: List<Guess>) : RecyclerView.Adapter<GuessListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewUsername: TextView = view.findViewById(R.id.friend_user)
        val textViewDistance: TextView = view.findViewById(R.id.friend_guess)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_guess, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guess = guesses[position]
        holder.textViewUsername.text = guess.username
        holder.textViewDistance.text = String.format("%.2f km", guess.distance / 1000)
    }

    override fun getItemCount() = guesses.size
}