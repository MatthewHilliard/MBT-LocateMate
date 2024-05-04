package com.example.mbt_locatemate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PostListAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {
    var onCommentsClickListener: ((Post) -> Unit)? = null
    var onGuessClickListener: ((Post) -> Unit)? = null
    var onLeaderboardClickListener: ((Post) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_post, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val leaderboardButton: ImageView = itemView.findViewById(R.id.leaderboardButton)
        private val commentsButton: ImageView = itemView.findViewById(R.id.commentsButton)

        private val postUser: TextView = itemView.findViewById(R.id.post_user)
        private val postCaption: TextView = itemView.findViewById(R.id.post_caption)
        private val postImage: ImageView = itemView.findViewById(R.id.post_image)
        private val pfpImage: ImageView = itemView.findViewById(R.id.post_pfp)
        private val timeAgo: TextView = itemView.findViewById(R.id.time_ago)

        private val guessButton: Button = itemView.findViewById(R.id.post_guess)

        init {
            //navigate to comments, guess, or leaderboard fragments when clicked
            guessButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onGuessClickListener?.invoke(posts[position])
                }
            }

            commentsButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCommentsClickListener?.invoke(posts[position])
                }
            }

            leaderboardButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLeaderboardClickListener?.invoke(posts[position])
                }
            }
        }

        fun bind(post: Post) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    postUser.text = post.username
                    postCaption.text = post.caption
                    Picasso.get().load(post.imgUrl).into(postImage)
                    Picasso.get().load(post.pfpUrl).into(pfpImage)
                    timeAgo.text = calculateTimeAgo(post.timestamp)
                }
            }
        }

        //used ChatGPT to assist with time conversion
        private fun calculateTimeAgo(timestamp: Long): String {
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - timestamp

            val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
            val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
            val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
            val weeks = days / 7
            val years = weeks / 52
            if (days.toInt() == 1 || seconds.toInt() == 1 || minutes.toInt() == 1 || hours.toInt() == 1) {
                return when {
                    years > 0 -> "$years year ago"
                    weeks > 0 -> "$weeks week ago"
                    days > 0 -> "$days day ago"
                    hours > 0 -> "$hours hour ago"
                    minutes > 0 -> "$minutes minute ago"
                    else -> "$seconds second ago"
                }
            }
            return when {
                years > 0 -> "$years years ago"
                weeks > 0 -> "$weeks weeks ago"
                days > 0 -> "$days days ago"
                hours > 0 -> "$hours hours ago"
                minutes > 0 -> "$minutes minutes ago"
                else -> "$seconds seconds ago"
            }
        }
    }
}