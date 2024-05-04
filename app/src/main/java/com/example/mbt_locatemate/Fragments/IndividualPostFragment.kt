package com.example.mbt_locatemate

import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.LatLng
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

class IndividualPostFragment: Fragment() {
    private lateinit var post: Post
    private lateinit var usernameTV: TextView
    private lateinit var caption: EditText
    private lateinit var postImage: ImageView
    private lateinit var pfpImage: ImageView
    private lateinit var delete: ImageView
    private lateinit var postId: String
    private lateinit var timeAgo: TextView
    private lateinit var leaderboard: ImageView
//    private lateinit var sendCaption: MaterialButton
    private lateinit var captionView: TextInputLayout

    var onCommentsClickListener: ((Post) -> Unit)? = null
    private val db = FirebaseFirestore.getInstance()

//<EditText
//        android:id="@+id/post_caption"
//        android:layout_width="276dp"
//        android:layout_height="44dp"
//        android:layout_marginStart="10dp"
//        android:layout_marginTop="5dp"
//        android:fontFamily="@font/poppins"
//        android:text=""
//        android:textColor="@color/md_theme_onSurface"
//        android:textSize="18dp"
//        android:textStyle="bold"
//        app:layout_constraintStart_toStartOf="@id/post_image"
//        app:layout_constraintTop_toBottomOf="@+id/leaderboardButton" />
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_individual_post, container, false)
        usernameTV = view.findViewById(R.id.post_user)
        captionView = view.findViewById(R.id.captionField)
        caption = view.findViewById(R.id.post_caption)
        postImage = view.findViewById(R.id.post_image)
        pfpImage = view.findViewById(R.id.post_pfp)
        delete = view.findViewById(R.id.delete_button)
        timeAgo = view.findViewById(R.id.time_ago)
//        sendCaption = view.findViewById(R.id.send_caption)
        leaderboard = view.findViewById(R.id.leaderboardButton)


        delete.setOnClickListener {
            deletePost()
        }
        captionView.setEndIconOnClickListener {
            // Respond to end icon presses
            Log.d("PostActions", "attmepting to update caption")
            db.collection("posts").document(postId)
                .update("caption", caption.text.toString())
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                }
            caption.clearFocus()
        }
//        caption.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                //do nothing
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                //do nothing
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                // update caption in database
//                if (s.toString() != "") {
//                    sendCaption.visibility = View.VISIBLE
//                } else {
//
//                }
//            }
//        })

//        sendCaption.setOnClickListener{
//
//
//        }

        val commentsButton = view.findViewById<ImageView>(R.id.commentsButton)
        commentsButton.setOnClickListener{
            val bundle = Bundle().apply {
                putParcelable("post", post)
            }
            val commentsFragment = CommentsFragment().apply {
                arguments = bundle
            }
            commentsFragment.show(parentFragmentManager, "CommentsFragment")
        }

        pfpImage.setOnClickListener {
            goToProfile()
        }
        usernameTV.setOnClickListener {
            goToProfile()
        }
        leaderboard.setOnClickListener {
            val leaderboardFragment = PostLeaderboardFragment.newInstance(post)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, leaderboardFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun goToProfile() {
        val profileFragment = ProfileFragment()
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, profileFragment).commit()
        (activity as MainActivity).bottomNavBar.selectedItemId =
            R.id.profileTab
    }

    private fun deletePost() {
        Log.d("PostActions", "attmetping to delete post $postId")
        //need id to delete?
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.d("PostActions", "successfully deleted post $postId")
            }
            .addOnFailureListener { e ->
                Log.d("PostActions", "Error deleting post $postId")
            }
        goToProfile()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currPost: Post? = arguments?.getParcelable("post")
        if (currPost != null) {
            val location = com.google.android.gms.maps.model.LatLng(currPost.latitude, currPost.longitude)
            Log.d("Location", location.toString())
            postId = currPost.id
            Log.d("display", "post $postId")
            post = currPost
            usernameTV.text = post.username
            caption.setText(post.caption)
            Picasso.get().load(post.imgUrl).into(postImage)
            Picasso.get().load(post.pfpUrl).into(pfpImage)
            timeAgo.text = calculateTimeAgo(post.timestamp)
        }
    }

    private fun calculateTimeAgo(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - timestamp

        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
        val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
        val weeks = days / 7
        val years = weeks / 52
        if (days.toInt() == 1 || seconds.toInt() == 1 || minutes.toInt() == 1 || hours.toInt() == 1) {
            return when {
                years > 0 -> "$years year ago"
                weeks > 0 -> "$weeks week ago"
                days > 0 -> "$days day ago"
                hours > 0 -> "$hours hour ago"
                minutes > 0 -> "$minutes minute ago"
                else -> "$seconds second ago"
            }
        }
        return when {
            years > 0 -> "$years years ago"
            weeks > 0 -> "$weeks weeks ago"
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "$seconds seconds ago"
        }
    }
}