package service.remote

import com.fazecast.jSerialComm.SerialPort
import com.google.gson.Gson
import service.game.data.GameObject
import service.game.data.GameStatus
import service.remote.api.CurrentGame
import service.remote.api.GameState
import service.remote.api.Packet
import service.remote.protocol.MessageProtocol
import utils.Log
import java.lang.StringBuilder


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
            it.portDescription.contains("AMA0") // it is first UART PORT used for BT
        }
        if (serialPort != null) {
            remoteServer.start(serialPort, this, this)
            started = true
            return true
        }
        Log.warn(this.javaClass.canonicalName, "Serial port rfcomm not found!")


        return false
    }

    fun dispose() {
        remoteServer.dispose()
    }

    override fun onReadMessage(message: String) {
        Log.info(this.javaClass.canonicalName, "Message received $message")

        val text = message.replace(MessageProtocol.START_CHAR, "").replace(MessageProtocol.END_CHAR, "")
        val gson = Gson()
        val packet = gson.fromJson(text, Packet::class.java)

        remoteCommunicationListener?.onRemoteCommand(packet.endpoint, packet.payload)
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

        val gson = Gson()
        val payload = gson.toJson(currentGame)
        val packet = Packet(
            payload,
            System.currentTimeMillis(),
            Packet.Direction.RESPONSE.toString(),
            "current_game"
        )
        val g = Gson()
        val p = g.toJson(packet)
        val stringBuilder = StringBuilder()
        stringBuilder.insert(0, MessageProtocol.START_CHAR).append(p).append(MessageProtocol.END_CHAR)
        Log.info(this.javaClass.canonicalName, "Message sent: " + stringBuilder.toString())
        remoteServer.write(stringBuilder.toString())
    }

    fun sendNoGame() {
        val packet = Packet(
            null,
            System.currentTimeMillis(),
            Packet.Direction.RESPONSE.toString(),
            "current_game"
        )
        val g = Gson()
        val p = g.toJson(packet)
        remoteServer.write(p)
    }
}

