package service.remote

import com.google.gson.Gson
import service.game.data.GameObject
import service.game.data.GameStatus
import service.remote.api.CurrentGame
import service.remote.api.GameState
import service.remote.api.Packet
import utils.Log


class RemoteService : RemoteServer.ReadMessageListener, RemoteServer.ConnectionListener {

    private var remoteServer: RemoteServer = RemoteServer()
    private var bluetoothApi: BluetoothApi = BluetoothApi()
    var started = false
    private var remoteCommunicationListener: RemoteCommunicationListener? = null

    init {
        Log.info(Log.MessageGroup.SYSTEM, "Remote service init")
    }

    fun start(): Boolean {
        if (started) {
            return false
        }
        remoteServer.start()
        remoteServer.readMessageListener = this
        remoteServer.connectionStatusListener = this
        started = true
        return started

    }

    fun dispose() {
        remoteServer.dispose()
    }

    override fun onReadMessage(message: String) {
        val gson = Gson()
        val packet = gson.fromJson(message, Packet::class.java)

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
        remoteServer.write(p)
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

