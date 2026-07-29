package com.lostf1sh.pixelplayeross.data.network.deezer

import com.google.gson.annotations.SerializedName

data class SmartLoginRequestResponse(
    @SerializedName("data") val data: SmartLoginData?
)

data class SmartLoginData(
    @SerializedName("smartLoginCode") val smartLoginCode: String,
    @SerializedName("url") val url: String,
    @SerializedName("pollingInterval") val pollingInterval: Int
)

data class SmartLoginPollResponse(
    @SerializedName("data") val data: SmartLoginPollData?
)

data class SmartLoginPollData(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("userId") val userId: Long?
)
