package service.remote.api

import com.google.gson.annotations.SerializedName


class GameConfig(
    @SerializedName("timeout") val timeout: Int,
    @SerializedName("hitPoints") val hitPoints: Int,
    @SerializedName("missesPoints") val missesPoints: Int
)