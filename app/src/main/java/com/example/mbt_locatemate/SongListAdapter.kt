package com.example.mbt_locatemate

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
private var mediaPlayer: MediaPlayer? = null

class SongListAdapter(private var songs: List<Result>) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {
    private var clickedPosition: Int = RecyclerView.NO_POSITION
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(songs[position], position == clickedPosition)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSongs(newSongs: List<Result>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    fun stopMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songTitle: TextView = itemView.findViewById(R.id.song_title)
        private val songImage: ImageView = itemView.findViewById(R.id.song_image)
        private val acceptButton: ImageView = itemView.findViewById(R.id.add_song)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currSong = songs[position]
                    mediaPlayer?.release()
                    mediaPlayer = null
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(currSong.audio)
                        prepareAsync()
                        setOnPreparedListener {
                            it.start()
                        }
                        setOnErrorListener { mp, what, extra ->
                            false
                        }
                    }
                    clickedPosition = position
                    notifyDataSetChanged()
                }
            }
        }

        fun bind(song: Result, isClicked: Boolean) {
            songTitle.text = song.name
            Picasso.get().load(song.album_image).into(songImage)
            if (isClicked) {
                itemView.setBackgroundResource(R.color.md_theme_surfaceContainerHigh)
            } else {
                itemView.setBackgroundResource(android.R.color.transparent)
            }

            acceptButton.setOnClickListener{

            }
        }
    }
}