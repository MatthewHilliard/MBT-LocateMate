package com.example.mbt_locatemate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

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
        fun bind(comment: Comment) {
            Log.d("CommentBind", comment.toString())
            commentUser.text = comment.username
            Picasso.get().load(comment.pfpUrl).into(pfpImage)
            commentText.text = comment.text
        }
    }
}