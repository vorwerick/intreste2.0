package service.hw

import utils.mainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class RaspberryInfoService {

    private val hardwareStatusListeners = mutableListOf<HardwareStatusListener>()

    init {
        Log.info(Log.MessageGroup.SYSTEM, "Raspberry info service init")
    }

    fun initialize() {
        var process: Process
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {

                try {
                    process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp")
                    process.waitFor()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val line: String = reader.readLine()
                    val temp = line.toFloat() / 1000.0f
                    invokeData(temp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                delay(5000)
            }

        }
    }

    private fun invokeData(cpuTemp: Float) {
        Log.info(
            Log.MessageGroup.SYSTEM,"CPU-TEMP: $cpuTemp C"
        )
        mainThread {
            hardwareStatusListeners.forEach {
                it.onHardwareData(
                    cpuTemp
                )
            }
        }
    }

    fun addHardwareStatusListener(hardwareStatusListener: HardwareStatusListener) {
        hardwareStatusListeners.add(hardwareStatusListener)
    }

    fun removeHardwareStatusListener(hardwareStatusListener: HardwareStatusListener) {
        hardwareStatusListeners.remove(hardwareStatusListener)
    }
}