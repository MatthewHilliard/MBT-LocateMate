package com.example.mbt_locatemate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mbt_locatemate.databinding.FragmentProfileBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import java.util.UUID

class ProfileFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: ProfilePostListAdapter
    private lateinit var profilePostRecyclerView: RecyclerView

    private var username: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = Firebase.auth
        val user = auth.currentUser

        profilePostRecyclerView = view.findViewById(R.id.userPostsRecyclerView)

        layoutManager = GridLayoutManager(requireContext(), 2)
        profilePostRecyclerView.layoutManager = layoutManager

        adapter = ProfilePostListAdapter(emptyList()){ post ->
            val bundle = Bundle().apply {
                putParcelable("post", post)
            }
            val individualPostFragment = IndividualPostFragment().apply {
                arguments = bundle
            }
            Log.d("OnClick", "Beginning fragment replacement")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, individualPostFragment)
                .addToBackStack(null)
                .commit()
        }
        profilePostRecyclerView.adapter = adapter

        val usernameText = view.findViewById<TextView>(R.id.txtUsername)
        val pfpImage = view.findViewById<ImageView>(R.id.imgProfile)
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                username = document.getString("username")
                val pfpUrl = document.getString("pfp_url")
                usernameText.text = username
                Picasso.get().load(pfpUrl).into(pfpImage)
                loadUserPosts()
            }
        }

        val settingsButton = view.findViewById<ImageView>(R.id.settingsButton)

        settingsButton.setOnClickListener(){
            val settingsFragment = SettingsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment).commit()
        }

        return view
    }

    private fun loadUserPosts() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("posts")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { posts ->
                    val postList = mutableListOf<Post>()
                    for (document in posts) {
                        Log.d("ProfileFragment", "Post found")
                        val username = document.getString("username") ?: ""
                        val caption = document.getString("caption") ?: ""
                        val imgUrl = document.getString("img_url") ?: ""
                        val pfpUrl = document.getString("pfp_url") ?: ""
                        val post = Post(UUID.randomUUID(), username, caption, imgUrl, pfpUrl, LatLng(40.712775, -74.0059717))
                        postList.add(post)
                    }
                    adapter.updatePosts(postList)
                }
        }
    }
}