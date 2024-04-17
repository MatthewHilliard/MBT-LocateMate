package com.example.mbt_locatemate

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
import java.util.UUID

class MapGuessFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_mapguess, container, false)

        val backButton = view?.findViewById<Button>(R.id.backButton)
        backButton?.setOnClickListener(){
            Toast.makeText(context, "Back button pressed!", Toast.LENGTH_SHORT).show()
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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // example
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(sydney))

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true

        // set click listener
        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title("Guess Location"))
        }
    }


    companion object {
        fun newInstance(id: UUID): MapGuessFragment {
            return MapGuessFragment()
        }

        fun newInstanceWithArgs(someId: String): MapGuessFragment {
            val bundle = Bundle()
            bundle.putString("some_key", someId)
            val fragment = MapGuessFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}