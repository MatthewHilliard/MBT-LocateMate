package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostLeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GuessListAdapter
    private lateinit var post: Post
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        post = arguments?.getParcelable<Post>(MapGuessFragment.ARG_POST)!!
        postId = post.id
        if (postId == null) {
            Log.e("PostLeaderboardFragment", "Post ID is null")
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
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            loadLeaderboard(postId)
        }

        val backButton = view.findViewById<ImageView>(R.id.leaderboardBackButton)
        backButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        return view
    }


    //load the guesses for the post and rank by highest guess
    private fun loadLeaderboard(postId: String) {
        Log.d("PostLeaderboardFragment", "Loading leaderboard for post: $postId")
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        var currentUsername = String()

        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { userDoc ->
                    currentUsername = userDoc.getString("username") ?: "Unknown"
                }
        }

        db.collection("posts").document(postId)
            .collection("guesses")
            .orderBy("distance")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("PostLeaderboardFragment", "Number of guesses loaded: ${documents.size()}")
                if (documents.isEmpty) {
                    Toast.makeText(context, "No guesses to show", Toast.LENGTH_SHORT).show()
                } else {
                    val guesses = documents.mapIndexed { index, doc ->
                        val user = doc.getString("user") ?: "Unknown"
                        val distance = doc.getDouble("distance") ?: 0.0
                        val pfpUrl = doc.getString("pfpUrl") ?: "Unknown"
                        val isCurrentUser = user == currentUsername
                        Guess(user, distance, pfpUrl, index + 1, isCurrentUser)
                    }
                    adapter.setGuesses(guesses)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PostLeaderboardFragment", "Error loading guesses", exception)
                Toast.makeText(context, "Error loading guesses: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val ARG_POST = "post"

        fun newInstance(post: Post): PostLeaderboardFragment {
            val fragment = PostLeaderboardFragment()
            val args = Bundle().apply {
                putParcelable(ARG_POST, post)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
