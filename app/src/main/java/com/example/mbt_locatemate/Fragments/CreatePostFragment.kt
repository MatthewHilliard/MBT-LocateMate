package com.example.mbt_locatemate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID


class CreatePostFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, SongsFragment.SongSelectionListener {

    private lateinit var nMap: GoogleMap
    private val CONTENT_REQUEST = 1337
    private var output: File? = null
    private lateinit var lastLocation: Location
    private lateinit var image: ImageView
    private lateinit var caption: EditText
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var storage: FirebaseStorage
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var imageBitmap: Bitmap
    private lateinit var segmentedButton: MaterialButtonToggleGroup
    private lateinit var friendsOnlyButton: Button
    private lateinit var publicButton: Button
    private lateinit var imageName: String
    private var isPublicPost = false
    private lateinit var addSongButton: Button
    private var songUrl = ""
//    val locationRequest = LocationRequest.create().apply {
//        priority = Priority.PRIORITY_HIGH_ACCURACY
//        interval = 10000 // 10 seconds
//        fastestInterval = 5000 // 5 seconds
//    }
//    val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            locationResult ?: return
//            for (location in locationResult.locations) {
//                // Handle received location
//                val currentLatLong = LatLng(location.latitude, location.longitude)
//                placeMarker(currentLatLong)
//            }
//        }
//    }

    val resultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val capturedImage = BitmapFactory.decodeFile(output!!.absolutePath)
            image.setImageBitmap(capturedImage)
//            val bitmap = result.data?.extras?.get("data") as Bitmap
            imageBitmap = capturedImage
//            image.setImageBitmap(bitmap)
        }
    }
    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_create, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        mapFragment?.onCreate(savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage
        //open camera to take a photo
        image = view.findViewById(R.id.imageView)
        caption = view.findViewById(R.id.captionText)
        val takePic = view.findViewById<ImageView>(R.id.cameraButton)
        takePic.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val dir = context?.getExternalFilesDir(Environment.DIRECTORY_DCIM)
                imageName = UUID.randomUUID().toString() + ".jpeg"
                val currOutput = File(dir, imageName)
                output = currOutput
                val uri = context?.let { it1 ->
                    FileProvider.getUriForFile(
                        it1,
                        "com.example.mbt_locatemate.fileprovider",
                        currOutput
                    )
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                resultContract.launch(intent)
            }
        }
        val cancel = view.findViewById<ImageView>(R.id.cancelButton)
        cancel.setOnClickListener {
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
            (activity as MainActivity).bottomNavBar.selectedItemId =
                R.id.exploreTab
        }
        val post = view.findViewById<ImageView>(R.id.postButton)
        post.setOnClickListener {
            savePost()
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
            (activity as MainActivity).bottomNavBar.selectedItemId =
                R.id.exploreTab
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        friendsOnlyButton = view.findViewById(R.id.friendsOnlyButton)
        publicButton = view.findViewById(R.id.publicButton)
        segmentedButton = view.findViewById(R.id.postTypeButton)
        segmentedButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.friendsOnlyButton -> {
                        isPublicPost = false
                        friendsOnlyButton.setBackgroundColor(resources.getColor(R.color.md_theme_secondaryContainer))
                        publicButton.setBackgroundColor(resources.getColor(R.color.md_theme_surface))
                    }
                    R.id.publicButton -> {
                        isPublicPost = true
                        publicButton.setBackgroundColor(resources.getColor(R.color.md_theme_secondaryContainer))
                        friendsOnlyButton.setBackgroundColor(resources.getColor(R.color.md_theme_surface))
                    }
                }
            }
        }

        addSongButton = view.findViewById(R.id.post_add_song)
        addSongButton.setOnClickListener{
            val songsFragment = SongsFragment()
            songsFragment.setSongSelectionListener(this)
            songsFragment.show(parentFragmentManager, "SongsFragment")
        }

        segmentedButton.check(R.id.friendsOnlyButton)
        return view
    }

    private fun savePost() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val imageId = UUID.randomUUID().toString()

            storage.reference.child("images/${currentUser.uid}/posts/$imageId.jpg").putBytes(data)
                .addOnSuccessListener { task->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { url ->
                            val imageUrl = url.toString()
                            val captionText = caption.text.toString()
                            val location = LatLng(lastLocation.latitude, lastLocation.longitude)
                            //get username and stuff
                            db.collection("users")
                                .document(currentUser.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val postId = UUID.randomUUID().toString()
                                        val username = document.getString("username") ?: ""
                                        val pfpUrl = document.getString("pfp_url") ?: ""
                                        val emptyList: MutableList<Any> = mutableListOf()
                                        val timestamp = System.currentTimeMillis()
                                        val postInfo = hashMapOf(
                                            "id" to postId,
                                            "username" to username,
                                            "caption" to captionText,
                                            "pfp_url" to pfpUrl,
                                            "img_url" to imageUrl,
                                            "location" to location,
                                            "timestamp" to timestamp,
                                            "public" to isPublicPost,
                                            "song_url" to songUrl
                                        )
                                        db.collection("posts").document(postId)
                                            .set(postInfo)
                                            .addOnSuccessListener { documentReference ->
                                                // Post uploaded successfully
                                            }
                                            .addOnFailureListener { e ->
                                                // Handle failure
                                            }
                                    }
                                }.addOnFailureListener { exception ->
                                            // Handle any errors that may occur
                                            println("Error getting document: $exception")
                                }
                        }
                }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        nMap.uiSettings.isZoomControlsEnabled = true
        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        nMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarker(currentLatLong)
            } else {
                //fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
    }

    private fun placeMarker(latLong: LatLng) {
        val markerOptions = MarkerOptions().position(LatLng(latLong.latitude, latLong.longitude))
        markerOptions.title("$latLong")
        nMap.addMarker(markerOptions)
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 12f))
    }

    override fun onSongSelected(audioUrl: String) {
        songUrl = audioUrl
        Log.d("CreatePostFragment", "Song has been accepted, $songUrl")
    }

    override fun onMarkerClick(p0: Marker): Boolean = false

}