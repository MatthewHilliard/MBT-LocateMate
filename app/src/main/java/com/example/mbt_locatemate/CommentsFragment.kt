package com.example.mbt_locatemate

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import java.util.UUID

class CommentsFragment : BottomSheetDialogFragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: CommentListAdapter
    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var newCommentText: EditText
    private lateinit var newCommentPfp: ImageView
    private lateinit var newCommentUsername: TextView

    private lateinit var postId: String
    private lateinit var username: String
    private lateinit var pfpUrl: String

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comments_sheet, container, false)
        newCommentText = view.findViewById(R.id.comment_text)
        newCommentPfp = view.findViewById(R.id.pfp_comment)
        newCommentUsername = view.findViewById(R.id.comment_user)
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

        newCommentText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                // needs to check if comments exist yet and if not create a collection
                // set comment text, pfpUrl, username for a new comment document
                db.collection("posts").document(postId)
                    .update("caption", newCommentText.text.toString())
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { e ->
                    }
            }
        })

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.standard_bottom_sheet) as LinearLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.peekHeight = bottomSheet.height
        }
        val currPost: Post? = arguments?.getParcelable("post")
        if (currPost != null) {
            postId = currPost.id
            username = currPost.username
            pfpUrl = currPost.pfpUrl
            newCommentUsername.text = username
            Picasso.get().load(pfpUrl).into(newCommentPfp)
            loadComments()
        }
    }

    private fun loadComments() {
        db.collection("posts").document(postId).collection("comments")
            .get()
            .addOnSuccessListener { documents ->
                val commentList = mutableListOf<Comment>()
                for (document in documents) {
                    val commentText = document.getString("text") ?: ""
                    val username = document.getString("username") ?: ""
                    val pfpUrl = document.getString("pfp_url") ?: ""
                    val timeAgo = document.getLong("timestamp") ?: 0
                    val comment = Comment(username, pfpUrl, commentText, timeAgo)
                    commentList.add(comment)
                }
                Log.d("CommentsList", commentList.toString())
                adapter.updateComments(commentList)
            }
    }
}