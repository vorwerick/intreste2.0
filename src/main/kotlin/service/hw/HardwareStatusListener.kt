package service.hw

interface HardwareStatusListener {

    fun onHardwareData(cpuTemp: Float)
}