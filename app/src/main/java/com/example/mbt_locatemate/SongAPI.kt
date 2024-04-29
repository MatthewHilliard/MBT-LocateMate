package com.example.mbt_locatemate

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.mbt_locatemate.Constants.Companion.JAMENDO_KEY
import retrofit2.Call
interface SongAPI {
    @GET("v3.0/tracks")
    fun getSongs(
        @Query("search")
        search: String,
        @Query("client_id")
        clientId: String = JAMENDO_KEY,
        @Query("limit")
        limit: String = "40",

    ): Call<SongResponse>
}