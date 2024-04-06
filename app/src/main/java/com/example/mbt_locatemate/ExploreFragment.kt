package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.UUID

class ExploreFragment: Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: PostListAdapter
    private lateinit var postRecyclerView: RecyclerView

    private lateinit var segmentedButton: MaterialButtonToggleGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        postRecyclerView = view.findViewById(R.id.post_recycler_view)
        layoutManager = LinearLayoutManager(requireContext())
        postRecyclerView.layoutManager = layoutManager

        adapter = PostListAdapter(mutableListOf())
        postRecyclerView.adapter = adapter

        val dummyPosts = generateDummyPosts()
        adapter.updatePosts(dummyPosts)

        segmentedButton = view.findViewById(R.id.segmentedButton)

        segmentedButton.addOnButtonCheckedListener { segmentedButton, checkedId, isChecked ->
            when (checkedId) {
                R.id.friendsButton -> {

                }
                R.id.exploreButton -> {

                }
            }
        }

        return view
    }
    private fun generateDummyPosts(): List<Post> {
        val dummyList = mutableListOf<Post>()
        for (i in 1..10) {
            val username = "User $i"
            val caption = "This is post number $i"
            dummyList.add(Post(UUID.randomUUID(), username, caption))
        }
        return dummyList
    }
}