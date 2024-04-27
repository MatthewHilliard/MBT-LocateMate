package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.UUID

class CommentsFragment : BottomSheetDialogFragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: CommentListAdapter
    private lateinit var commentRecyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comments_sheet, container, false)
        commentRecyclerView = view.findViewById(R.id.comment_recycler_view)
        commentRecyclerView.addItemDecoration(
            DividerItemDecoration(
                commentRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        layoutManager = LinearLayoutManager(requireContext())
        commentRecyclerView.layoutManager = layoutManager

        auth = Firebase.auth

        adapter = CommentListAdapter(mutableListOf())
        commentRecyclerView.adapter = adapter

        loadComments()
        return view
    }

    private fun loadComments() {
        db.collection("posts").document("2e8d3529-6f8a-466f-951e-0b5f78802ca8").collection("comments")
            .get()
            .addOnSuccessListener { documents ->
                val commentList = mutableListOf<Comment>()
                for (document in documents) {
                    val commentText = document.getString("text") ?: ""
                    val username = document.getString("username") ?: ""
                    val pfpUrl = document.getString("pfp_url") ?: ""
                    val comment = Comment(username, pfpUrl, commentText)
                    commentList.add(comment)
                }
                Log.d("CommentsList", commentList.toString())
                adapter.updateComments(commentList)
            }
    }
}