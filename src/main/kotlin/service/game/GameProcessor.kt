package service.game

import kotlinx.coroutines.*
import service.game.data.GameObject
import service.game.data.GameStatus
import service.hw.Panel
import service.led.ExternalDisplayService
import service.remote.api.GameState
import service.serial.ModuleCommunicationService
import utils.Constants
import utils.SynchronizedTimer
import service.serial.protocol.Commands
import utils.Log
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.Lock
import kotlin.random.Random

class GameProcessor() {


    companion object {
        private val lock = Object()
        private val executor = Executors.newFixedThreadPool(4)
        private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    }

    private var doubleBlow: Boolean = false

    private val activePanels = mutableListOf<Panel>()

    private var startUpTimer = SynchronizedTimer()
    private var progressTimer = SynchronizedTimer()
    private var endGameTimer = SynchronizedTimer()

    private lateinit var gameObject: GameObject //current game config
    private lateinit var gameStatus: GameStatus //current game data
    private var gameState: GameState = GameState.FINISHED

    private val sensorBlowForIdleStateListener =
        object : ModuleCommunicationService.SensorBlowListener {
            override fun onSensorBlow(sensorIndex: Int) {
                if (doubleBlow) {
                    startUpGame()
                }
                doubleBlow = true
                scheduledExecutor.schedule({
                    synchronized(lock) {
                        doubleBlow = false
                    }
                }, 1000L, TimeUnit.MILLISECONDS)
            }
        }

    private val gameSensorBlowListener: ModuleCommunicationService.SensorBlowListener =
        object : ModuleCommunicationService.SensorBlowListener {
            override fun onSensorBlow(sensorIndex: Int) {
                sensorBlow(sensorIndex)
            }
        }

    /**
     * Prepare game clean previous data, setup listeners, and wait for double kick sensor for game start
     * You can startup without waiting for double kick
     * */
    fun onGamePrepare(gameObject: GameObject) {
        this.gameObject = gameObject
        this.gameStatus = GameStatus(gameObject)

        activePanels.clear()

        gameState = GameState.PREPARED
        Service.moduleCommunicationService.stopAllAnimations()
        Service.remoteMasterService.sendCurrentGameInfo(gameState, gameObject, gameStatus)
        Service.moduleCommunicationService.startSensorDetecting()
        Service.moduleCommunicationService.addSensorBlowListener(sensorBlowForIdleStateListener)

        executor.submit {
            Service.externalDisplayService.clearAll()
            Thread.sleep(100)
            Service.externalDisplayService.updateFullMessage("READY", ExternalDisplayService.GREEN)
            Service.moduleCommunicationService.stopAllAnimations()
            Thread.sleep(300)
            Service.moduleCommunicationService.playAnimationAllPanels(
                ModuleCommunicationService.ANIM_ID_BREATHING,
                Commands.PanelColor.PURPLE,
                5,
                255.toByte()
            )
        }

        Service.gameService.gameProcessListener?.onGamePrepared(gameObject)
        Service.gameService.gameResultListener?.onGamePrepared(gameObject)

        //wait for double kick
    }

    /**
     * Interrupt game variablies and pause all timers, dispose, remove listeners
     * */
    fun onGameInterrupt() {
        endGameTimer.terminate()
        progressTimer.terminate()
        startUpTimer.terminate()
        Service.moduleCommunicationService.removeSensorBlowListener(sensorBlowForIdleStateListener)
        Service.moduleCommunicationService.removeSensorBlowListener(gameSensorBlowListener)
        Service.moduleCommunicationService.lightOffAllPanels()
        Service.moduleCommunicationService.stopAllAnimations()
        Service.moduleCommunicationService.stopSensorDetecting()
    }

    private fun sensorBlow(sensorIndex: Int) {
        val maxHits = gameObject.rules.maxHits
        val infinity = maxHits == 0
        if (gameStatus.hitCount < maxHits || infinity) {
            val panel =
                activePanels.firstOrNull { panel -> panel.sensorIndex == sensorIndex }
            if (panel != null) {
                gameStatus.hitPanelId = panel.sensorIndex
                gameStatus.hitPanelIndex = panel.positionIndex
                hit(sensorIndex)
                activePanels.remove(panel)
                generateActivePanel(
                    gameObject.rules.maxActivePanels,
                    gameObject.rules.panelsGap,
                    sensorIndex
                )
            } else {
                miss(sensorIndex)
            }
        } else {
            synchronized(lock){
                gameFinished()
            }
        }
    }


    private fun resultTimeStarted() {
        val timeout = 20
        Log.info(this.javaClass.name, "Game is in result phase, duration " + timeout + "s")

        playResultAnimation()
        endGameTimer.begin(timeout, onFinished = {
            Thread.sleep(500)
            synchronized(lock){
                Service.gameService.startGameProcess(gameObject)
            }
        })
    }

