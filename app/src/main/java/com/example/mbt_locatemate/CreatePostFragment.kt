package com.example.mbt_locatemate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

class CreatePostFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var nMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var image: ImageView
    var imageTaken = false
    private lateinit var caption: EditText
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val resultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as Bitmap
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
        }
        val post = view.findViewById<MaterialButton>(R.id.postButton)
        post.setOnClickListener {
            savePost()
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        mapFragment?.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return view
    }

    private fun savePost() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        val captionText = caption.text.toString()
        val imageUrl = "https://firebasestorage.googleapis.com/v0/b/locatemate-cf72b.appspot.com/o/matthew_hilliard_test.jpg?alt=media&token=230d74c2-fba6-41f4-a147-2175029f6e59"
        val profilePictureUrl = "https://firebasestorage.googleapis.com/v0/b/locatemate-cf72b.appspot.com/o/images%2Fmattpfp.PNG?alt=media&token=31c60078-ec95-4277-8b5a-8046d6cd2341"
        var username = ""
        if (currentUser != null) {
            username = currentUser.displayName.toString()
        }
        val location = LatLng(lastLocation.latitude, lastLocation.longitude)

        val post = Post(id= UUID.randomUUID(), username = username, caption = captionText, imgUrl = imageUrl, pfpUrl = profilePictureUrl, location = location)
        // Upload post to Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                // Post uploaded successfully
            }
            .addOnFailureListener { e ->
                // Handle failure
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