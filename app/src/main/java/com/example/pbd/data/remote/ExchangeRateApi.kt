package com.example.pbd.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {

    @GET("v6/latest/{baseCurrency}")
    suspend fun getLatestExchangeRates(
        @Path("baseCurrency") baseCurrency: String
    ): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val rates: Map<String, Double> = emptyMap()
)
