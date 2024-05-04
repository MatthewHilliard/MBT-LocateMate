package com.example.mbt_locatemate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso

class FriendListAdapter(private var friends: List<Friend>) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {
    private var onAddFriends: Boolean = false
    private var onRequestFriends: Boolean = false
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateFriends(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
        onAddFriends = false
        onRequestFriends = false
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAddFriends(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
        onAddFriends = true
        onRequestFriends = false
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateRequestFriends(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
        onAddFriends = false
        onRequestFriends = true
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkButton: ImageView = itemView.findViewById(R.id.checkButton)
        private val friendButton: ImageView = itemView.findViewById(R.id.friendButton)
        private val friendUser: TextView = itemView.findViewById(R.id.friend_user)
        private val pfpImage: ImageView = itemView.findViewById(R.id.pfp_friend)
        fun bind(friend: Friend) {
            friendUser.text = friend.username
            Picasso.get().load(friend.pfpUrl).into(pfpImage)
            //changing icon next to friend based on if we are requesting, accepting, or removing friend
            if(onAddFriends) {
                checkButton.isVisible = false
                friendButton.setImageResource(R.drawable.baseline_person_add_alt_1_24)
            } else if(onRequestFriends){
                checkButton.isVisible = true
                friendButton.setImageResource(R.drawable.baseline_close_24)
            } else {
                checkButton.isVisible = false
                friendButton.setImageResource(R.drawable.baseline_person_remove_24)
            }

            //actions for requesting, denying, removing friend
            friendButton.setOnClickListener {
                if (onAddFriends) {
                    moveToRequest(friend)
                } else if (onRequestFriends){
                    denyRequest(friend)
                } else {
                    removeFriend(friend)
                }
            }

            checkButton.setOnClickListener{
                addFriend(friend)
            }
        }
    }

    private fun addFriend(friend: Friend){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val friendUsername = friend.username
            //get a reference to users friends
            val friendDocumentRef = db.collection("friends")
                .document(userId)
                .collection("friend_usernames")
                .document(friendUsername)

            val friendData = mapOf(
                "id" to friend.id,
                "username" to friendUsername,
                "pfp_url" to friend.pfpUrl
            )
            //add the friend to the database
            friendDocumentRef.set(friendData).addOnSuccessListener {
                val updatedFriendsList = friends.toMutableList()
                updatedFriendsList.remove(friend)
                //update adapter friend list
                updateAddFriends(updatedFriendsList)
            }
            //get a reference to and delete the friend request from the database (now that it is accepted)
            val friendRequestRef = db.collection("friends")
                .document(userId)
                .collection("incoming_requests")
                .document(friendUsername)

            friendRequestRef.delete().addOnSuccessListener {

            }


            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                //add self to friend list of the other user
                val userData = mapOf(
                    "id" to userId,
                    "username" to document.getString("username"),
                    "pfp_url" to document.getString("pfp_url")
                )

                val userDocumentRef = userData["username"]?.let {
                    db.collection("friends")
                        .document(friend.id)
                        .collection("friend_usernames")
                        .document(it)
                }

                userDocumentRef?.set(userData)?.addOnSuccessListener {

                }
                //delete outgoing request from friend side of the request
                val userRequestRef = userData["username"]?.let {
                    db.collection("friends")
                        .document(friend.id)
                        .collection("outgoing_requests")
                        .document(it)
                }
                userRequestRef?.delete()?.addOnSuccessListener {

                }
            }
        }
    }

    private fun removeFriend(friend: Friend){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            //get a reference to the friend in our friends collection
            val friendDocumentRef = db.collection("friends")
                .document(userId)
                .collection("friend_usernames")
                .document(friend.username)

            //delete the friend from our friend collection
            friendDocumentRef.delete().addOnSuccessListener {
                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                    val userData = mapOf(
                        "id" to userId,
                        "username" to document.getString("username"),
                        "pfp_url" to document.getString("pfp_url")
                    )
                    //delete ourselves from the friend's friend list
                    val userDocumentRef = userData["username"]?.let { it1 ->
                        db.collection("friends")
                            .document(friend.id)
                            .collection("friend_usernames")
                            .document(it1)
                    }

                    userDocumentRef?.delete()?.addOnSuccessListener {
                        val updatedFriendsList = friends.toMutableList()
                        updatedFriendsList.remove(friend)

                        updateFriends(updatedFriendsList)
                    }
                }
            }
        }
    }

    //i requested somebody, so i want to move that person out of my "add friends" tab
    private fun moveToRequest(friend: Friend){
        val userId = auth.currentUser?.uid
        if (userId != null) {

            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                val userData = mapOf(
                    "id" to userId,
                    "username" to document.getString("username"),
                    "pfp_url" to document.getString("pfp_url")
                )
                //add my request to friend's incoming requests
                val userDocumentRef = document.getString("username")?.let {
                    db.collection("friends")
                        .document(friend.id)
                        .collection("incoming_requests")
                        .document(it)
                }

                val friendData = mapOf(
                    "id" to friend.id,
                    "username" to friend.username,
                    "pfp_url" to friend.pfpUrl
                )
                //add friend data to my outgoing requests
                val friendRequestRef = userData["id"]?.let { it ->
                        db.collection("friends")
                            .document(it)
                            .collection("outgoing_requests")
                            .document(friend.username)
                }
                userDocumentRef?.set(userData)?.addOnSuccessListener {
                    friendRequestRef?.set(friendData)?.addOnSuccessListener {
                        val updatedFriendsList = friends.toMutableList()
                        updatedFriendsList.remove(friend)
                        //update add friend list adapter
                        updateAddFriends(updatedFriendsList)
                    }
                }
            }
        }
    }

    //deny a request from another user
    private fun denyRequest(friend: Friend){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            //
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                val userData = mapOf(
                    "id" to userId,
                    "username" to document.getString("username"),
                    "pfp_url" to document.getString("pfp_url")
                )
                //delete outgoing request from friend side
                val userDocumentRef = userData["username"]?.let {
                    db.collection("friends")
                        .document(friend.id)
                        .collection("outgoing_requests")
                        .document(it)
                }

                userDocumentRef?.delete()?.addOnSuccessListener {
                    //delete incoming request from our side
                    val friendDocumentRef = db.collection("friends")
                        .document(userId)
                        .collection("incoming_requests")
                        .document(friend.username)

                    friendDocumentRef.delete().addOnSuccessListener {
                        val updatedFriendsList = friends.toMutableList()
                        updatedFriendsList.remove(friend)
                        //update friend list adapter
                        updateFriends(updatedFriendsList)
                    }
                }
            }
        }
    }
}