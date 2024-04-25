package com.example.mbt_locatemate

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mbt_locatemate.ExploreFragment
import com.example.mbt_locatemate.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MapGuessFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private val boston = LatLng(42.0, -71.0)
    private lateinit var guess: LatLng

    private lateinit var post: Post

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        post = arguments?.getParcelable<Post>(ARG_POST)!!
        val postID = post.id
        Toast.makeText(requireContext(), "Post ID: $postID", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_mapguess, container, false)

        val backButton = view.findViewById<ImageView>(R.id.guessBackButton)
        backButton.setOnClickListener(){
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exploreFragment).commit()
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        auth = Firebase.auth
        storage = Firebase.storage

        val guessButton: Button = view.findViewById(R.id.guessButton)
        guessButton.setOnClickListener {
            if (::guess.isInitialized) {
                val distance = FloatArray(1)
                Location.distanceBetween(
                    boston.latitude,
                    boston.longitude,
                    guess.latitude,
                    guess.longitude,
                    distance
                )
                val distanceInMeters = distance[0]
                showDistanceToast(distanceInMeters)

                CoroutineScope(Dispatchers.IO).launch {
                    //val postID = post.id
                    val postRef = db.collection("posts").document(post.id.toString())

                    val newGuess = hashMapOf(
                        "user" to "username_here",
                        "distance" to "distance_value_here"
                    )

                    postRef.collection("guesses").add(newGuess).addOnSuccessListener { documentReference ->
                        // success
                    }.addOnFailureListener { e ->
                        // error
                    }
                }

                navigateToPostLeaderboardFragment(post)


            } else {
                Toast.makeText(context, "Please select a location first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDistanceToast(distance: Float) {
        val distanceInKm = distance / 1000
        Toast.makeText(context, "Distance to target: ${distanceInKm}km", Toast.LENGTH_LONG).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true

        // set click listener
        map.setOnMapClickListener { latLng ->
            guess = latLng
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title("Guess Location"))
        }
    }

    private fun navigateToPostLeaderboardFragment(post: Post) {
        val leaderboardFragment = PostLeaderboardFragment.newInstance(post.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, leaderboardFragment)
            .commit()
    }

    companion object {
        private const val ARG_POST = "post"

        // Change the method to accept a Post object
        fun newInstance(post: Post): MapGuessFragment {
            val fragment = MapGuessFragment()
            val args = Bundle().apply {
                putParcelable(ARG_POST, post)
            }
            fragment.arguments = args
            return fragment
        }
    }

}