    private fun playResultAnimation() {
        executor.submit {
            Thread.sleep(500)
            Service.moduleCommunicationService.playAnimationAllPanels(
                ModuleCommunicationService.ANIM_ID_BREATHING,
                Commands.PanelColor.RED,
                5,
                255.toByte()
            )
        }
    }

    private fun gameRunning() {
        Log.info(this.javaClass.name, "Game was started, now in progress")

        activePanels.clear()
        gameState = GameState.PROGRESS
        Service.remoteMasterService.sendCurrentGameInfo(gameState, gameObject, gameStatus)
        Service.moduleCommunicationService.lightOffAllPanels()
        Service.moduleCommunicationService.stopAllAnimations()

        executor.submit {

            Thread.sleep(200)
            Service.gameService.gameProcessListener?.onGameStarted(gameObject)
            Service.moduleCommunicationService.startSensorDetecting()
            Service.moduleCommunicationService.addSensorBlowListener(gameSensorBlowListener)

            Service.externalDisplayService.clearFullMessage()
            Thread.sleep(10)
            Service.externalDisplayService.updateTime(gameObject.configuration.timeoutSeconds)
            Thread.sleep(20)
            Service.externalDisplayService.updateScore((gameStatus.hitCount * gameObject.configuration.hitPoints) + gameStatus.missCount * gameObject.configuration.missesPoints)

            synchronized(lock){
                generateActivePanel(
                    gameObject.rules.maxActivePanels,
                    gameObject.rules.panelsGap
                )
            }

            progressTimer.begin(
                gameObject.configuration.timeoutSeconds,
                { secondsPassed: Int, totalSeconds: Int ->
                    synchronized(lock){
                        gameStatus.timeout = totalSeconds - secondsPassed
                        Service.gameService.gameProcessListener?.onGameTimeStep(gameStatus, gameObject)
                        Service.externalDisplayService.updateTime(gameStatus.timeout)

                        Service.remoteMasterService.sendCurrentGameInfo(
                            gameState,
                            gameObject,
                            gameStatus
                        )
                    }
                },
                {
                    Thread.sleep(1000)
                    synchronized(lock){
                        gameFinished()
                    }
                })
        }

    }

    private fun gameFinished() {
        gameState = GameState.FINISHED
        Service.gameService.gameProcessListener?.onGameFinished(gameObject, gameStatus)
        Service.gameService.gameResultListener?.onGameFinished(gameObject, gameStatus)

        executor.submit {
            Service.externalDisplayService.stopAnimation()
            Service.externalDisplayService.clearTime()
            Thread.sleep(100)
            Service.externalDisplayService.showScoreTitle((gameStatus.hitCount * gameObject.configuration.hitPoints) + gameStatus.missCount * gameObject.configuration.missesPoints)
        }
        Log.info(this.javaClass.name, "Game was finished")

        Service.moduleCommunicationService.removeSensorBlowListener(gameSensorBlowListener)
        Service.moduleCommunicationService.stopAllAnimations()
        Service.moduleCommunicationService.stopSensorDetecting()
        Service.moduleCommunicationService.lightOffAllPanels()

        Service.remoteMasterService.sendCurrentGameInfo(gameState, gameObject, gameStatus)

        activePanels.clear()
        resultTimeStarted()
    }


    private fun hit(sensorIndex: Int) {
        Log.info(
            this.javaClass.name,
            "Hit correct panel, index is $sensorIndex"
        )
        gameState = GameState.PROGRESS
        gameStatus.hitCount++
        Service.moduleCommunicationService.lightOffPanel(sensorIndex) // off
        Service.gameService.gameProcessListener?.onGameHit(gameStatus, gameObject)
        Service.remoteMasterService.sendCurrentGameInfo(gameState, gameObject, gameStatus)
        Service.externalDisplayService.updateScore((gameStatus.hitCount * gameObject.configuration.hitPoints) + gameStatus.missCount * gameObject.configuration.missesPoints)

        val hitPoints = gameObject.configuration.hitPoints
        Service.externalDisplayService.showAnimation(true, "+" + gameObject.configuration.hitPoints)
    }

