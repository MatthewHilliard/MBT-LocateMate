package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CreatePostFragment: Fragment() {
    private lateinit var mapView: MapView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_create, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        //mapView.getMapAsync(this)
        return view
    }
//    fun onMapReady(googleMap: GoogleMap) {
//        // Add a marker on your location
//        val myLocation = LatLng(yourLatitude, yourLongitude)
//        googleMap.addMarker(MarkerOptions().position(myLocation).title("Your Location"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
//    }
}