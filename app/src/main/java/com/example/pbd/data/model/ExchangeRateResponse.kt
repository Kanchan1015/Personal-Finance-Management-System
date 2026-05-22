package com.example.pbd.data.model

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    val result: String = "",
    @SerializedName("base_code")
    val baseCode: String = "",
    val rates: Map<String, Double> = emptyMap()
)
