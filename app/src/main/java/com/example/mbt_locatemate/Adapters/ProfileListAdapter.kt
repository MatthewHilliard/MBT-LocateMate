package com.example.mbt_locatemate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePostListAdapter(
    private var posts: List<Post>,
    private val onItemClick: (Post) -> Unit
) : RecyclerView.Adapter<ProfilePostListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_cell, parent, false)
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
        private val postCaption: TextView = itemView.findViewById(R.id.cardCaption)
        private val postImage: ImageView = itemView.findViewById(R.id.cardPost)
        init {
            itemView.setOnClickListener {
                //navigate to individual post when clicked
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    val postId = posts[position].id
                    Log.d("OnClick", "post $postId clicked!!")
                    onItemClick(posts[position])
                }
            }
        }
        fun bind(post: Post) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    Picasso.get().load(post.imgUrl).into(postImage)
                    postCaption.text = post.caption
                }
            }
        }
    }
}