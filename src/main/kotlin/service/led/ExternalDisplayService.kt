package service.led

import utils.mainThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ExternalDisplayService {

    init {
        Log.info(Log.MessageGroup.SYSTEM, "External display service init")
    }

    enum class DisplaySize {
        _96x16, _96x48_TYP1
    }

    companion object {

        const val START_MESSAGE = 0x02
        const val INTERNAL_ADDRESS = 0x01
        const val END_MESSAGE = 0x03

        const val BLOCK_END = 0x17

        const val RED = "C0065"
        const val YELLOW = "C0129"
        const val GREEN = "C0001"

        const val RED_MAX_FONT = "C097"
        const val YELLOW_MAX_FONT = "C161"
        const val GREEN_MAX_FONT = "C033"

        const val UTF = "UTF"

        const val RECT_SCORE_DISPLAY_96x16 = "N00"
        const val RECT_TIME_DISPLAY_96x16 = "N01"
        const val RECT_ANIMATION_DISPLAY_96x16 = "N02"
        const val RECT_FULL_DISPLAY_96x16 = "N03"
        const val RECT_LEFT_DISPLAY_96x16 = "N04"

        const val R_96x48_FIRST = "N00"
        const val R_96x48_SECOND = "N01"
        const val R_96x48_THIRD = "N02"


        const val CLEAR_RECT = "EC"
        const val DEFINE_RECT = "DW"
        const val EDIT_RECT = "EX"
        const val SET_BRIGHTNESS = "TB"
        const val DRAW_LINE = "LG"

        const val TEXT_ALIGN_RIGHT = "B1"
        const val TEXT_ALIGN_CENTER = "B2"
        const val TEXT_ALIGN_LEFT = "B3"

        const val FONT_CLASSIC = "F6"
        const val FONT_CLASSIC_SMALLER = "F7"
        const val FONT_32_BOLD = "F8"
        const val FONT_32 = "F7"
        const val FONT_24_BOLD = "F6"
        const val FONT_24 = "F5"
        const val FONT_MEDIUM = "F1"
        const val FONT_SMALL = "F0"
    }

    private var lcdDisplayListener: LCDDisplayListener? = null

    private var executor: ExecutorService = Executors.newFixedThreadPool(2)

    @Volatile
    var client: Socket? = null

    @Volatile
    private var animationDuration = -1

    init {
        GlobalScope.launch {
            while (true) {
                try {
                    delay(100)
                    animationDuration -= 1
                    if (animationDuration <= -1) {
                        animationDuration = -1
                    }
                    if (animationDuration == 0) {
                        clearDisplay(RECT_ANIMATION_DISPLAY_96x16)
                        clearDisplay(R_96x48_THIRD)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun showMessageSortPanels(){
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                showTextOnDisplay(RECT_FULL_DISPLAY_96x16, "SORT!", RED, FONT_CLASSIC)
            }
            DisplaySize._96x48_TYP1 -> {
                clearDisplay(R_96x48_FIRST)
                clearDisplay(R_96x48_THIRD)
                showTextOnDisplay(R_96x48_SECOND, "SORT PANELS!", RED, FONT_CLASSIC_SMALLER)
            }
        }
    }


    fun updateFullMessage(message: String, color: String) {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                showTextOnDisplay(RECT_FULL_DISPLAY_96x16, message, color, FONT_CLASSIC)
            }
            DisplaySize._96x48_TYP1 -> {
                clearDisplay(R_96x48_FIRST)
                clearDisplay(R_96x48_THIRD)
                showTextOnDisplay(R_96x48_SECOND, message, color, FONT_CLASSIC)
            }
        }
    }

    fun clearFullMessage() {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                clearDisplay(RECT_FULL_DISPLAY_96x16)
            }
            DisplaySize._96x48_TYP1 -> {
                clearDisplay(R_96x48_SECOND)
            }
        }

    }

    fun stopAnimation() {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                animationDuration = -1
                clearDisplay(RECT_ANIMATION_DISPLAY_96x16)
            }
            DisplaySize._96x48_TYP1 -> {
                clearDisplay(R_96x48_THIRD)
                animationDuration = -1
                //drawScoreLine(4)
            }
        }

    }

    fun showAnimation(hit: Boolean, score: String = "") {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                if (hit) {
                    showTextOnDisplay(RECT_ANIMATION_DISPLAY_96x16, "OK", YELLOW, FONT_CLASSIC)
                } else {
                    showTextOnDisplay(RECT_ANIMATION_DISPLAY_96x16, "X", RED, FONT_CLASSIC)
                }
                animationDuration = 8
            }
            DisplaySize._96x48_TYP1 -> {
                if (hit) {
                    showTextOnDisplay(
                        R_96x48_THIRD,
                        "OK",
                        GREEN,
                        FONT_CLASSIC_SMALLER
                    )
                } else {
                    showTextOnDisplay(
                        R_96x48_THIRD,
                        "XXX",
                        RED,
                        FONT_CLASSIC_SMALLER
                    )
                }
                animationDuration = 8

                /** if (hit) {
                drawScoreLine(0)
                } else {
                drawScoreLine(1)
                }
                animationDuration = 8 */
            }

        }

    }


    fun updateScore(score: Int) {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                if (score >= 0) {
                    showTextOnDisplay(
                        RECT_SCORE_DISPLAY_96x16,
                        score.toString(),
                        GREEN,
                        FONT_CLASSIC
                    )
                } else {
                    showTextOnDisplay(RECT_SCORE_DISPLAY_96x16, score.toString(), RED, FONT_CLASSIC)
                }
            }
            DisplaySize._96x48_TYP1 -> {
                if (score >= 0) {
                    showTextOnDisplay(
                        R_96x48_FIRST,
                        "SCORE $score",
                        GREEN,
                        FONT_CLASSIC_SMALLER
                    )
                } else {
                    showTextOnDisplay(
                        R_96x48_FIRST,
                        "SCORE $score",
                        RED,
                        FONT_CLASSIC_SMALLER
                    )
                }
            }

        }
    }

    fun clearScore() {
        clearDisplay(RECT_SCORE_DISPLAY_96x16)
    }

    fun updateTime(time: Int) {

        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                val timeString = "${time}s"
                showTextOnDisplay(RECT_TIME_DISPLAY_96x16, timeString, YELLOW, FONT_CLASSIC)
            }
            DisplaySize._96x48_TYP1 -> {
                val timeString = "TIME ${time}s"
                showTextOnDisplay(
                    R_96x48_SECOND,
                    timeString,
                    YELLOW,
                    FONT_CLASSIC_SMALLER
                )
            }

        }
    }

    fun clearTime() {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                clearDisplay(RECT_TIME_DISPLAY_96x16)
            }
            DisplaySize._96x48_TYP1 -> {
                //clearDisplay(R_96x48_FIRST)
            }

        }
    }

    fun showScoreTitle(score: Int) = when (Service.settingsService.externalDisplaySize) {
        DisplaySize._96x16 -> {
            showTextOnDisplay(RECT_LEFT_DISPLAY_96x16, "SCORE", YELLOW, FONT_CLASSIC)
        }
        DisplaySize._96x48_TYP1 -> {
            showTextOnDisplay(
                R_96x48_FIRST,
                "FINAL", /// result is biggest
                YELLOW,
                FONT_CLASSIC_SMALLER
            )
            showTextOnDisplay(
                R_96x48_SECOND,
                "SCORE", /// result is biggest
                YELLOW,
                FONT_CLASSIC_SMALLER
            )
            showTextOnDisplay(
                R_96x48_THIRD,
                score.toString(), /// result is biggest
                when {
                    score < 0 -> RED
                    score > 0 -> GREEN
                    else -> YELLOW
                },
                FONT_CLASSIC_SMALLER
            )
        }

    }

    fun clearScoreTitle() {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                clearDisplay(RECT_LEFT_DISPLAY_96x16)
            }
        }
    }

    fun connect(address: String, port: Int) {
        executor.submit {
            try {
                client = Socket()
                client?.connect(
                    InetSocketAddress(
                        address,
                        port
                    ), 5000
                )
                Log.info(Log.MessageGroup.HW, "Connected to LCD display")

                Thread.sleep(100)
                defineDisplays()
                Thread.sleep(100)
                clearAll()
                Thread.sleep(100)
                testDisplay()
                mainThread {
                    Service.settingsService.lcdDisplayConnected = true
                   lcdDisplayListener?.onLCDDisplayConnected()
                }
            } catch (e: IOException) {
                Log.error(Log.MessageGroup.HW, e.message)
                e.message?.let { disconnect(it) }
            }
        }
    }

    fun disconnect(reason: String) {
        if (client == null) {
            return
        }
        try {
            val outputStream = client?.getOutputStream()
            outputStream?.close()
        } catch (e: IOException) {
            Log.error(Log.MessageGroup.HW, e.message)
        }
        client?.close()
        client = null
        Log.info(Log.MessageGroup.HW, "Disconnected from LCD display")
        mainThread {
            Service.settingsService.lcdDisplayConnected = false
            lcdDisplayListener?.onLCDDisplayDisconnected(reason)
        }
    }

    fun defineDisplays() {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                defineDisplay(RECT_SCORE_DISPLAY_96x16, 0, 0, 33, 16, TEXT_ALIGN_CENTER)
                defineDisplay(RECT_TIME_DISPLAY_96x16, 60, 0, 96, 16, TEXT_ALIGN_CENTER)
                defineDisplay(RECT_ANIMATION_DISPLAY_96x16, 34, 0, 59, 16, TEXT_ALIGN_CENTER)
                defineDisplay(RECT_FULL_DISPLAY_96x16, 0, 0, 96, 16, TEXT_ALIGN_CENTER)
                defineDisplay(RECT_LEFT_DISPLAY_96x16, 34, 0, 96, 16, TEXT_ALIGN_CENTER)
            }
            DisplaySize._96x48_TYP1 -> {
                defineDisplay(R_96x48_FIRST, 0, 0, 96, 16, TEXT_ALIGN_CENTER)
                defineDisplay(R_96x48_SECOND, 0, 16, 96, 32, TEXT_ALIGN_CENTER)
                defineDisplay(R_96x48_THIRD, 0, 32, 96, 48, TEXT_ALIGN_CENTER)
            }

        }

    }

    fun testDisplay() {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                GlobalScope.launch {
                    showTextOnDisplay(RECT_SCORE_DISPLAY_96x16, "TEST", GREEN, FONT_CLASSIC)
                    delay(200)
                    showTextOnDisplay(RECT_TIME_DISPLAY_96x16, "TEST", RED, FONT_CLASSIC)
                    delay(200)
                    showTextOnDisplay(RECT_ANIMATION_DISPLAY_96x16, "TEST", YELLOW, FONT_SMALL)
                    delay(1300)
                    clearAll()
                }
            }
            DisplaySize._96x48_TYP1 -> {
                GlobalScope.launch {
                    showTextOnDisplay(R_96x48_FIRST, "CONNECTED", GREEN, FONT_CLASSIC_SMALLER)
                    delay(50)
                    showTextOnDisplay(R_96x48_SECOND, "ABCDEFGHIJ", RED, FONT_CLASSIC_SMALLER)
                    delay(50)
                    showTextOnDisplay(R_96x48_THIRD, "1234567890", YELLOW, FONT_CLASSIC_SMALLER)
                    delay(1000)
                    showTextOnDisplay(R_96x48_FIRST, "TESTTESTTEST", RED, FONT_CLASSIC)
                    delay(50)
                    showTextOnDisplay(R_96x48_SECOND, "><–./([{$#@~^", YELLOW, FONT_CLASSIC)
                    delay(50)
                    showTextOnDisplay(R_96x48_THIRD, "&*%;'>_+|§])}", GREEN, FONT_CLASSIC)
                    delay(1000)
                    clearAll()
                }
            }

        }

    }


    private fun clearDisplay(displayId: String) {
        val message =
            createMessage(mutableListOf<String>(CLEAR_RECT + displayId), UTF)
        sendData(message.toByteArray())
    }

    fun setBrightnessValue(brightness: Int) {
        val message =
            createMessage(mutableListOf<String>(SET_BRIGHTNESS + "B" + brightness.toString()), UTF)
        sendData(message.toByteArray())
    }

    private fun defineDisplay(
        displayId: String,
        x: Int,
        y: Int,
        x2: Int,
        y2: Int,
        TEXT_ALIGN_RIGHT: String
    ) {
        val padX = x.toString().padStart(4, '0')
        val padY = y.toString().padStart(4, '0')
        val padWidth = x2.toString().padStart(4, '0')
        val padHeight = y2.toString().padStart(4, '0')
        val message =
            createMessage(
                mutableListOf<String>(DEFINE_RECT + displayId + "P" + padX + padY + "E" + padWidth + padHeight + TEXT_ALIGN_CENTER),
                UTF
            )
        sendData(message.toByteArray())
    }

    private fun drawScoreLine(color: Int) {
        val message =
            createMessage(
                mutableListOf<String>(
                    "${DRAW_LINE}O0A000B096C000B${color}",
                    "${DRAW_LINE}O0A000B096C001B${color}",
                    "${DRAW_LINE}O0A000B096C047B${color}",
                    "${DRAW_LINE}O0A000B096C048B${color}",
                    "${DRAW_LINE}O1A000B048C000B${color}",
                    "${DRAW_LINE}O1A000B048C001B${color}",
                    "${DRAW_LINE}O1A000B048C095B${color}",
                    "${DRAW_LINE}O1A000B048C096B${color}",
                ),
                UTF
            )
        sendData(message.toByteArray())
    }

    private fun showTextOnDisplay(displayId: String, text: String, color: String, font: String) {
        val message =
            createMessage(
                mutableListOf<String>("$EDIT_RECT${displayId}${color}${font}T$text"),
                UTF
            )
        sendData(message.toByteArray())
    }

    private fun sendData(data: ByteArray) {
        if (client == null) {
            Log.warn(Log.MessageGroup.HW, "Cannot send LCD display message, not connected")
            return
        }
        Log.debug(Log.MessageGroup.HW, "LED display message: $data")
        try {
            executor.submit {
                val outputStream = client?.getOutputStream()
                outputStream?.write(data)
                outputStream?.flush()
            }
        } catch (e: IOException) {
            Log.error(Log.MessageGroup.HW, e.message)
            e.message?.let { disconnect(it) }
        }
    }

    private fun createMessage(d: MutableList<String>, code: String): MutableList<Byte> {
        val datagrams = d.toTypedArray()
        val bytes = mutableListOf<Byte>()
        bytes.add(0, START_MESSAGE.toByte())
        bytes.add(1, INTERNAL_ADDRESS.toByte())
        var index = 2
        var dataCount = 1
        for (datagram in datagrams) {
            bytes.add(index, dataCount.toByte())
            index++

            var data = ByteArray(10)
            data = if (code == UTF) {
                datagram.toByteArray()
            } else {
                datagram.toByteArray(Charset.forName("windows-1250"))
            }
            for (byte in data) {
                bytes.add(index, byte)
                index++
            }

            bytes.add(index, BLOCK_END.toByte())
            index++
            dataCount++
        }
        bytes.add(index, END_MESSAGE.toByte())

        return bytes
    }

    fun setLCDDisplayListener(lcdDisplayListener: LCDDisplayListener?) {
        this.lcdDisplayListener = lcdDisplayListener
    }

    fun clearAll() {
        when (Service.settingsService.externalDisplaySize) {
            DisplaySize._96x16 -> {
                clearDisplay(RECT_ANIMATION_DISPLAY_96x16)
                clearDisplay(RECT_FULL_DISPLAY_96x16)
                clearDisplay(RECT_SCORE_DISPLAY_96x16)
                clearDisplay(RECT_TIME_DISPLAY_96x16)
                clearDisplay(RECT_LEFT_DISPLAY_96x16)
            }
            DisplaySize._96x48_TYP1 -> {
                clearDisplay(R_96x48_FIRST)
                clearDisplay(R_96x48_SECOND)
                clearDisplay(R_96x48_THIRD)
            }
        }

        animationDuration = -1
    }

}