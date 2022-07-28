package service.remote

import Constants
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


    var stringbuilder = StringBuilder()

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


        return try {
            serialPort.baudRate = Constants.HC_05_BT_MODULE_BAUD
            serialPort.openPort()
            Log.info(this.javaClass.canonicalName, "Opening serial port for communication")

            startReadTask(serialPort)
            //startWriteTask(serialPort)

            true

        } catch (e: IOException) {
            Log.error(this.javaClass.canonicalName, e.localizedMessage)
            false
        }

    }

    private fun startReadTask(serialPort: SerialPort): Thread {
        return thread(start = true, isDaemon = false, name = "COMM_BLUETOOTH_READ_THREAD") {
            val inputStream = serialPort.inputStream
            val readBuffer = mutableListOf<Byte>()
            var reading = false
            while (true) {
                Thread.sleep(utils.Constants.NONBLOCKING_CYCLE_DELAY)
                try {

                    val available = inputStream?.available() ?: 0
                    if(available <= 0){
                        continue
                    }
                    val buffer = ByteArray(available)
                    val bytes = inputStream?.read(buffer, 0, available) ?: 0

                    val message = String(buffer)
                    Log.info("REMOTE READ", message)
                    if (message.contains(MessageProtocol.START_CHAR)) {
                        reading = true
                    }
                    if (reading) {
                        readBuffer.addAll(message.toByteArray().toMutableList())
                        if ((message.contains(MessageProtocol.END_CHAR))) {
                            readMessageListener?.onReadMessage(String(readBuffer.toByteArray()))
                            readBuffer.clear()
                            reading = false
                        }
                    }

                } catch (e: IOException) {

                } catch (se: SerialPortIOException) {

                } catch (ste: SerialPortTimeoutException) {

                }
            }
        }
    }


    private fun startConnection() {

        GlobalScope.launch(Dispatchers.IO) {
            delay(2000)


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

    fun write(message: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val bytes = message.toByteArray()
                outputStream?.write(bytes)
                outputStream?.flush()
                Log.info(this.javaClass.canonicalName, "Message sent $message")

            } catch (e: IOException) {
                e.printStackTrace()
                closeSockets(true)
            }
        }
    }

    interface ReadMessageListener {
        fun onReadMessage(message: String)
    }

    interface ConnectionListener {
        fun onRemoteConnectionStatusChanged(status: ConnectionStatus)
    }

}
