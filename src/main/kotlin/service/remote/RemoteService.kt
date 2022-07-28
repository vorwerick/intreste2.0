package service.remote

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.game.data.GameObject
import service.game.data.GameStatus
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
                while (true){
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

    fun sendCurrentGameInfo(status: GameState, gameObject: GameObject, gameStatus: GameStatus) {
        val currentGame = CurrentGame(
            status.toString(),
            0,
            gameStatus.timeout,
            gameObject.configuration.hitPoints,
            gameObject.configuration.missesPoints,
            gameObject.name,
            gameStatus.hitCount,
            gameStatus.missCount,
            ((gameStatus.hitCount * gameObject.configuration.hitPoints) + (gameStatus.missCount * gameObject.configuration.missesPoints)),
            gameStatus.hitPanelId,
            gameStatus.hitPanelIndex
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
            //TODO
        }

    }

    override fun onPingReceived() {
    }

    override fun onResponseOkReceived(messageId: Long) {
    }
}

