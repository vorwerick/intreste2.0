package service.remote.protocol

import utils.Log

class MessageProtocol {

    private val inputBuffer = mutableListOf<String>()
    var readMessageCallback: ((message: Message) -> Unit)? = null

    companion object {
        const val DATA = 0
        const val CONNECT = 1
        const val DISCONNECT = 2
        const val PING = 3
        const val RESPONSE_OK = 10
        const val RESPONSE_ERROR = 20

        const val START_CHAR = "^"
        const val END_CHAR = "&"
        const val SPLIT_CHAR = ";"
        const val NUMBER_OF_PARAMETERS =
            7 // start char, id, type, timestamp, data length,data, end char
    }

    // ^;612;10;192494924;3000;{};&
    fun packMessage(message: Message): String {
        val builder = StringBuilder()
        builder.append(START_CHAR).append(message.id).append(message.type).append(message.timestamp)
            .append(message.dataLength).append(message.data).append(END_CHAR)
        Log.info("HOO", builder.toString())
        return builder.toString()
    }

    fun readBytes(bytes: ByteArray) {
        bytes.forEach { b ->
            val char = b.toString()
            inputBuffer.add(char)
            if (char == END_CHAR) {
                val indexOfStartChar = inputBuffer.indexOfLast { e -> e == START_CHAR }
                if (indexOfStartChar >= 0) {
                    val block =
                        inputBuffer.slice(indexOfStartChar until inputBuffer.size).joinToString("")
                    // message is ok
                    Log.info("HOO", block)
                    try {
                        val message = checkMessage(block)
                        readMessageCallback?.invoke(message)
                    } catch (e: IllegalStateException) {
                        Log.error("HOO", e.localizedMessage ?: "unknown error")
                    }
                }
                inputBuffer.clear()
            }
        }
    }

    // ^;612;10;192494924;3000;{};&
    private fun checkMessage(message: String): Message {
        val parameters = message.split(SPLIT_CHAR)
        val parametersCount = parameters.size


        if (parametersCount != NUMBER_OF_PARAMETERS) {
            throw IllegalStateException("Invalid message - less or more than $NUMBER_OF_PARAMETERS parameters")
        }
        if (parameters[0] != START_CHAR) {
            throw IllegalStateException("Invalid message - invalid start char")
        }
        if (parameters[parametersCount - 1] != END_CHAR) {
            throw IllegalStateException("Invalid message - invalid end char")
        }

        val id: Long? = parameters[1].toLongOrNull(10)
        val type: Int? = parameters[2].toIntOrNull(10)
        val timestamp: Long? = parameters[3].toLongOrNull(10)
        val dataLength: Int? = parameters[4].toIntOrNull(10)
        val data: String = parameters[5]

        if (id == null || type == null || timestamp == null || dataLength == null) {
            throw IllegalStateException("Invalid message - parameters are invalid")
        }
        if (data.toByteArray().size != dataLength) {
            throw IllegalStateException("Invalid message - data length is invalid")
        }

        return Message(id, type, timestamp, dataLength, data)
    }

    class Message(
        val id: Long,
        val type: Int,
        val timestamp: Long,
        val dataLength: Int,
        val data: String
    )
}
