package com.example.mbt_locatemate

import android.location.Location
import android.media.Image
import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class Post (
    val id: UUID,
    val username:String,
    val caption:String,
    val imgUrl:String,
    val pfpUrl:String,
    val location: LatLng
)