    private fun miss(sensorIndex: Int) {
        Log.info(
            this.javaClass.name,
            "Hit wrong panel, index is $sensorIndex"
        )
        gameState = GameState.PROGRESS
        gameStatus.missCount++
        Service.moduleCommunicationService.lightUpPanel(
            sensorIndex,
            Commands.PanelColor.RED,
            300
        )
        Service.externalDisplayService.updateScore((gameStatus.hitCount * gameObject.configuration.hitPoints) + gameStatus.missCount * gameObject.configuration.missesPoints)

        Service.gameService.gameProcessListener?.onGameMiss(gameStatus, gameObject)
        Service.remoteMasterService.sendCurrentGameInfo(gameState, gameObject, gameStatus)
        Service.externalDisplayService.showAnimation(false, gameObject.configuration.missesPoints.toString())

    }

    private fun generateActivePanel(
        maxActivePanels: Int,
        panelsGap: Int,
        lastSensorIndex: Int = -1
    ) {
        val panels = Service.moduleSensorService.configuredPanels
        val gap = if (panels.size / 4 >= panelsGap) panelsGap else 0
        var panelsCount = activePanels.size
        val configuredPanelsCount = panels.size
        val lastIndex =
            panels.firstOrNull() { panel -> panel.sensorIndex == lastSensorIndex }?.positionIndex
                ?: -1
        val finalMaxActivePanels =
            if (configuredPanelsCount >= maxActivePanels) maxActivePanels else configuredPanelsCount
        while (panelsCount < finalMaxActivePanels) {
            val claimedIndexes = mutableListOf<Int>()
            if (lastIndex != -1) {
                claimedIndexes.add(lastIndex)
            }
            activePanels.forEach {
                val index = it.positionIndex
                if (gap > 0) {
                    claimedIndexes.add(index)
                    for (i in 1..gap) {
                        var indexWithGap = index + i
                        if (indexWithGap >= panelsCount) {
                            indexWithGap -= panelsCount
                        }
                        claimedIndexes.add(indexWithGap)
                    }
                    for (i in 1..gap) {
                        var indexWithGap = index - i
                        if (indexWithGap < 0) {
                            indexWithGap += panelsCount
                        }
                        claimedIndexes.add(indexWithGap)
                    }
                }
            }
            val availableIndexes = panels.map { it.positionIndex }
            val indexes =
                availableIndexes.filter { availableIndex -> !claimedIndexes.any { claimedIndex -> claimedIndex == availableIndex } }
            val randomIndex =
                Random(System.currentTimeMillis()).nextInt(0, indexes.size)
            val random = indexes[randomIndex]
            val randomPanel = panels.first { it.positionIndex == random }

            Log.info(
                this.javaClass.name,
                "Next generated active panel $randomIndex(sensor: ${randomPanel.sensorIndex}) waits for hit"
            )
            activePanels.add(randomPanel)
            Service.moduleCommunicationService.lightUpPanel(
                randomPanel.sensorIndex,
                Commands.PanelColor.GREEN,
                Int.MAX_VALUE
            )
            panelsCount++
        }
    }


    /**
     * Startup begin sequence 3.2.1..
     * Then game begins
     * Start sequence timer
     * */
    private fun startUpGame() {
        Service.gameService.gameResultListener?.onGameStarted()
        Service.moduleCommunicationService.stopSensorDetecting()
        Service.moduleCommunicationService.removeSensorBlowListener(
            sensorBlowForIdleStateListener
        )
        Service.moduleCommunicationService.playAnimationAllPanels(
            ModuleCommunicationService.ANIM_ID_BLINKING,
            Commands.PanelColor.PURPLE,
            80,
            10
        )

        startUpTimer.begin(
            Constants.READY_COUNTDOWN, onTick =
            { secondsPassed: Int, secondsTotal: Int ->
                if (secondsPassed == 4) {
                    Service.moduleCommunicationService.playAnimationAllPanels(
                        ModuleCommunicationService.ANIM_ID_BLINKING,
                        Commands.PanelColor.PURPLE,
                        15,
                        20
                    )
                }
                synchronized(lock){
                    Service.gameService.gameProcessListener?.onGameStartedUpProgress(
                        secondsPassed,
                        Constants.READY_COUNTDOWN
                    )
                }
                var timeout: Int = (secondsTotal - secondsPassed)
                if (timeout <= 1) {
                    timeout = 1
                }

                Service.externalDisplayService.updateFullMessage(
                    timeout.toString(),
                    when (timeout) {
                        3 -> ExternalDisplayService.RED
                        2 -> ExternalDisplayService.YELLOW
                        else -> ExternalDisplayService.GREEN
                    }
                )
            }, onFinished = {
                Service.moduleCommunicationService.lightOffAllPanels()
                Service.moduleCommunicationService.stopAllAnimations()

                synchronized(lock){
                    gameRunning()
                }
                // after startup sequence game is running
            })
    }

    fun sendCurrentGameStatus() {
        Service.remoteMasterService.sendCurrentGameInfo(gameState, gameObject, gameStatus)
    }

}