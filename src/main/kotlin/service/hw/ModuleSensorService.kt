package service.hw

import service.serial.ModuleCommunicationService
import utils.mainThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.serial.protocol.Commands
import utils.Log

class ModuleSensorService : ModuleCommunicationService.SensorListener {

    init {
        Log.info(this.javaClass.name, "Module sensor service init")
    }

    interface PanelSortingListener {
        fun onSortingFailed()
        fun onSortingFinished(configuredPanels: List<Panel>)
        fun onSortingProgress(sortedPanels: Int, totalPanels: Int)
        fun onSortingStarted(totalPanels: Int)
    }

    private var panelSortingListener: PanelSortingListener? = null

    val sensors = mutableListOf<Int>()
    val configuredPanels = mutableListOf<Panel>()

    private val lock = Object()

    private var sensorsConnectedValue = 0

    var waitingForSensors: Boolean = false
    var isSorting = false

    var sensorCallback: (() -> Unit)? = null

    fun startSorting(panelSortingListener: PanelSortingListener) {
        this.panelSortingListener = panelSortingListener

        if (isSorting) {
            cancelSorting()
        }

        Service.moduleCommunicationService.addSensorListener(this)
        Service.moduleCommunicationService.listSensors()

        configuredPanels.clear()
        waitingForSensors = true

    }


    private fun isSensorConfigured(sensorIndex: Int): Boolean {
        configuredPanels.forEach { panel ->
            val isConfiguredSensor = panel.sensorIndex == sensorIndex
            if (isConfiguredSensor) {
                return true
            }
        }
        return false
    }

    private fun lightOff(sensorIndex: Int) {
        Service.moduleCommunicationService.lightOffPanel(sensorIndex)
    }

    private fun failedConfiguration() {
        mainThread { panelSortingListener?.onSortingFailed() }
        GlobalScope.launch {
            repeat(3) {
                delay(800)
                Service.moduleCommunicationService.lightUpAllPanels(
                    Commands.PanelColor.RED,
                    500,
                )
            }
        }
    }

    private fun playFinishedAnimation() {
        GlobalScope.launch {
            repeat(3) {
                delay(800)
                Service.moduleCommunicationService.lightUpAllPanels(
                    Commands.PanelColor.BLUE,
                    500,
                )
            }
        }

    }

    override fun onSensorHit(sensorIndex: Int) {
        if (isSorting) {
            if (isSensorConfigured(sensorIndex)) {
                failedConfiguration()
            } else {
                goodConfigure(sensorIndex)
            }
        }
    }

    private fun goodConfigure(sensorIndex: Int) {
        val index = configuredPanels.size
        val panel = Panel(index, sensorIndex)
        configuredPanels.add(panel)

        lightOff(sensorIndex)

        mainThread { panelSortingListener?.onSortingProgress(index + 1, sensors.size) }

        Log.info(
            this.javaClass.name,
            "Panel $index assigned to sensor $sensorIndex"
        )

        if (configuredPanels.size >= sensors.size) {
            isSorting = false
            Service.moduleCommunicationService.stopSensorDetecting()
            Service.moduleCommunicationService.removeSensorListener(this)
            Service.settingsService.sortedPanels = configuredPanels

            GlobalScope.launch {
                delay(400)
                playFinishedAnimation()
                mainThread {
                    panelSortingListener?.onSortingFinished(configuredPanels)
                    sensorCallback?.invoke()
                }
            }

            Log.info(this.javaClass.name, "Panel configuration finished")
        }
    }

    override fun onListSensorIds(sensorIds: List<Int>) {
        if (waitingForSensors) {
            sensors.clear()
            sensors.addAll(sensorIds)
            isSorting = true
            Service.moduleCommunicationService.stopAllAnimations()
            Service.moduleCommunicationService.startSensorDetecting()
            Service.moduleCommunicationService.lightUpAllPanels(
                Commands.PanelColor.BLUE,
                Int.MAX_VALUE
            )

            Log.info(this.javaClass.name, "Panel sorting started")
            waitingForSensors = false

            mainThread {
                panelSortingListener?.onSortingStarted(sensorIds.size)
            }
        }
    }

    fun cancelSorting() {
        configuredPanels.clear()
        isSorting = false
        waitingForSensors = false
        Service.moduleCommunicationService.lightOffAllPanels()
        Service.moduleCommunicationService.stopSensorDetecting()
        Service.moduleCommunicationService.removeSensorListener(this)
    }

    fun loadConfiguredPanels(loadedSortedPanels: List<Panel>) {
        configuredPanels.clear()
        configuredPanels.addAll(loadedSortedPanels)
    }

    fun setSensorsCallback(sensorCallback: () -> Unit) {
        this.sensorCallback = sensorCallback
    }

    fun setSensorsConnected(value: List<Int>) {
        synchronized(lock) {
            sensors.clear()
            sensors.addAll(value)
            sensorsConnectedValue = value.size
        }
    }

    fun getSensorsConnected(): Int {
        synchronized<Int>(lock) {
            return sensorsConnectedValue
        }
    }
}