package com.example.mbt_locatemate

import retrofit2.Retrofit
import com.example.mbt_locatemate.Constants.Companion.BASE_URL
import retrofit2.converter.moshi.MoshiConverterFactory

private val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

internal val api: SongAPI = retrofit.create(SongAPI::class.java)