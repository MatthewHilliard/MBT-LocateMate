package com.example.mbt_locatemate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavBar: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavBar = findViewById(R.id.nav_bar)

        bottomNavBar.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.exploreTab->{
                    val exploreFragment = ExploreFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, exploreFragment).commit()
                    true
                }
                R.id.postTab->{
                    val postFragment = PostFragment()
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
}