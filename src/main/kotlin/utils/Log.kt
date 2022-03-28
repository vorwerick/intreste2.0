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
        val group: MessageGroup
    )

    enum class MessageLevel {
        DEBUG, INFO, WARN, ERROR,
    }

    enum class MessageGroup {
        API, HW, CRASH, UI, SYSTEM, GAME, PANEL, UNKNOWN
    }

    fun initialize() {
        val dir = File(Constants.INTRESTE_APP_FILEPATH)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val newFile = File(
            dir, "log-" + DateTimeFormatter
                .ofPattern("dd-MM-HH-mm-ss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now()) + ".txt"
        )
        newFile.createNewFile()
        file = newFile
    }

    private fun writeToFile(message: String) {
        if(fileLoggingEnabled) {
            file?.appendText(message + "\n")
        }
    }

    private var logLevel = MessageLevel.INFO

    companion object {
        val instance = Log()

        public fun debug(group: MessageGroup, message: String?) {
            instance.logMessage(group, message, MessageLevel.DEBUG)
        }

        public fun info(group: MessageGroup, message: String?) {
            instance.logMessage(group, message, MessageLevel.INFO)
        }

        public fun warn(group: MessageGroup, message: String?) {
            instance.logMessage(group, message, MessageLevel.WARN)
        }

        public fun error(group: MessageGroup, message: String?) {
            instance.logMessage(group, message, MessageLevel.ERROR)
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

    private fun logMessage(group: MessageGroup, text: String?, level: MessageLevel) {


        if (text == null) {
            return
        }

        val message = Message(Instant.now(), text, level, group)
        println(message.printString())
        writeToFile(message.printString())
        listeners.forEach {
            it.onMessage(message)
        }
    }


}

fun Log.Message.printString(): String {
    return DateTimeFormatter
        .ofPattern("HH:mm:ss.SSS")
        .withZone(ZoneOffset.UTC)
        .format(timestamp) + " " + level.name + "[" + group.name + "] " + text
}

