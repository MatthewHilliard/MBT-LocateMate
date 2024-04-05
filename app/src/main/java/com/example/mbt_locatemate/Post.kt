package com.example.mbt_locatemate

import android.media.Image
import java.util.UUID

data class Post (
    val id: UUID,
    val username:String,
    val caption:String
)