package service.serial


import utils.Constants
import utils.mainThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.repositories.SettingsService
import service.serial.protocol.Command
import service.serial.protocol.Commands
import utils.Log


class ModuleCommunicationService : CommunicationListener {

    init {
        Log.info(Log.MessageGroup.SYSTEM, "Module communication service init")
    }

    interface ConnectionListener {
        fun onConnectionLost()
        fun onConnected()
        fun onDataRateSpeed(incomingDataCount: Int, outgoingDataCount: Int) {}
    }

    interface SensorListener {
        fun onSensorHit(sensorIndex: Int)
        fun onListSensorIds(sensorIds: List<Int>)
    }

    interface SensorBlowListener {
        fun onSensorBlow(sensorIndex: Int)
    }

    companion object {
        const val ANIM_ID_BREATHING = 0
        const val ANIM_ID_BLINKING = 1
        const val ANIM_ID_ARROW = 2
    }

    private val sensorListeners = mutableListOf<SensorListener>()
    private val sensorHitListeners = mutableListOf<SensorBlowListener>()
    private val connectionListeners = mutableListOf<ConnectionListener>()

    private var communication: SerialCommunicationService? = null

    fun connect(): Boolean {
        Log.info(
            Log.MessageGroup.API,
            " COMMAND | Connect to unknown device")

        communication = SerialCommunicationService(this)

        communication?.initialize()
        communication?.connect()
        return true
    }

    fun disconnect() {
        stopSensorDetecting()
        Log.info(Log.MessageGroup.SYSTEM, " COMMAND | Disconnect")
        communication!!.interrupt()
    }

    fun getVersion(response: (version: String) -> Unit) {
        Log.info(Log.MessageGroup.API, " REQUEST | Get version")
        communication!!.sendMessage(Commands.GetVersion())
    }

    fun getFirmwareVersion(sensor: Byte) {
        Log.info(Log.MessageGroup.API, " REQUEST | Get firmware version")
        communication!!.sendMessage(Commands.GetFirmwareVersion(sensor))
    }

    fun startSensorDetecting() {
        Log.info(Log.MessageGroup.API, " REQUEST | Start sensor hit detecting")
        GlobalScope.launch {
            communication!!.sendMessage(
                Commands.SetConfiguration(
                    Constants.ODR,
                    Constants.FS,
                    Constants.MODE,
                    Constants.DS
                )
            )
            delay(30)
            communication!!.sendMessage(Commands.SetDataRate(Constants.DATA_RATE))
            delay(30)
            communication!!.sendMessage(Commands.SetThreshold(SettingsService().threshold))
            delay(30)
            communication!!.sendMessage(Commands.Start())
        }
    }

    fun stopSensorDetecting() {
        Log.info(Log.MessageGroup.API, " REQUEST | Stop sensor hit detecting")
        communication!!.sendMessage(Commands.Stop())
    }

    fun listSensors() {
        Log.info(Log.MessageGroup.API, " REQUEST | List sensors")
        communication!!.sendMessage(Commands.ListSensors())
    }

    fun stopAllAnimations() {
        val panelsCount = Service.moduleSensorService.sensors.size + 1
        Log.info(
            Log.MessageGroup.API,
            " REQUEST | Stop all animations [animationId: 0, color: 00 00 00, brightness: 0, duration: 10, repeat: 1]"
        )
        communication!!.sendMessage(
            Commands.ShowAnimation(
                panelsCount.toByte(), 0, Commands.PanelColor.BLACK, 0,
                10,
                1
            )
        )
    }

    fun playAnimationAllPanels(
        animationId: Int,
        color: Commands.PanelColor,
        duration: Int,
        repeat: Byte
    ) {
        val panelsCount = Service.moduleSensorService.sensors.size + 1
        val brightness = Service.settingsService.brightness
        Log.info(
            Log.MessageGroup.API,
            " REQUEST | Play animation on all panels [panelsCount: $panelsCount, animationId: $animationId, color: ${color.toString()}, brightness: ${brightness.toByte()}, duration: $duration, repeat: $repeat]"
        )
        communication!!.sendMessage(
            Commands.ShowAnimation(
                panelsCount.toByte(), animationId.toByte(), color, brightness.toByte(),
                duration.toByte(),
                repeat.toByte()
            )
        )
    }

    fun playAnimation(
        panelId: Int,
        animationId: Int,
        color: Commands.PanelColor,
        duration: Int,
        repeat: Int
    ) {
        val brightness = Service.settingsService.brightness
        Log.info(Log.MessageGroup.API, " REQUEST | Play animation")
        communication!!.sendMessage(
            Commands.ShowAnimation(
                panelId.toByte(), animationId.toByte(), color, brightness.toByte(),
                duration.toByte(),
                repeat.toByte()
            )
        )
    }

