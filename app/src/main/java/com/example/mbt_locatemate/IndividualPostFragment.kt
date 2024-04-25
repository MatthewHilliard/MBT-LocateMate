package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso

class IndividualPostFragment: Fragment() {
    private lateinit var post: Post
    private lateinit var usernameTV: TextView
    private lateinit var caption: TextView
    private lateinit var postImage: ImageView
    private lateinit var pfpImage: ImageView

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currPost: Post? = arguments?.getParcelable("post")
        if (currPost != null) {
            post = currPost
            usernameTV.text = post.username
            caption.text = post.caption
            Picasso.get().load(post.imgUrl).into(postImage)
            Picasso.get().load(post.pfpUrl).into(pfpImage)
        }
    }
}