package service.serial

import service.serial.protocol.Command

interface CommunicationListener {

    fun onCommandReceived(command: Command?)

    fun onConnectionLost()

    fun onCommunicationError(desc: String)

    fun onConnected()
    fun onDataRateSpeed(incomingDataCount: Int, outgoingDataCount: Int) {

    }
}