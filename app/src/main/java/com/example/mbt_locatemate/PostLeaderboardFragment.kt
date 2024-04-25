package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

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
    ): View {
        val view = inflater.inflate(R.layout.fragment_post_leaderboard, container, false)

        recyclerView = view.findViewById(R.id.guess_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = GuessListAdapter(mutableListOf())
        recyclerView.adapter = adapter

        postId?.let { loadLeaderboard(it) }

        val backButton = view.findViewById<ImageView>(R.id.leaderboardBackButton)
        backButton.setOnClickListener(){
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exploreFragment).commit()
        }

        return view
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
                Toast.makeText(context, "Failure ??", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: String) = PostLeaderboardFragment().apply {
            return PostLeaderboardFragment()
        }

        fun newInstanceWithArgs(someId: String): PostLeaderboardFragment {
            val bundle = Bundle()
            bundle.putString("some_key", someId)
            val fragment = PostLeaderboardFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
