package com.example.mbt_locatemate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class Guess(
    val username: @RawValue String,
    val distance: @RawValue Double
) : Parcelable
