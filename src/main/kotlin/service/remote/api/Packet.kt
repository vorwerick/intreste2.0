package service.remote.api

import com.google.gson.annotations.SerializedName


data class Packet(
    @SerializedName("payload") val payload: String?,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("direction") val direction: String,
    @SerializedName("endpoint") val endpoint: String,
    @SerializedName("error") val error: String? = null
) {

    enum class Direction {
        REQUEST, RESPONSE
    }
}