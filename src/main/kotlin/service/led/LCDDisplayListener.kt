package service.led

interface LCDDisplayListener {

    fun onLCDDisplayConnected()
    fun onLCDDisplayDisconnected(reason: String)
}