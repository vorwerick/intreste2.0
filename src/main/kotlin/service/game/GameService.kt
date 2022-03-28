package service.game

import service.app.StatusMessage
import service.game.data.GameLibrary
import service.game.data.GameObject
import utils.Log

class GameService {

    var gameProcessListener: GameProcessListener? = null
    var gameResultListener: GameResultListener? = null

    private val games = mutableListOf<GameObject>()

    private var processor: GameProcessor? = null
    var selectedGameObject: GameObject? = null

    init {
        Log.info(Log.MessageGroup.SYSTEM, "Game service init")
        games.addAll(GameLibrary.all())
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
    }

    fun interruptGameProcess() {
        processor?.onGameInterrupt()
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

    fun sendCurrentGameStatus() {
        if(processor == null){
            Service.remoteMasterService.sendNoGame()
        } else {
            processor!!.sendCurrentGameStatus()
        }

    }


}