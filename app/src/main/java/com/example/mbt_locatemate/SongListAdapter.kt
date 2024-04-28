package com.example.mbt_locatemate

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongListAdapter(private var songs: List<Result>) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSongs(newSongs: List<Result>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songTitle: TextView = itemView.findViewById(R.id.song_title)

//        init {
//            itemView.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onItemClick(songs[position])
//                }
//            }
//        }

        fun bind(song: Result) {
            songTitle.text = song.name
        }
    }
}