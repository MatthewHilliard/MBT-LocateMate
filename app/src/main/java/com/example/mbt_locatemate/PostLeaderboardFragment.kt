package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PostLeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GuessListAdapter
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString(ARG_POST_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.guess_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = GuessListAdapter(mutableListOf())
        recyclerView.adapter = adapter

        postId?.let { loadLeaderboard(it) }
    }

    private fun loadLeaderboard(postId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId)
            .collection("guesses")
            .orderBy("distance")
            .get()
            .addOnSuccessListener { documents ->
                val guesses = documents.map { doc ->
                    Guess(doc.getString("userId") ?: "", doc.getDouble("distance") ?: 0.0)
                }
                //adapter.setGuesses(guesses) im gonna come back to this
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
            }
    }

    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: String) = PostLeaderboardFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_POST_ID, postId)
            }
        }
    }
}
