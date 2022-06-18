package service.hw

import com.sun.management.OperatingSystemMXBean
import utils.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.management.ManagementFactory
import java.util.concurrent.Executors


class RaspberryInfoService {

    private val hardwareStatusListeners = mutableListOf<HardwareStatusListener>()

    companion object {
        private val executor = Executors.newSingleThreadExecutor()
    }

    init {
        Log.info(this.javaClass.name, "Raspberry info service was initialized")
    }

    fun startTemperatureReadTask(repeat: Long) {
        var process: Process

        val osBean: OperatingSystemMXBean = ManagementFactory.getPlatformMXBean(
            OperatingSystemMXBean::class.java
        )

        val runtime = Runtime.getRuntime()

        executor.submit {
            while (true) {

                try {
                    process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp")
                    process.waitFor()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val line: String = reader.readLine()
                    val temp = line.toFloat() / 1000.0f

                    Log.info(
                        this.javaClass.name, "Temperature CPU: $temp C"
                    )
                } catch (e: Exception) {
                    Log.error(this.javaClass.name, e.localizedMessage)
                }

                val jvmCpuUsage = osBean.processCpuLoad * 100 //percent
                Log.info(
                    this.javaClass.name, "CPU usage JVM: $jvmCpuUsage%"
                )

                val systemCpuUsage = osBean.systemCpuLoad * 100 //percent
                Log.info(
                    this.javaClass.name, "CPU usage overall: $systemCpuUsage%"
                )

                val totalMemory = runtime.totalMemory()
                val freeMemory = runtime.freeMemory()
                val usedMemory = totalMemory - freeMemory
                val maxMemory = runtime.maxMemory()

                Log.info(
                    this.javaClass.name, "RAM used: ${usedMemory / (1024 * 1024)} MB"
                )
                Log.info(
                    this.javaClass.name, "RAM total: ${totalMemory / (1024 * 1024)} MB"
                )
                Log.info(
                    this.javaClass.name, "RAM free: ${freeMemory / (1024 * 1024)} MB"
                )

                Log.info(
                    this.javaClass.name, "RAM max: ${maxMemory / (1024 * 1024)} MB"
                )

                Thread.sleep(repeat)
            }
        }
    }


    fun initialize() {
        startTemperatureReadTask(10000)
    }


    fun addHardwareStatusListener(hardwareStatusListener: HardwareStatusListener) {
        hardwareStatusListeners.add(hardwareStatusListener)
    }

    fun removeHardwareStatusListener(hardwareStatusListener: HardwareStatusListener) {
        hardwareStatusListeners.remove(hardwareStatusListener)
    }
}