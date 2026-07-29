package com.lostf1sh.pixelplayeross.data.network.deezer

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface DeezerGatewayApiService {
    @POST("1.0/gateway.php")
    suspend fun call(
        @Query("method") method: String,
        @Query("api_key") apiKey: String,
        @Query("sid") sid: String,
        @Query("arl") arl: String,
        @Query("network") network: String,
        @Query("input") input: String = "3",
        @Query("output") output: String = "3",
        @Query("gateway_input") gatewayInput: String? = null,
        @Body body: Map<String, @JvmSuppressWildcards Any?> = emptyMap()
    ): GwEnvelope
}
