package com.example.mbt_locatemate

import android.location.Location
import android.media.Image

import com.google.android.gms.maps.model.LatLng
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Post (
    val id: String,
    val username:String,
    val caption:String,
    val imgUrl:String,
    val pfpUrl:String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
) : Parcelable

