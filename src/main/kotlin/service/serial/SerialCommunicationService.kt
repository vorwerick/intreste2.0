package service.serial

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortTimeoutException
import service.serial.protocol.Command
import service.serial.protocol.MessageResolver
import service.serial.protocol.TMsg_EOF
import utils.Constants
import utils.Log
import utils.toHexString
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import javax.usb.UsbDevice
import javax.usb.UsbHostManager
import javax.usb.UsbHub
import kotlin.concurrent.thread


class SerialCommunicationService(private val communicationListener: CommunicationListener) {

    companion object {
        private val executorPool = Executors.newFixedThreadPool(4)
    }

    private val readBuffer = mutableListOf<Byte>()

    private var readThread: Thread? = null
    private var writeThread: Thread? = null
    private var writeQueue: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue<ByteArray>(512)

    private var isConnected = false
    var isInitialized = false

    fun initialize() {
        isInitialized = true
    }

    private fun findUsbDeviceById(hub: UsbHub?, productId: Short): UsbDevice? {
        for (device in hub!!.attachedUsbDevices as List<UsbDevice?>) {
            var d = device
            val desc = d!!.usbDeviceDescriptor
            if (desc.idProduct() == productId) return d
            if (d.isUsbHub) {
                d = findUsbDeviceById(device as UsbHub?, productId)
                if (d != null) return d
            }
        }
        return null
    }

    fun connect() {
        if (!isInitialized) {
            throw CommunicationNotInitializedException()
        }
        val services = UsbHostManager.getUsbServices()
        val device = findUsbDeviceById(services.rootUsbHub, Config.MODULE_PRODUCT_ID)
        Log.info(this.javaClass.name, "Found USB device " + device?.productString)


        val result =
            SerialPort.getCommPorts().firstOrNull { serialPort ->
                Log.info(this.javaClass.name, "Serial port: " +   serialPort.portDescription)
                serialPort.portDescription == device?.productString
            }

        Log.info(this.javaClass.name, "Starting communication " + result?.descriptivePortName)
        if(result != null){
            startCommunication(result)
        }
    }

    fun sendMessage(command: Command) {
        val payload: ByteArray = MessageResolver.createSendPacket(command)
        if (payload.isEmpty()) {
            return
        }
        writeQueue.add(payload)
    }


    private fun startCommunication(serialPort: SerialPort) {
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0)
        serialPort.setComPortParameters(Config.BAUD_RATE, 8, 1, SerialPort.NO_PARITY)
        serialPort.openPort(300)

        Log.info(this.javaClass.name, "Starting tasks")

        startReadTask(serialPort)
        startWriteThread(serialPort)
        isConnected = true
        communicationListener.onConnected()
    }

    private fun startReadTask(serialPort: SerialPort): Thread {
        return thread(start = true, isDaemon = false, name = "COMM_READ_THREAD") {
            val inputStream = serialPort.inputStream
            while (true) {
                Thread.sleep(Constants.NONBLOCKING_CYCLE_DELAY)
                try {
                    val buffer = ByteArray(Config.COMMAND_BYTE_ARRAY_SIZE)
                    val length = inputStream.read(buffer)
                    if (length > 0) {
                        for (index in 0 until length) {
                            val byte = buffer[index]
                            readBuffer.add(byte)
                            if (byte == TMsg_EOF) {
                                val message = readBuffer.toByteArray()
                                val command =
                                    MessageResolver.resolve(message)
                                communicationListener.onCommandReceived(command)
                                readBuffer.clear()
                            }
                        }
                    }
                } catch (e: IOException) {
                    //e.printStackTrace()
                    //closeConnection()
                } catch (e: SerialPortTimeoutException) {
                    //
                }
            }
        }
    }

    private fun startWriteThread(serialPort: SerialPort): Thread {
        return thread(start = true, isDaemon = false, name = "COMM_WRITE_THREAD") {
            val outputStream = serialPort.outputStream
            while (true) {
                Thread.sleep(Constants.NONBLOCKING_CYCLE_DELAY)
                try {
                    Thread.sleep(Config.WRITE_SAFE_DELAY)
                    val payload = writeQueue.take()
                    outputStream.write(payload, 0, payload.size)

                    Log.debug(this.javaClass.name, "Message sent: " + payload.toHexString())
                } catch (e: IOException) {
                    communicationListener.onConnectionLost()
                    Log.warn(this.javaClass.name, e.message)
                    communicationListener.onCommunicationError(e.message ?: "unknow error")
                    Log.warn(this.javaClass.name, e.message)
                    break
                } catch (e: InterruptedException) {
                    communicationListener.onConnectionLost()
                    Log.warn(this.javaClass.name, e.message)
                    break
                }
            }
        }
    }

    fun interrupt() {

    }
}