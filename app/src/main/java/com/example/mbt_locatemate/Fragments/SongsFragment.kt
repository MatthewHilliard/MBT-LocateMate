package com.example.mbt_locatemate

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongsFragment : BottomSheetDialogFragment(), SongListAdapter.SongClickListener {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: SongListAdapter
    private lateinit var songRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private var songSelectionListener: SongSelectionListener? = null
    interface SongSelectionListener {
        fun onSongSelected(audioUrl: String)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song_sheet, container, false)

        songRecyclerView = view.findViewById(R.id.song_recycler_view)
        songRecyclerView.addItemDecoration(
            DividerItemDecoration(
                songRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        layoutManager = LinearLayoutManager(requireContext())
        songRecyclerView.layoutManager = layoutManager

        adapter = SongListAdapter(mutableListOf(), this)
        songRecyclerView.adapter = adapter

        searchView = view.findViewById(R.id.song_search)
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchSongs(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.standard_bottom_sheet) as LinearLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.peekHeight = bottomSheet.height
        }
        //show random songs if the search bar is empty
        showRandomSongs()
        return view
    }

    override fun onAcceptClicked(audioUrl: String) {
        Toast.makeText(context, "Song added to post!", Toast.LENGTH_SHORT).show()
        songSelectionListener?.onSongSelected(audioUrl)
        dismiss()
    }

    fun setSongSelectionListener(listener: SongSelectionListener) {
        this.songSelectionListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.stopMediaPlayer()
    }

    private fun showRandomSongs() {
        val call: Call<SongResponse> = api.getSongs("")
        call.enqueue(object : Callback<SongResponse> {
            override fun onResponse(call: Call<SongResponse>, response: Response<SongResponse>) {
                if (response.isSuccessful) {
                    val songs = response.body()?.results ?: emptyList()
                    adapter.updateSongs(songs)
                }
            }

            override fun onFailure(call: Call<SongResponse>, t: Throwable) {
                Log.d("SongsFragment", "Failed to fetch songs")
            }
        })
    }

    //search Jamendo songs based on user input
    private fun searchSongs(query: String){
        val call: Call<SongResponse> = api.getSongs(query)
        call.enqueue(object : Callback<SongResponse> {
            override fun onResponse(call: Call<SongResponse>, response: Response<SongResponse>) {
                if (response.isSuccessful) {
                    val songs = response.body()?.results ?: emptyList()
                    adapter.updateSongs(songs)
                }
            }

            override fun onFailure(call: Call<SongResponse>, t: Throwable) {
                Log.d("SongsFragment", "Failed to fetch songs")
            }
        })
    }
}