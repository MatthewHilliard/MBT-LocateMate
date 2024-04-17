package com.example.mbt_locatemate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreatePostFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var nMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var lastLocation: Location
    private lateinit var image: ImageView
    var imageTaken = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val resultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result?.data?.extras?.get("data") as Bitmap
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
        if (!imageTaken) {
            GlobalScope.launch(Dispatchers.IO) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                resultContract.launch(intent)
                imageTaken = true
            }
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
       // mapView.onCreate(savedInstanceState)
        //mapView.getMapAsync(this)
        val markerOptions = MarkerOptions().position(LatLng(40.7128, 74.0060))
        markerOptions.title("New York")
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return view
    }
    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        nMap.uiSettings.isZoomControlsEnabled = true
        nMap.setOnMarkerClickListener(this)

        //setUpMap()
    }

//    private fun setUpMap() {
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
//            return
//        }
//        nMap.isMyLocationEnabled = true
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//            if (location != null) {
//                lastLocation = location
//                val currentLatLong = LatLng(location.latitude, location.longitude)
//                placeMarker(currentLatLong)
//            }
//        }
//    }

//    private fun placeMarker(latLong: LatLng) {
//        val markerOptions = MarkerOptions().position(LatLng(40.7128, 74.0060))
//        markerOptions.title("$latLong")
//        nMap.addMarker(markerOptions)
//    }

    override fun onMarkerClick(p0: Marker): Boolean = false

}