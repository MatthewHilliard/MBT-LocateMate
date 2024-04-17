package com.example.mbt_locatemate

import MapGuessFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PostListAdapter(private var posts: List<Post>,
                      private val fragmentManager: FragmentManager) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {
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
        private val postUser: TextView = itemView.findViewById(R.id.post_user)
        private val postCaption: TextView = itemView.findViewById(R.id.post_caption)
        private val postImage: ImageView = itemView.findViewById(R.id.post_image)
        private val pfpImage: ImageView = itemView.findViewById(R.id.post_pfp)
        private val guessButton: Button = itemView.findViewById(R.id.post_guess)

        init {
            guessButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val post = posts[position]
                    showGuessFragment(post)
                }
            }
        }

        fun bind(post: Post) {
            postUser.text = post.username
            postCaption.text = post.caption
            Picasso.get().load(post.imgUrl).into(postImage)
            Picasso.get().load(post.pfpUrl).into(pfpImage)
        }

        private fun showGuessFragment(post: Post) {
            // Assuming GuessFragment has a static newInstance method or similar
            val guessFragment = MapGuessFragment.newInstance()
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, guessFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}