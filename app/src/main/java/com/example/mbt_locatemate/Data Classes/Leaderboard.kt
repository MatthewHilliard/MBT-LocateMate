package com.example.mbt_locatemate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Leaderboard(
    val username: String,
    val average: Double?,
    val pfpUrl: String,
    var rank: Int,
    var isCurrentUser: Boolean = false
) : Parcelable {
}