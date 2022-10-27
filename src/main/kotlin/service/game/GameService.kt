package service.game

import service.app.StatusMessage
import service.game.data.GameLibrary
import service.game.data.GameObject
import service.game.data.GameStatus
import service.remote.api.GameState
import utils.Log

class GameService {

    var gameProcessListener: GameProcessListener? = null
    var gameResultListener: GameResultListener? = null

    private val games = mutableListOf<GameObject>()

    private var processor: GameProcessor? = null
    var selectedGameObject: GameObject? = null

    init {
        Log.info(this.javaClass.name, "Game service was initialized, games loading now...")
        GameLibrary.all().forEach {
            games.add(it)
            Log.info(this.javaClass.name, "Loaded game: " + it.name)
        }
        Log.info(this.javaClass.name, "Finally loaded " + games.size + " games")
    }

    fun startGameProcess(gameObject: GameObject) {

        StatusMessage.show(
            StatusMessage.DOUBLE_BLOW_WILL_RESTART_GAME,
            "Dvojúderem zahájíš hru",
            StatusMessage.Level.INFO
        )
        processor?.onGameInterrupt()
        this.selectedGameObject = gameObject
        processor = GameProcessor()
        processor!!.onGamePrepare(gameObject)

        Log.info(this.javaClass.name, "Game process was started, new game is ready to go")
    }

    fun interruptGameProcess() {
        processor?.onGameInterrupt()

        Log.info(this.javaClass.name, "Game process was interrupted, game was aborted")
    }

    fun getPagedList(): MutableList<MutableList<GameObject>> {
        val paged = mutableListOf<MutableList<GameObject>>()
        var page = 0
        var index = 0
        paged.add(mutableListOf())
        for (i in 0 until games.size) {
            if (index >= 9) {
                page++
                index = 0
            }
            paged[page].add(games[i])
            index++
        }
        return paged
    }

    fun setGameListener(gameProcessListener: GameProcessListener) {
        this.gameProcessListener = gameProcessListener
    }

    fun setResultListener(gameResultListener: GameResultListener) {
        this.gameResultListener = gameResultListener
    }

    fun getGameProcessStatus() : GameStatus? {
        return processor?.getGameStatus()
    }

    fun getGameState() : GameState? {
        return processor?.getState()
    }


}