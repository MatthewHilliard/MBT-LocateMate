package com.example.mbt_locatemate

import android.media.Image
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.firestore.CollectionReference
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
    private lateinit var captionView: TextInputLayout

    private val db = FirebaseFirestore.getInstance()
    private var mediaPlayer: MediaPlayer? = null

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
        leaderboard = view.findViewById(R.id.leaderboardButton)

        //used ChatGPT to help with dialog
        delete.setOnClickListener {
            //asks for user confirmation to delete
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirm Deletion")
            builder.setMessage("Are you sure you want to delete this post?")

            builder.setPositiveButton("Yes") { dialog, _ ->
                deletePost()
                dialog.dismiss()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
        captionView.setEndIconOnClickListener {
            // respond to end icon presses to update the caption
            Log.d("PostActions", "attmepting to update caption")
            db.collection("posts").document(postId)
                .update("caption", caption.text.toString())
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                }
            //clear focus from the caption input to stop typing
            caption.clearFocus()
            captionView.clearFocus()
        }

        val commentsButton = view.findViewById<ImageView>(R.id.commentsButton)
        commentsButton.setOnClickListener{
            //send the post info to the comments fragment and open it
            val bundle = Bundle().apply {
                putParcelable("post", post)
            }
            val commentsFragment = CommentsFragment().apply {
                arguments = bundle
            }
            commentsFragment.show(parentFragmentManager, "CommentsFragment")
        }
        //allows user to navigate back to profile fragment by clicking the profile picture or the username
        pfpImage.setOnClickListener {
            goToProfile()
        }
        usernameTV.setOnClickListener {
            goToProfile()
        }
        //navigates to leaderboard and adds this fragment to back stack (for navigation back here)
        leaderboard.setOnClickListener {
            val leaderboardFragment = PostLeaderboardFragment.newInstance(post)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, leaderboardFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //stop music
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun goToProfile() {
        val profileFragment = ProfileFragment()
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, profileFragment).commit()
        (activity as MainActivity).bottomNavBar.selectedItemId =
            R.id.profileTab
    }

    private fun deletePost() {
        // delete post document
        val post = db.collection("posts").document(postId)
        post.delete()
            .addOnSuccessListener {
                Log.d("PostActions", "Successfully deleted post $postId")
                // delete guesses
                deleteCollection(post.collection("guesses"))
                // delete comments
                deleteCollection(post.collection("comments"))
                goToProfile()
            }
            .addOnFailureListener { e ->
                Log.d("PostActions", "Error deleting post $postId: $e")
            }
    }
    //deletes the collection within a post by using a batch commit
    private fun deleteCollection(collection: CollectionReference) {
        collection.get()
            .addOnSuccessListener { snapshot ->
                val batch = collection.firestore.batch()
                for (document in snapshot.documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnFailureListener { e ->
                        Log.d("Firestore", "Error deleting collection", e)
                    }
            }
            .addOnFailureListener { e ->

            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currPost: Post? = arguments?.getParcelable("post")
        if (currPost != null) {
            //set all the post info in the fragment
            postId = currPost.id
            Log.d("display", "post $postId")
            post = currPost
            usernameTV.text = post.username
            caption.setText(post.caption)
            Picasso.get().load(post.imgUrl).into(postImage)
            Picasso.get().load(post.pfpUrl).into(pfpImage)
            timeAgo.text = calculateTimeAgo(post.timestamp)

            //allow us to play song in individual post fragment
            db.collection("posts").document(post.id).get().addOnSuccessListener {document ->
                if (document.contains("song_url")) {
                    val songUrl = document.getString("song_url").toString()
                    if (songUrl != "") {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(songUrl)
                            prepareAsync()
                            setOnPreparedListener {
                                it.start()
                            }
                            setOnErrorListener { mp, what, extra ->
                                false
                            }
                        }
                    }
                }
            }
        }
    }

    //calculate how long ago the post was made using the timestamp
    //used ChatGPT to assist in time conversion
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