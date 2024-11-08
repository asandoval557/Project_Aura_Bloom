package com.example.project_aura_bloom.api

import retrofit2.Call
import retrofit2.http.GET

interface ZenQuotesApiService {
    @GET("api/random")
    fun getRandomQuote(): Call<List<ZenQuote>>
}