package com.example.mbt_locatemate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PostListAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {
    var onCommentsClickListener: ((Post) -> Unit)? = null
    var onGuessClickListener: ((Post) -> Unit)? = null
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

        private val guessButton: Button = itemView.findViewById(R.id.post_guess)

        init {
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
        }

        fun bind(post: Post) {
            postUser.text = post.username
            postCaption.text = post.caption
            Picasso.get().load(post.imgUrl).into(postImage)
            Picasso.get().load(post.pfpUrl).into(pfpImage)
        }

    }
}