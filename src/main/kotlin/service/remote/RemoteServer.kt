package service.remote

import Constants
import com.intel.bluetooth.BluetoothConnectionAccessAdapter
import com.intel.bluetooth.BluetoothConsts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.Log
import utils.mainThread
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.bluetooth.LocalDevice
import javax.bluetooth.RemoteDevice
import javax.microedition.io.Connector
import javax.microedition.io.StreamConnection
import javax.microedition.io.StreamConnectionNotifier


class RemoteServer {

    // var serverSocket: BluetoothServerSocket? = null
    // var clientSocket: BluetoothSocket? = null
    var readMessageListener: ReadMessageListener? = null
    var connectionStatusListener: ConnectionListener? = null
    var status = ConnectionStatus.DISCONNECTED

    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null

    var streamConnection: StreamConnection? = null

    var stringbuilder = StringBuilder()

    enum class ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING, DISCONNECTING
    }


    fun start(): Boolean {
        val connectionString =
            "btspp://localhost:${UUID.fromString(Constants.BLUETOOTH_UUID).toString()};name=Sample SPP Server"


        val localDevice =  LocalDevice.getLocalDevice()

        val streamConnectionNotifier: StreamConnectionNotifier =
            Connector.open(connectionString) as StreamConnectionNotifier


        acceptConnectionTask(streamConnectionNotifier)

        return true

    }

    private fun acceptConnectionTask(streamConnectionNotifier: StreamConnectionNotifier) {
        Log.info(Log.MessageGroup.SYSTEM, "Starting accept connection")
        try {
            GlobalScope.launch(Dispatchers.IO) {


                while (true) {
                    Log.info(Log.MessageGroup.SYSTEM, "Waiting for client")
                    try {
                        streamConnection = streamConnectionNotifier.acceptAndOpen()
                        inputStream = streamConnection!!.openInputStream()
                        outputStream = streamConnection!!.openOutputStream()

                        Log.info(
                            Log.MessageGroup.SYSTEM,
                            "Client accepted client connected"
                        )
                        changeConnectionStatus(ConnectionStatus.CONNECTED)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (streamConnection == null) {
                        continue
                    }


                    while (true) {
                        try {
                            val buffer = ByteArray(512)
                            val bytes = inputStream?.read(buffer) ?: 0
                            if (bytes > 0) {
                                val mes = String(buffer.sliceArray(0 until bytes))
                                println("Message received: $mes")
                                if (mes.contains("ping")) {

                                } else {
                                    readMessageListener?.onReadMessage(mes)
                                }
                            } else {
                                break
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            break
                        }
                        changeConnectionStatus(ConnectionStatus.DISCONNECTED)
                    }

                }

            }


        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun startPing() {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1000)
                // write("ping")
            }
        }
    }

    private fun startConnection() {

        GlobalScope.launch(Dispatchers.IO) {
            delay(2000)

        }
    }

    @Throws(IOException::class)
    fun write(message: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val bytes = message.toByteArray()
                outputStream?.write(bytes, 0, bytes.size)
            } catch (e: IOException) {
                e.printStackTrace()
                closeSockets(true)
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

    interface ReadMessageListener {
        fun onReadMessage(message: String)
    }

    interface ConnectionListener {
        fun onRemoteConnectionStatusChanged(status: ConnectionStatus)
    }
}