package com.example.mbt_locatemate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup

class ExploreFragment: Fragment() {
    private lateinit var segmentedButton: MaterialButtonToggleGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        segmentedButton = view.findViewById(R.id.segmentedButton)

        segmentedButton.addOnButtonCheckedListener { segmentedButton, checkedId, isChecked ->
            when (checkedId) {
                R.id.friendsButton -> {

                }
                R.id.exploreButton -> {

                }
            }
        }

        return view
    }
}