package service.app


class StatusMessage(val id: Int, val message: String, val level: Level, val duration: Long) {

    enum class Level {
        INFO, WARNING
    }

    companion object {


        const val CONNECTED_TO_MODULE = 0
        const val PANEL_SORTING_LOADED = 1
        const val DOUBLE_BLOW_WILL_RESTART_GAME = 2
        const val PANELS_NOT_SORTED = 3
        const val PANELS_SORTING_FAILED = 4
        const val PANELS_SORTING_SUCCESS = 5
        const val SETTINGS_WAS_SAVED = 6
        const val PANEL_THRESHOLD_VALUE = 7
        const val PANEL_BRIGHTNESS_VALUE = 8
        const val INVALID_GAME_TIMEOUT = 9
        const val LCD_DISPLAY_CONNECTED = 10
        const val LCD_DISPLAY_DISCONNECTED = 11
        const val LCD_DISPLAY_BRIGHTNESS = 12
        const val SENSORS_COUNT = 13
        const val LCD_DISPLAY_SET_SIZE = 14
        const val REMOTE_DEVICE_CONNECTION_CHANGED = 15


        fun show(id: Int, message: String, level: Level, duration: Long = -1) {
            Service.statusMessagingService.addMessage(StatusMessage(id, message, level, duration))
        }

        fun hide(id: Int) {
            Service.statusMessagingService.removeMessage(id)
        }
    }
}

