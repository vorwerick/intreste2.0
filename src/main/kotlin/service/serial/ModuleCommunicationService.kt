package service.serial


import Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.repositories.SettingsService
import service.serial.protocol.Command
import service.serial.protocol.Commands
import utils.Constants
import utils.Log
import utils.mainThread
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock


class ModuleCommunicationService : CommunicationListener {

    val lock = ReentrantReadWriteLock()
    val readLock = lock.readLock()
    val writeLock = lock.writeLock()

    init {
        Log.info(this.javaClass.name, "Module comunication service was initialized")
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

    private val sensorListeners = Collections.synchronizedList(mutableListOf<SensorListener>())
    private val sensorHitListeners = Collections.synchronizedList(mutableListOf<SensorBlowListener>())
    private val connectionListeners = Collections.synchronizedList(mutableListOf<ConnectionListener>())

    private var communication: SerialCommunicationService? = null

    fun connect(): Boolean {
        Log.info(this.javaClass.name, "Communication starting")

        communication = SerialCommunicationService(this)

        communication?.initialize()

        return try {
            communication?.connect()


            Log.info(this.javaClass.name, "Connected to serial communication Intreste module")
            true
        } catch (e: NotFoundSerialDeviceException) {
            Log.error(this.javaClass.name, e.localizedMessage)
            false
        }
    }

    fun disconnect() {
        stopSensorDetecting()
        communication!!.interrupt()
    }

    fun getVersion(response: (version: String) -> Unit) {
        communication!!.sendMessage(Commands.GetVersion())
    }

    fun getFirmwareVersion(sensor: Byte) {
        communication!!.sendMessage(Commands.GetFirmwareVersion(sensor))
    }

    fun startSensorDetecting() {
        Log.info(this.javaClass.name, "Sensor detecting started")

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
        Log.info(this.javaClass.name, "Sensor detecting stopped")

        communication!!.sendMessage(Commands.Stop())
    }

    fun listSensors() {
        Log.info(this.javaClass.name, "List sensors call, waiting for response")
        communication!!.sendMessage(Commands.ListSensors())
    }

    fun stopAllAnimations() {
        val panelsCount = Service.moduleSensorService.sensors.size + 1
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
        if (command == null) {
            return
        }
        when (command) {
            /*is Commands.Data -> {
                command.sensors.forEachIndexed { index, sensor ->
                    senso.forEach { listener ->
                         synchronized(connectionListeners) {
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
                val sensorListenersIterator: MutableIterator<SensorListener> = sensorListeners.iterator()
                while (sensorListenersIterator.hasNext()) {
                    val listener: SensorListener = sensorListenersIterator.next()
                    mainThread {
                        listener.onSensorHit(
                            command.value
                        )
                    }
                }


                val sensorHitListenersIterator: MutableIterator<SensorBlowListener> = sensorHitListeners.iterator()
                while (sensorHitListenersIterator.hasNext()) {
                    val listener: SensorBlowListener = sensorHitListenersIterator.next()
                    mainThread {
                        listener.onSensorBlow(
                            command.value
                        )
                    }
                }

            }
            is Commands.SensorList -> {

                val sensorListenersIterator: MutableIterator<SensorListener> = sensorListeners.iterator()
                while (sensorListenersIterator.hasNext()) {
                    val listener: SensorListener = sensorListenersIterator.next()
                    mainThread {
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
        synchronized(connectionListeners) {
            connectionListeners.forEach { listener -> listener.onConnectionLost() }
        }
    }

    override fun onCommunicationError(desc: String) {
        synchronized(connectionListeners) {
            connectionListeners.forEach { listener -> listener.onConnectionLost() }
        }
    }

    override fun onConnected() {
        synchronized(connectionListeners) {
            connectionListeners.forEach { listener -> listener.onConnected() }
        }
    }

    override fun onDataRateSpeed(incomingDataCount: Int, outgoingDataCount: Int) {
        synchronized(connectionListeners) {
            connectionListeners.forEach { listener ->
                listener.onDataRateSpeed(
                    incomingDataCount,
                    outgoingDataCount
                )
            }
        }
    }

    fun addSensorListener(listener: SensorListener) {
        synchronized(sensorListeners) {
            sensorListeners.add(listener)
        }
    }

    fun removeSensorListener(listener: SensorListener) {
        synchronized(sensorListeners) {
            sensorListeners.remove(listener)
        }
    }

    fun addSensorBlowListener(listener: SensorBlowListener) {
        synchronized(sensorHitListeners) {
            sensorHitListeners.add(listener)
        }
    }

    fun removeSensorBlowListener(listener: SensorBlowListener) {
        synchronized(sensorHitListeners) {
            sensorHitListeners.remove(listener)
        }
    }

    fun addConnectionListener(listener: ConnectionListener) {
        synchronized(connectionListeners) {
            connectionListeners.add(listener)
        }
    }

    fun removeConnectionListener(listener: ConnectionListener) {
        synchronized(connectionListeners) {
            connectionListeners.remove(listener)
        }
    }

    fun lightOffAllPanels() {
        val panelsCount = Service.moduleSensorService.sensors.size + 1

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