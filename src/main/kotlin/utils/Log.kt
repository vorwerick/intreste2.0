package utils

import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Log {

    private var file: File? = null

    var fileLoggingEnabled = true

    interface MessageListener {
        fun onMessage(message: Message)
    }

    data class Message(
        val timestamp: Instant,
        val text: String,
        val level: MessageLevel,
        val className: String,
    )

    enum class MessageLevel {
        DEBUG, INFO, WARN, ERROR,
    }

    fun initialize() {
        val dir = File("/etc/intrestelogs")
        if (!dir.exists()) {
            dir.mkdir()
        }
        println(dir.absolutePath)
        val newFile = File(
            dir, "log-" + DateTimeFormatter
                .ofPattern("HHmm-ddMMyy")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now()) + ".txt"
        )
        newFile.createNewFile()
        println(newFile.absolutePath)
        file = newFile
    }

    private fun writeToFile(message: String) {
        file!!.appendText(message + "\n")

    }

    private var logLevel = MessageLevel.INFO

    companion object {
        val instance = Log()

        public fun debug(className: String, message: String?) {
            instance.logMessage(className, message, MessageLevel.DEBUG)
        }

        public fun info(className: String, message: String?) {
            instance.logMessage(className, message, MessageLevel.INFO)
        }

        public fun warn(className: String, message: String?) {
            instance.logMessage(className, message, MessageLevel.WARN)
        }

        public fun error(className: String, message: String?) {
            instance.logMessage(className, message, MessageLevel.ERROR)
        }
    }

    private val listeners = mutableListOf<MessageListener>()

    fun setLogLevel(level: MessageLevel) {
        logLevel = level
    }

    fun addMessageListener(listener: MessageListener) {
        listeners.add(listener)
    }

    fun removeMessageListener(listener: MessageListener) {
        listeners.remove(listener)
    }

    private fun logMessage(className: String, text: String?, level: MessageLevel) {

        if (text == null) {
            return
        }

        val message = Message(Instant.now(), text, level, className)
        println(message.printString())
        writeToFile(message.printString())
        listeners.forEach {
            it.onMessage(message)
        }
    }


}

fun Log.Message.printString(): String {
    return DateTimeFormatter
        .ofPattern("dd/mm/yy HH:mm:ss.SSS")
        .withZone(ZoneOffset.UTC)
        .format(timestamp) + " " + "["+ level.name+  "] "+ className + " > " + text
}

