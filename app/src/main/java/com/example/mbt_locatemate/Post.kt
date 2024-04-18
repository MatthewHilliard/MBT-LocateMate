package com.example.mbt_locatemate

import android.media.Image
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Post (
    val id: UUID,
    val username: String,
    val caption: String,
    val imgUrl: String,
    val pfpUrl: String
) : Parcelable