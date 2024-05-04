package com.example.mbt_locatemate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CommentListAdapter(private var comments: List<Comment>) : RecyclerView.Adapter<CommentListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_comment, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    fun updateComments(newComments: List<Comment>) {
        Log.d("NewComments", newComments.toString())
        comments = newComments
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val commentUser: TextView = itemView.findViewById(R.id.comment_user)
        private val pfpImage: ImageView = itemView.findViewById(R.id.pfp_comment)
        private val commentText: TextView = itemView.findViewById(R.id.comment_text)
        private val commentTime: TextView = itemView.findViewById(R.id.time_ago)
        fun bind(comment: Comment) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    Log.d("CommentBind", comment.toString())
                    commentUser.text = comment.username
                    Picasso.get().load(comment.pfpUrl).into(pfpImage)
                    commentText.text = comment.text
                    commentTime.text = calculateCommentTime(comment.timestamp)
                }
            }
        }

        //converting timestamp into a string for comment list items
        //used ChatGPT to assist with time conversion
        private fun calculateCommentTime(timestamp: Long): String {
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - timestamp

            val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
            val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
            val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
            val weeks = days / 7
            val years = weeks / 52

            return when {
                years > 0 -> "$years" + "y"
                weeks > 0 -> "$weeks" + "w"
                days > 0 -> "$days" + "d"
                hours > 0 -> "$hours" + "h"
                minutes > 0 -> "$minutes" + "m"
                else -> "$seconds" + "s"
            }
        }
    }
}