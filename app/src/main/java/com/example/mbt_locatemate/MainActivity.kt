package com.example.mbt_locatemate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var bottomNavBar: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        bottomNavBar = findViewById(R.id.nav_bar)

        bottomNavBar.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.exploreTab->{
                    val exploreFragment = ExploreFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
                    true
                }
                R.id.postTab->{
                    val postFragment = CameraFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, postFragment).commit()
                    true
                }
                R.id.profileTab->{
                    val profileFragment = ProfileFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, profileFragment).commit()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) run {
            val loginFragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loginFragment).commit()
        }
    }

    fun showBottomNavBar(show: Boolean) {
        if (show) {
            bottomNavBar.visibility = View.VISIBLE
        } else {
            bottomNavBar.visibility = View.GONE
        }
    }
}