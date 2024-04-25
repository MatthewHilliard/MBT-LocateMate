package com.example.mbt_locatemate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
import com.google.android.material.button.MaterialButton
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
import java.util.UUID
import java.util.*


class CreatePostFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var nMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var image: ImageView
    var imageTaken = false
    private lateinit var caption: EditText
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var storage: FirebaseStorage
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var imageBitmap: Bitmap
    private lateinit var segmentedButton: MaterialButtonToggleGroup
    private var isPublicPost = false

    val resultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as Bitmap
            imageBitmap = bitmap
            image.setImageBitmap(bitmap)
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
        auth = Firebase.auth
        storage = Firebase.storage
        //open camera to take a photo
        image = view.findViewById(R.id.imageView)
        caption = view.findViewById(R.id.captionText)
        if (!imageTaken) {
            GlobalScope.launch(Dispatchers.IO) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                resultContract.launch(intent)
                imageTaken = true
            }
        }
        val cancel = view.findViewById<MaterialButton>(R.id.cancelButton)
        cancel.setOnClickListener {
            imageTaken = false
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
            (activity as MainActivity).bottomNavBar.selectedItemId =
                R.id.exploreTab
        }
        val post = view.findViewById<MaterialButton>(R.id.postButton)
        post.setOnClickListener {
            savePost()
            val exploreFragment = ExploreFragment()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
            (activity as MainActivity).bottomNavBar.selectedItemId =
                R.id.exploreTab
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        mapFragment?.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        segmentedButton = view.findViewById(R.id.postTypeButton)
        segmentedButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.friendsOnlyButton -> {
                        isPublicPost = false
                    }
                    R.id.publicButton -> {
                        isPublicPost = true
                    }
                }
            }
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
                                        val postId = UUID.randomUUID()
                                        val username = document.getString("username") ?: ""
                                        val pfpUrl = document.getString("pfp_url") ?: ""
                                        val postInfo = hashMapOf(
                                            "id" to postId,
                                            "username" to username,
                                            "caption" to captionText,
                                            "pfp_url" to pfpUrl,
                                            "img_url" to imageUrl,
                                            "location" to location,
                                            "public" to isPublicPost
                                        )
                                        db.collection("posts").document(postId.toString())
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
            }
        }
    }

    private fun placeMarker(latLong: LatLng) {
        val markerOptions = MarkerOptions().position(LatLng(latLong.latitude, latLong.longitude))
        markerOptions.title("$latLong")
        nMap.addMarker(markerOptions)
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 12f))
    }

    override fun onMarkerClick(p0: Marker): Boolean = false

}