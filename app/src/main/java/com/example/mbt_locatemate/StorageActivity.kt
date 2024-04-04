package com.example.mbt_locatemate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

abstract class StorageActivity : AppCompatActivity() {

    // [START storage_field_declaration]
    lateinit var storage: FirebaseStorage
    // [END storage_field_declaration]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //might change later
        setContentView(R.layout.activity_main)

        // [START storage_field_initialization]
        storage = Firebase.storage
        // [END storage_field_initialization]
    }
}