package com.example.mbt_locatemate

import java.util.UUID
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Friend (
    val id:String,
    val username:String,
    val pfpUrl:String,
) :Parcelable