    fun lightUpAllPanels(
        color: Commands.PanelColor,
        duration: Int
    ) {
        val brightness = Service.settingsService.brightness
        val panelsCount = Service.moduleSensorService.sensors.size + 1
        Log.info(
            Log.MessageGroup.API,
            " REQUEST | Emit light on all panels [color: ${color.toString()}, brightness: ${brightness.toByte()}, duration: $duration]"
        )

        communication!!.sendMessage(
            Commands.LightUpLED(
                panelsCount.toByte(),
                0,
                color,
                brightness.toByte(),
                duration
            )
        )
    }

    fun lightUpPanel(
        panelId: Int,
        color: Commands.PanelColor,
        duration: Int
    ) {
        val brightness = Service.settingsService.brightness
        Log.info(
            Log.MessageGroup.API,
            " REQUEST | Emit light [sensor: $panelId, color: ${color.toString()}, brightness: ${brightness.toByte()}), duration: $duration]"
        )
        communication!!.sendMessage(
            Commands.LightUpLED(
                panelId.toByte(),
                0,
                color,
                brightness.toByte(),
                duration
            )
        )
    }

    override fun onCommandReceived(command: Command?) {
        Log.info(
            Log.MessageGroup.API,
            "RESPONSE reveived " + command.toString()
        )
        if (command == null) {
            return
        }
        when (command) {
            /*is Commands.Data -> {
                command.sensors.forEachIndexed { index, sensor ->
                    senso.forEach { listener ->
                        mainThread {
                            listener.onSe(
                                index,
                                sensor.data,
                                sensor.treshold
                            )
                        }
                    }
                }
            }*/
            is Commands.SensorHit -> {
                Log.info(
                    Log.MessageGroup.API,
                    "RESPONSE | Sensor hit [sensor: ${command.value}]"
                )

                mainThread {
                    sensorListeners.forEach { listener ->
                        listener.onSensorHit(
                            command.value
                        )
                    }

                    sensorHitListeners.forEach { listener ->
                        listener.onSensorBlow(
                            command.value
                        )
                    }
                }

            }
            is Commands.SensorList -> {
                val sensors = command.sensors.joinToString(", ")
                Log.info(
                    Log.MessageGroup.API,
                    "RESPONSE | List sensors [sensor ids: $sensors]"
                )
                mainThread {
                    sensorListeners.forEach { listener ->
                        listener.onListSensorIds(
                            command.sensors
                        )
                    }
                }
            }
            is Commands.GetVersion -> {

            }

        }
    }

    override fun onConnectionLost() {
        mainThread {
            connectionListeners.forEach { listener -> listener.onConnectionLost() }
        }
    }

    override fun onCommunicationError(desc: String) {
        mainThread {
            connectionListeners.forEach { listener -> listener.onConnectionLost() }
        }
    }

    override fun onConnected() {
        mainThread {
            connectionListeners.forEach { listener -> listener.onConnected() }
        }
    }

    override fun onDataRateSpeed(incomingDataCount: Int, outgoingDataCount: Int) {
        mainThread {
            connectionListeners.forEach { listener ->
                listener.onDataRateSpeed(
                    incomingDataCount,
                    outgoingDataCount
                )
            }
        }
    }

    fun addSensorListener(listener: SensorListener) {
        sensorListeners.add(listener)
    }

    fun removeSensorListener(listener: SensorListener) {
        sensorListeners.remove(listener)
    }

    fun addSensorBlowListener(listener: SensorBlowListener) {
        sensorHitListeners.add(listener)
    }

    fun removeSensorBlowListener(listener: SensorBlowListener) {
        sensorHitListeners.remove(listener)
    }

    fun addConnectionListener(listener: ConnectionListener) {
        connectionListeners.add(listener)
    }

    fun removeConnectionListener(listener: ConnectionListener) {
        connectionListeners.remove(listener)
    }

    fun lightOffAllPanels() {
        val panelsCount = Service.moduleSensorService.sensors.size + 1
        Log.info(
            Log.MessageGroup.API,
            " REQUEST | Turn off light on all panels"
        )

        communication!!.sendMessage(
            Commands.LightUpLED(
                panelsCount.toByte(),
                0,
                Commands.PanelColor.BLACK,
                0,
                10
            )
        )
    }

    fun lightOffPanel(sensorIndex: Int) {
        Log.info(
            Log.MessageGroup.API,
            " REQUEST | Turn off light "
        )
        communication!!.sendMessage(
            Commands.LightUpLED(
                sensorIndex.toByte(),
                0,
                Commands.PanelColor.BLACK,
                0,
                10
            )
        )
    }


}