package com.example.mbt_locatemate

import android.graphics.Color
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapGuessFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    //private val boston = LatLng(42.0, -71.0)
    private lateinit var postLocation: LatLng
    private lateinit var guess: LatLng

    private lateinit var post: Post

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val db = Firebase.firestore

    private var guessMade = false

    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        post = arguments?.getParcelable<Post>(ARG_POST)!!
        val postID = post.id
        postLocation = post.location
        //Toast.makeText(requireContext(), "Post ID: $postID", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_mapguess, container, false)

        val backButton = view.findViewById<ImageView>(R.id.guessBackButton)
        backButton.setOnClickListener(){
            mediaPlayer?.release()
            mediaPlayer = null
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exploreFragment).commit()

        }

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
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        auth = Firebase.auth
        storage = Firebase.storage
        val currentUserId = auth.currentUser?.uid

        val guessButton: Button = view.findViewById(R.id.guessButton)
        guessButton.setOnClickListener {
            if (guessMade) {
                navigateToPostLeaderboardFragment(post)
            }
            else if (::guess.isInitialized){
                val distance = FloatArray(1)
                Location.distanceBetween(
                    postLocation.latitude,
                    postLocation.longitude,
                    guess.latitude,
                    guess.longitude,
                    distance
                )
                val distanceInMeters = distance[0]
                showDistanceToast(distanceInMeters)

                if (currentUserId != null) {
                    // fetching user details
                    db.collection("users").document(currentUserId).get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val username = documentSnapshot.getString("username")
                                val pfpUrl = documentSnapshot.getString("pfp_url")
                                val postId = post.id.toString()  // Ensure you have the postId from your post object
                                Log.d("MapGuessFragment", "Loading image from URL: $pfpUrl")

                                if (username != null) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val userGuess = hashMapOf(
                                            "postId" to postId,
                                            "distance" to distanceInMeters
                                        )
                                        val postGuess = hashMapOf(
                                            "user" to username,
                                            "distance" to distanceInMeters,
                                            "pfpUrl" to pfpUrl
                                        )

                                        // add guess to guesses subcollection under the post document
                                        val postRef = db.collection("posts").document(postId)
                                        postRef.collection("guesses").add(postGuess).addOnSuccessListener {
                                            // success, handle UI update or navigation
                                        }.addOnFailureListener { e ->
                                            Log.w("TAG", "Error adding document to post guesses", e)
                                        }

                                        // add guess to guesses subcollection under the user document
                                        val userRef = db.collection("users").document(currentUserId)
                                        userRef.collection("guesses").add(userGuess).addOnSuccessListener {
                                            guessMade = true
                                            drawPolyline()
                                            guessButton.text = "See the Leaderboard"
                                            Log.d("MapGuessFragment", "Guess added to user's collection")
                                        }.addOnFailureListener { e ->
                                            Log.w("TAG", "Error adding document to user guesses", e)
                                        }
                                    }
                                } else {
                                    Log.w("TAG", "null username")
                                }
                            } else {
                                Log.w("TAG", "no user?")
                            }
                        }.addOnFailureListener { exception ->
                            Log.w("TAG", "error fetching user document", exception)
                        }
                }
            } else {
                Toast.makeText(context, "Please select a location first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

/*
    private fun drawPolyline() {
        map.addPolyline(
            PolylineOptions()
                .add(postLocation)
                .add(guess)
                .width(5f)
                .color(Color.RED)
        )
    }
 */

    private fun drawPolyline() {
        map.addMarker(MarkerOptions()
            .position(postLocation)
            .title("Real Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

        map.addMarker(MarkerOptions()
            .position(guess)
            .title("Guess Location"))

        val polyline = map.addPolyline(
            PolylineOptions()
                .add(postLocation)
                .add(guess)
                .width(5f)
                .color(Color.RED)
        )

        val builder = LatLngBounds.Builder()
        builder.include(postLocation)
        builder.include(guess)
        val bounds = builder.build()

        // use dynamic padding function
        val padding = calculateDynamicPadding()

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                disableMapClickListener()
                disableMapInteractions()
            }

            override fun onCancel() {
                disableMapClickListener()
                disableMapInteractions()
            }
        })
    }

    private fun disableMapClickListener() {
        map.setOnMapClickListener(null)
    }

    private fun disableMapInteractions() {
        map.uiSettings.apply {
            isScrollGesturesEnabled = false
            isZoomGesturesEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
            isZoomControlsEnabled = false
        }
    }

    private fun calculateDynamicPadding(): Int {
        val distance = FloatArray(1)
        Location.distanceBetween(postLocation.latitude, postLocation.longitude, guess.latitude, guess.longitude, distance)
        val kmDistance = distance[0] / 1000

        // increase padding as the distance increases
        return (50 + kmDistance * 10).toInt().coerceAtLeast(100).coerceAtMost(400)  // Ensures a minimum of 100px and a maximum of 400px padding
    }

    private fun adjustCameraZoom(distance: Float) {
        val zoomLevel = when {
            distance < 200 -> 16f  // zoom in closer for very close guesses
            distance < 1000 -> 14f  // slightly zoomed out
            distance < 5000 -> 12f  // moderate zoom out
            else -> 10f  // zoom out for distant guesses
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(guess, zoomLevel))
    }

    private fun adjustCameraBounds(postLocation: LatLng, guess: LatLng) {
        val builder = LatLngBounds.Builder()
        builder.include(postLocation)
        builder.include(guess)
        val bounds = builder.build()

        // Calculate padding based on distance
        val distance = FloatArray(1)
        Location.distanceBetween(postLocation.latitude, postLocation.longitude, guess.latitude, guess.longitude, distance)
        val padding = (distance[0] / 1000).toInt() * 50  // Increase padding as distance increases

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
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
        mediaPlayer?.release()
        mediaPlayer = null
        val leaderboardFragment = PostLeaderboardFragment.newInstance(post)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, leaderboardFragment)
            .commit()
    }

    companion object {
        const val ARG_POST = "post"

        // changing the method to accept a Post object
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