package service.remote.api

import com.google.gson.annotations.SerializedName

data class CurrentGame(
    @SerializedName("gameState") val gameState: String,
    @SerializedName("idGame") val idGame: Int,
    @SerializedName("timeout") val timeout: Int,
    @SerializedName("hitPoints") val hitPoints: Int,
    @SerializedName("missesPoints") val missesPoints: Int,
    @SerializedName("gameName") val gameName: String,
    @SerializedName("hits") val hits: Int,
    @SerializedName("misses") val misses: Int,
    @SerializedName("score") val score: Int,
    @SerializedName("hitPanelId") val hitPanelId: Int,
    @SerializedName("hitPanelIndex") val hitPanelIndex: Int,
) {

}

enum class GameState {
    PREPARED, STARTING, PROGRESS, FINISHED
}