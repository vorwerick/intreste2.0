package service.remote

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.game.data.GameLibrary
import service.game.data.GameObject
import service.game.data.GameStatus
import service.hw.ModuleSensorService
import service.hw.Panel
import service.hw.RaspberryInfoService
import service.remote.api.AppState
import service.remote.api.CurrentGame
import service.remote.api.GameState
import service.remote.protocol.MessageProtocol
import utils.Log


class RemoteService : RemoteServer.ReadMessageListener, RemoteServer.ConnectionListener {

    private var remoteServer: RemoteServer = RemoteServer()
    var started = false
    private var remoteCommunicationListener: RemoteCommunicationListener? = null

    init {
        Log.info(this.javaClass.name, "Remote service init")
    }

    fun start(): Boolean {
        if (started) {
            return false
        }
        started = false
        SerialPort.getCommPorts().forEach {
            Log.info(
                this.javaClass.canonicalName,
                "Found port " + it.portLocation + " " + it.portDescription + " " + it.systemPortPath + " " + it.systemPortName + " " + it.descriptivePortName
            )
        }
        val serialPort = SerialPort.getCommPorts().firstOrNull {
            it.portDescription.contains("S0") // it is first UART PORT S0 is serial port 0
        }
        if (serialPort != null) {
            remoteServer.start(serialPort, this, this)
            started = true
            GlobalScope.launch {
                while (true) {
                    delay(1000)
                    remoteServer.sendMessage(MessageProtocol.PING, null)
                }
            }
            return true
        }
        Log.warn(this.javaClass.canonicalName, "Serial port rfcomm not found!")


        return false
    }

    fun dispose() {
        remoteServer.dispose()
    }


    fun setCommandListener(remoteCommunicationListener: RemoteCommunicationListener) {
        this.remoteCommunicationListener = remoteCommunicationListener
    }

    interface RemoteCommunicationListener {
        fun onRemoteCommand(endpoint: String, payload: String?)
        fun onConnectionStatusChanged(connectionStatus: RemoteServer.ConnectionStatus)
    }

    override fun onRemoteConnectionStatusChanged(status: RemoteServer.ConnectionStatus) {
        remoteCommunicationListener?.onConnectionStatusChanged(status)
    }

    fun sendAppInfo() {
        val state = AppState(
            Service.moduleSensorService.getSensorsConnected(),
            RaspberryInfoService.coreTemp.toInt(),
            "${Build.VERSION_NAME} build ${Build.VERSION_NUMBER}",
            Service.gameService.selectedGameObject != null,
            Service.moduleSensorService.configuredPanels.isNotEmpty(),
            Service.moduleSensorService.isSorting,
            Service.moduleCommunicationService.isConnected(),
            Service.externalDisplayService.isConnected(), "*"


        )
        val jsonString = Klaxon().toJsonString(state)

        remoteServer.sendMessage(
            MessageProtocol.DATA, JsonObject(
                mapOf("command" to "state", "state" to jsonString)
            )
        )
    }

    fun sendCurrentGameInfo() {

        val gameObject = Service.gameService.selectedGameObject ?: GameObject.emptyGameObject()
        val status = Service.gameService.getGameState()
        val gameStatus = Service.gameService.getGameProcessStatus()

        val hitCount = gameStatus?.hitCount ?: 0
        val missCount = gameStatus?.missCount ?: 0

        val currentGame = CurrentGame(
            status.toString(),
            0,
            gameStatus?.timeout ?: gameObject.configuration.timeoutSeconds,
            gameObject.configuration.hitPoints,
            gameObject.configuration.missesPoints,
            gameObject.name,
            hitCount,
            missCount,
            ((hitCount * gameObject.configuration.hitPoints) + (missCount * gameObject.configuration.missesPoints)),
            gameStatus?.hitPanelId ?: -1,
            gameStatus?.hitPanelIndex ?: -1
        )
        val jsonString = Klaxon().toJsonString(currentGame)

        remoteServer.sendMessage(
            MessageProtocol.DATA, JsonObject(
                mapOf("command" to "currentGame", "currentGame" to jsonString)
            )
        )
    }

    fun sendNoGame() {
        remoteServer.sendMessage(
            MessageProtocol.DATA, JsonObject(
                mapOf("command" to "currentGame", "currentGame" to null)
            )
        )
    }

    override fun onDataReceived(type: Int, jsonObject: JsonObject) {

        GlobalScope.launch(Dispatchers.Main) {
            if (jsonObject["command"] == "getCurrentGame") {
                sendCurrentGameInfo()
                Log.info("Data received", "get current game")
            }
            if (jsonObject["command"] == "getState") {
                sendAppInfo()
                Log.info("Data received", "get current state")
            }
            if (jsonObject["command"] == "sortPanels") {
                Service.moduleSensorService.startSorting(object : ModuleSensorService.PanelSortingListener {
                    override fun onSortingFailed() {
                    }

                    override fun onSortingFinished(configuredPanels: List<Panel>) {
                        Service.settingsService.saveSettings()
                    }

                    override fun onSortingProgress(sortedPanels: Int, totalPanels: Int) {
                    }

                    override fun onSortingStarted(totalPanels: Int) {
                    }

                })
                Log.info("Data received", "sort panels")

            }
            if (jsonObject["command"] == "startGame") {
                val game = GameLibrary.all().first()
                game.configure(
                    GameObject.Configuration(
                        jsonObject["hitPoints"].toString().toIntOrNull(10) ?: GameObject.defaultHitPoints,
                        jsonObject["missesPoints"].toString().toIntOrNull(10) ?: GameObject.defaultMissPoints,
                        jsonObject["timeout"].toString().toIntOrNull(10) ?: GameObject.defaultTimeout
                    )
                )
                Service.gameService.startGameProcess(game)
                Log.info("Data received", "start game " + jsonObject.toJsonString())


            }
            if (jsonObject["command"] == "startLedTest") {
                Log.info("Data received", "start led test")
                Service.externalDisplayService.testDisplay()
            }
            if (jsonObject["command"] == "startPanelTest") {
                Log.info("Data received", "start panel test")
                Service.moduleCommunicationService.playPanelTest()
            }
            if (jsonObject["command"] == "interruptSortPanels") {
                Log.info("Data received", "interrupt sorting")

            }
            if (jsonObject["command"] == "restartGame") {
                Log.info("Data received", "restart game")
                Service.gameService.startGameProcess(
                    Service.gameService.selectedGameObject ?: GameObject(
                        "Zasahni co nejvic",
                        "",
                        GameObject.Type.CLASSIC_RANDOM_TIMEOUT, GameObject.Rules(0, 0, 2, 2, 1),
                    )
                )
            }
        }

    }

    override fun onPingReceived() {
    }

    override fun onResponseOkReceived(messageId: Long) {
    }
}

