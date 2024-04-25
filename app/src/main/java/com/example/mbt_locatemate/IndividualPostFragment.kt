package com.example.mbt_locatemate

import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class IndividualPostFragment: Fragment() {
    private lateinit var post: Post
    private lateinit var usernameTV: TextView
    private lateinit var caption: EditText
    private lateinit var postImage: ImageView
    private lateinit var pfpImage: ImageView
    private lateinit var delete: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_individual_post, container, false)
        usernameTV = view.findViewById(R.id.post_user)
        caption = view.findViewById(R.id.post_caption)
        postImage = view.findViewById(R.id.post_image)
        pfpImage = view.findViewById(R.id.post_pfp)
        delete = view.findViewById(R.id.delete_button)

        delete.setOnClickListener {
            deletePost()
        }
        caption.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // update caption in database... need id?

            }

            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }
        })

        return view
    }

    private fun deletePost() {
        val db = FirebaseFirestore.getInstance()
        //need id to delete?
        db.collection("posts").document("id")
            .delete()
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                println("Error deleting document: $e")
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currPost: Post? = arguments?.getParcelable("post")
        if (currPost != null) {
            post = currPost
            usernameTV.text = post.username
            caption.setText(post.caption)
            Picasso.get().load(post.imgUrl).into(postImage)
            Picasso.get().load(post.pfpUrl).into(pfpImage)
        }
    }
}