package service.app

import utils.mainThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.Log

class StatusMessagingService {

    interface StatusMessageListener {
        fun onMessageAdded(statusMessage: StatusMessage)
        fun onMessageRemoved(statusMessage: StatusMessage)
    }

    init {
        Log.info(this.javaClass.name, "Status messaging service init")
    }

    private val statusMessageListeners = mutableListOf<StatusMessageListener>()

    private val statusMessages = mutableListOf<StatusMessage>()


    fun addMessage(statusMessage: StatusMessage) {
        statusMessages.add(statusMessage)
        mainThread { statusMessageListeners.forEach { it.onMessageAdded(statusMessage) } }
        if (statusMessage.duration > 0) {
            GlobalScope.launch {
                delay(statusMessage.duration)
                statusMessages.remove(statusMessage)
                mainThread { statusMessageListeners.forEach { it.onMessageRemoved(statusMessage) } }
            }
        }
    }

    fun removeMessage(id: Int) {
        val message = statusMessages.firstOrNull { statusMessage -> statusMessage.id == id }
        message?.let { s ->
            statusMessages.remove(s)
            mainThread { statusMessageListeners.forEach { it.onMessageRemoved(s) } }
        }
    }

    fun addStatusMessageListener(statusMessageListener: StatusMessageListener) {
        statusMessageListeners.add(statusMessageListener)
    }

    fun removeStatusMessageListener(statusMessageListener: StatusMessageListener) {
        statusMessageListeners.remove(statusMessageListener)
    }
}