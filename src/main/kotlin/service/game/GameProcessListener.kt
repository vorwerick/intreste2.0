package service.game

import service.game.data.GameObject
import service.game.data.GameStatus

interface GameProcessListener {

    fun onGamePrepared(gameObject: GameObject)
    fun onGameStartedUpProgress(secondsPassed: Int, readyCountdown: Int)
    fun onGameStarted(gameObject: GameObject)
    fun onGameHit(gameStatus: GameStatus, gameObject: GameObject)
    fun onGameMiss(gameStatus: GameStatus, gameObject: GameObject)
    fun onGameTimeStep(gameStatus: GameStatus, gameObject: GameObject)
    fun onGameFinished(gameObject: GameObject, gameStatus: GameStatus)
}

interface GameResultListener {

    fun onGamePrepared(gameObject: GameObject)
    fun onGameFinished(gameObject: GameObject, gameStatus: GameStatus)
    fun onGameStarted()
}