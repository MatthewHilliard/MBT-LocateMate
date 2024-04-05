package com.example.mbt_locatemate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostListAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {
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
        fun bind(post: Post) {
            postUser.text = post.username
            postCaption.text = post.caption
        }
    }
}