package com.example.mbt_locatemate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.media.ExifInterface
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
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.CoroutineScope
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

    //if location permissions are requested, we can perform actions based on the result of the permission request
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            //set up the map now that we have access to our location
            Log.d("LocationPermissions", "permission granted, setting up map")
            setUpMap()
        } else {
            //if location permissions are denied, we navigate back to explore since we cannot make a post
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
            (activity as MainActivity).bottomNavBar.selectedItemId =
                R.id.exploreTab        }
    }

    //result of photo-taking action
    val resultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            //getting the orientation and setting the image to the bitmap of the camera result
            val exif = ExifInterface(output!!.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            //get the file using the uri generated when photo was taken
            val bitmap = BitmapFactory.decodeFile(output!!.absolutePath)
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            val capturedImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            image.setImageBitmap(capturedImage)
            imageBitmap = capturedImage
        }
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
        //firebase instances
        auth = Firebase.auth
        storage = Firebase.storage
        lastLocation = Location("")
        lastLocation.latitude = 0.0
        lastLocation.longitude = 0.0

        //open camera to take a photo
        image = view.findViewById(R.id.imageView)
        caption = view.findViewById(R.id.captionText)
        val takePic = view.findViewById<ImageView>(R.id.cameraButton)
        takePic.setOnClickListener {
            //take a photo, save the image to external file storage temporarily and set a uri for later use
            GlobalScope.launch(Dispatchers.IO) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val dir = context?.getExternalFilesDir(Environment.DIRECTORY_DCIM)
                imageName = UUID.randomUUID().toString() + ".jpeg"
                val currOutput = File(dir, imageName)
                output = currOutput
                //set a uri for the photo
                val uri = context?.let { it1 ->
                    FileProvider.getUriForFile(
                        it1,
                        "com.example.mbt_locatemate.fileprovider",
                        currOutput
                    )
                }
                //store the photo externally
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                resultContract.launch(intent)
            }
        }
        val cancel = view.findViewById<ImageView>(R.id.cancelButton)
        cancel.setOnClickListener {
            //canceling the post makes us go back to explore
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
            (activity as MainActivity).bottomNavBar.selectedItemId =
                R.id.exploreTab
        }
        val post = view.findViewById<ImageView>(R.id.postButton)
        post.setOnClickListener {
            //save the post to the database and navigate back to explore
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    savePost()
                } catch (e: Exception) {
                }
            }
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
            (activity as MainActivity).bottomNavBar.selectedItemId =
                R.id.exploreTab
        }
        //fused location provider client is for assisting with current location on the map
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        friendsOnlyButton = view.findViewById(R.id.friendsOnlyButton)
        publicButton = view.findViewById(R.id.publicButton)
        segmentedButton = view.findViewById(R.id.postTypeButton)
        //based on user selection, we can make the post public or private (friends only or able to be seen by all users of the app)
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
        //option to add a song or other background music to the post
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
            //store the photo first in firebase cloud storage
            storage.reference.child("images/${currentUser.uid}/posts/$imageId.jpg").putBytes(data)
                .addOnSuccessListener { task->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { url ->
                            //once stored, we have the url and can store the other post info in firestore
                            val imageUrl = url.toString()
                            val captionText = caption.text.toString()
                            val latitude = lastLocation.latitude
                            val longitude = lastLocation.longitude
                            db.collection("users")
                                .document(currentUser.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val postId = UUID.randomUUID().toString()
                                        val username = document.getString("username") ?: ""
                                        val pfpUrl = document.getString("pfp_url") ?: ""
                                        val timestamp = System.currentTimeMillis()
                                        val postInfo = hashMapOf(
                                            "id" to postId,
                                            "username" to username,
                                            "caption" to captionText,
                                            "pfp_url" to pfpUrl,
                                            "img_url" to imageUrl,
                                            "latitude" to latitude,
                                            "longitude" to longitude,
                                            "timestamp" to timestamp,
                                            "public" to isPublicPost,
                                            "song_url" to songUrl
                                        )
                                        db.collection("posts").document(postId)
                                            .set(postInfo)
                                            .addOnSuccessListener { documentReference ->
                                            }
                                            .addOnFailureListener { e ->
                                            }
                                    }
                                }.addOnFailureListener { exception ->
                                            println("Error getting document: $exception")
                                }
                        }
                }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        nMap.uiSettings.isZoomControlsEnabled = true
        //check for location permissions and set up map if they are met
        if (hasLocationPermission()) {
            setUpMap()
        } else {
            //request the permission and wait for the result
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    //checking for permissions
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpMap() {
        //make sure permissions are met before trying to get the last location
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        nMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            //once last location is fetched, set our lateinit variable and place a marker
            if (location != null) {
                lastLocation = location
                //place a marker at our location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarker(currentLatLong)
            } else {
                //do nothing
            }
        }
    }

    private fun placeMarker(latLong: LatLng) {
        val markerOptions = MarkerOptions().position(LatLng(latLong.latitude, latLong.longitude))
        markerOptions.title("$latLong")
        nMap.addMarker(markerOptions)
        //pan camera to the marker
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 12f))
    }

    override fun onSongSelected(audioUrl: String) {
        songUrl = audioUrl
        Log.d("CreatePostFragment", "Song has been accepted, $songUrl")
    }

    //make the marker non-clickable
    override fun onMarkerClick(p0: Marker): Boolean = false

}