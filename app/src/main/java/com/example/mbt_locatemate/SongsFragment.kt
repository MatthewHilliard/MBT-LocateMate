package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongsFragment : BottomSheetDialogFragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: SongListAdapter
    private lateinit var songRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
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

        adapter = SongListAdapter(mutableListOf())
        songRecyclerView.adapter = adapter

//        searchView = view.findViewById(R.id.song_search)
//        searchView.clearFocus()
//        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                if (query != null) {
//
//                }
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                return true
//            }
//        })

        showRandomSongs()
        return view
    }

    private fun showRandomSongs() {
        val call: Call<SongResponse> = api.getSongs("")
        call.enqueue(object : Callback<SongResponse> {
            override fun onResponse(call: Call<SongResponse>, response: Response<SongResponse>) {
                if (response.isSuccessful) {
                    val songs = response.body()?.results ?: emptyList()
                    Log.d("SongsFragment", "$songs")
                    adapter.updateSongs(songs)
                }
            }

            override fun onFailure(call: Call<SongResponse>, t: Throwable) {
                Log.d("SongsFragment", "Failed to fetch songs")
            }
        })
    }

//    private fun searchSongs(query: String){
//        val call: Call<NewsResponse> = api.getEverything(query)
//        call.enqueue(object : Callback<NewsResponse> {
//            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
//                if (response.isSuccessful) {
//                    val articles = response.body()?.articles ?: emptyList()
//                    updateAdapter(articles)
//                }
//            }
//
//            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
//                Log.d("ArticleListFragment", "Failed to fetch articles")
//            }
//        })
//    }
}