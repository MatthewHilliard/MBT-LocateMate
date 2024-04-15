package com.example.mbt_locatemate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso

class FriendListAdapter(private var friends: List<Friend>) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {
    private var onAddFriends: Boolean = false
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_friend, parent, false)
        auth = Firebase.auth
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
        onAddFriends = false
    }

    fun updateAddFriends(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
        onAddFriends = true
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendButton: ImageView = itemView.findViewById(R.id.friendButton)
        private val friendUser: TextView = itemView.findViewById(R.id.friend_user)
        private val pfpImage: ImageView = itemView.findViewById(R.id.pfp_friend)
        fun bind(friend: Friend) {
            friendUser.text = friend.username
            Picasso.get().load(friend.pfpUrl).into(pfpImage)
            if(onAddFriends){
                friendButton.setImageResource(R.drawable.baseline_person_add_alt_1_24)
            } else {
                friendButton.setImageResource(R.drawable.baseline_person_remove_24)
            }

            friendButton.setOnClickListener {
                if (onAddFriends) {
                    addFriend(friend)
                } else {
                    removeFriend(friend)
                }
            }
        }
    }

    private fun addFriend(friend: Friend){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val friendUsername = friend.username

            val friendDocumentRef = db.collection("friends")
                .document(userId)
                .collection("friend_usernames")
                .document(friendUsername)

            val emptyData: Map<String, Any> = HashMap()

            friendDocumentRef.set(emptyData)
        }
    }

    private fun removeFriend(friend: Friend){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val friendUsername = friend.username

            val friendDocumentRef = db.collection("friends")
                .document(userId)
                .collection("friend_usernames")
                .document(friendUsername)

            friendDocumentRef.delete()
        }
    }
}