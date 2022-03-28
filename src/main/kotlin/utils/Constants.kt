package utils

import service.led.ExternalDisplayService

object Constants {


    const val BLUETOOTH_UUID = "ef9e24b8-5981-41d6-9f76-419c16a8df51"
    const val BLUETOOTH_NAME = "Intreste"
    val DEFAULT_LCD_DISPLAY_SIZE = ExternalDisplayService.DisplaySize._96x16
    const val INTRESTE_APP_FILEPATH = "/storage/emulated/0/intreste/"
    const val READY_COUNTDOWN = 4
    const val RESULTS_IDLE_TIME = 20
    const val DEFAULT_GAME_TIMEOUT = 60
    const val DEFAULT_GAME_HIT_POINTS = 1
    const val DEFAULT_GAME_MISSES_POINTS = -2
    const val DEFAULT_BRIGHTNESS = 100
    const val DEFAULT_LOGGING_ENABLED = false
    const val DEFAULT_THRESHOLD = 800
    const val DEFAULT_LCD_DISPLAY_ADDRESS = "192.168.1.216"
    const val DEFAULT_LCD_DISPLAY_PORT = 5000
    const val DEFAULT_LCD_DISPLAY_BRIGHTNESS = 130
    const val DATA_RATE = 20.toByte()
    const val ODR = 2.toByte()
    const val FS = 0.toByte()
    const val MODE = 1.toByte()
    const val DS = 255.toByte()
    const val DEVICE_PRODUCT_ID = 14155
    const val ADDRESS_STM32: Byte = 0x01;
    const val ADDRESS_ANDROID_DEVICE: Byte = 0x00

    const val BAUD_RATE = 115200

    const val DEFAULT_COMM_WRITE_TIMEOUT = 300
    const val DEFAULT_COMM_READ_TIMEOUT = 300

    const val COMMAND_BYTE_ARRAY_SIZE: Int = 1024

    const val WRITE_SAFE_DELAY = 2L

    const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    const val NONBLOCKING_CYCLE_DELAY = 5L
}