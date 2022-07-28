package service.remote.api

import com.beust.klaxon.Json


data class CurrentGame(
    @Json("gameState") val gameState: String,
    @Json("idGame") val idGame: Int,
    @Json("timeout") val timeout: Int,
    @Json("hitPoints") val hitPoints: Int,
    @Json("missesPoints") val missesPoints: Int,
    @Json("gameName") val gameName: String,
    @Json("hits") val hits: Int,
    @Json("misses") val misses: Int,
    @Json("score") val score: Int,
    @Json("hitPanelId") val hitPanelId: Int,
    @Json("hitPanelIndex") val hitPanelIndex: Int,
) {

}

enum class GameState {
    PREPARED, STARTING, PROGRESS, FINISHED
}