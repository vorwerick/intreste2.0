package service.remote

import Constants
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortIOException
import com.fazecast.jSerialComm.SerialPortTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.remote.protocol.MessageProtocol
import utils.Log
import utils.mainThread
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.thread


class RemoteServer {

    // var serverSocket: BluetoothServerSocket? = null
    // var clientSocket: BluetoothSocket? = null
    var readMessageListener: ReadMessageListener? = null
    var connectionStatusListener: ConnectionListener? = null
    var status = ConnectionStatus.DISCONNECTED

    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null

    val messageProtocol: MessageProtocol = MessageProtocol()



    enum class ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING, DISCONNECTING
    }


    fun start(
        serialPort: SerialPort,
        readMessageListener: ReadMessageListener,
        connectionListener: ConnectionListener
    ): Boolean {
        this.readMessageListener = readMessageListener
        this.connectionStatusListener = connectionListener

        messageProtocol.readMessageCallback = { message ->
            resolveMessage(message)
        }

        return try {
            serialPort.baudRate = Constants.HC_05_BT_MODULE_BAUD
            serialPort.openPort()
            Log.info(this.javaClass.canonicalName, "Opening serial port for communication")
            outputStream = serialPort.outputStream
            startReadTask(serialPort)
            //startWriteTask(serialPort)

            true

        } catch (e: IOException) {
            Log.error(this.javaClass.canonicalName, e.localizedMessage)
            false
        }

    }

    private fun resolveMessage(message: MessageProtocol.Message) {

        if (message.type == MessageProtocol.DATA) {
            val jsonObject = Parser.default().parse(message.data) as JsonObject
            readMessageListener?.onDataReceived(message.type, jsonObject)
            sendMessage(MessageProtocol.RESPONSE_OK, null)

        }
        if (message.type == MessageProtocol.PING) {
            readMessageListener?.onPingReceived()
            sendMessage(MessageProtocol.PONG, null)
        }
        if (message.type == MessageProtocol.RESPONSE_OK) {
            readMessageListener?.onResponseOkReceived(message.id)
        }

    }

    private fun startReadTask(serialPort: SerialPort): Thread {
        return thread(start = true, isDaemon = false, name = "COMM_BLUETOOTH_READ_THREAD") {
            val inputStream = serialPort.inputStream
            while (true) {
                Thread.sleep(utils.Constants.NONBLOCKING_CYCLE_DELAY)
                try {

                    val available = inputStream?.available() ?: 0
                    if(available <= 0){
                        continue
                    }
                    val buffer = ByteArray(available)
                    val bytes = inputStream?.read(buffer, 0, available) ?: 0

                    messageProtocol.readBytes(buffer)


                } catch (e: IOException) {
                    Log.error("HH", e.localizedMessage)
                } catch (se: SerialPortIOException) {
                    Log.error("HH", se.localizedMessage)

                } catch (ste: SerialPortTimeoutException) {
                    Log.error("HH", ste.localizedMessage)

                }
            }
        }
    }


    fun dispose() {
        closeSockets(true)
    }

    private fun closeSockets(dispose: Boolean) {

    }

    private fun changeConnectionStatus(connectionStatus: ConnectionStatus) {
        mainThread {
            connectionStatusListener?.onRemoteConnectionStatusChanged(connectionStatus)
        }
    }

    fun sendMessage(type: Int, data: JsonObject?): Boolean {
        val message = MessageProtocol.Message(
            0,
            type,
            System.currentTimeMillis(),
            data?.size ?: 0,
            data?.toJsonString() ?: ""
        )
        val packagedMessage = messageProtocol.packMessage(message)
        write(packagedMessage)
        //messageCount++;
        return true
    }

    fun write(message: String) {

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val bytes = message.toByteArray()
                outputStream!!.write(bytes)

            } catch (e: IOException) {
                e.printStackTrace()
                closeSockets(true)
            }
        }
    }

    interface ReadMessageListener {
        fun onDataReceived(type: Int, jsonObject: JsonObject)
        fun onPingReceived()
        fun onResponseOkReceived(messageId: Long)
    }

    interface ConnectionListener {
        fun onRemoteConnectionStatusChanged(status: ConnectionStatus)
    }

}
