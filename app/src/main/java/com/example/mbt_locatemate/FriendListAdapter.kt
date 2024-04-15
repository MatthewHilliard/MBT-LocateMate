package com.example.mbt_locatemate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class FriendListAdapter(private var friends: List<Friend>) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_friend, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    fun updateFriends(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendUser: TextView = itemView.findViewById(R.id.friend_user)
        private val pfpImage: ImageView = itemView.findViewById(R.id.pfp_friend)
        fun bind(friend: Friend) {
            friendUser.text = friend.username
            Picasso.get().load(friend.pfpUrl).into(pfpImage)
        }
    }
}