package com.example.mbt_locatemate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Leaderboard(
    val username: @RawValue String,
    val average: @RawValue Double?,
    val pfpUrl: String,
    var rank: Int,
    var medal: String? = null,
    var isCurrentUser: Boolean = false
) : Parcelable {
}