package service.serial.protocol

import utils.Log
import utils.toHexString


// TODO: 26/02/2021 REFACTOR!

const val TMsg_EOF: Byte = 0xF0.toByte();
const val TMsg_BS: Byte = 0xF1.toByte();
const val TMsg_BS_EOF: Byte = 0xF2.toByte();

var log = StringBuilder()


open class Command(
    val data: ByteArray = ByteArray(Constants.COMMAND_BYTE_ARRAY_SIZE),
    var length: Int = 0
) {
    init {
        data[0] = Constants.ADDRESS_STM32
        data[1] = Constants.ADDRESS_ANDROID_DEVICE
    }
}

class Commands {

    class Ping : Command() {

        init {
            data[2] = CMD_PING
            length = 3
        }
    }

    class Nack : Command() {
        init {
            data[2] = CMD_NACK
            length = 3
        }
    }

    class GetVersion : Command() {
        init {
            data[2] = CMD_READ_VERSION
            length = 3
        }
    }

    class SetConfiguration(private val odr: Byte, private val fs: Byte, private val mode: Byte, private val ds: Byte) : Command() {
        init {
            data[2] = CMD_CONFIG;
            data[3] = odr
            data[4] = fs
            data[5] = mode
            data[6] = ds
            length = 7;
        }
    }

    class SetThreshold(private val threshold: Int) : Command() {
        init {
            data[2] = CMD_SET_THRESHOLD

            data[3] = (threshold and 0xFF).toByte()
            data[4] = (threshold shr 8 and 0xFF).toByte()
            data[5] = (threshold shr 16 and 0xFF).toByte()
            data[6] = (threshold shr 24 and 0xFF).toByte()
            length = 7
        }
    }

    class Start : Command() {
        init {
            data[2] = CMD_START
            length = 3
        }
    }

    class Stop : Command() {
        init {
            data[2] = CMD_STOP
            length = 3
        }
    }

    class LightUpLED(
        panelId: Byte,
        ledId: Byte,
        color: PanelColor,
        brightnessPercent: Byte,
        duration: Int
    ) : Command() {
        init {
            data[2] = CMD_LED
            data[3] = panelId
            data[4] = ledId
            data[5] = color.r
            data[6] = color.g
            data[7] = color.b
            data[8] = brightnessPercent
            data[9] = (duration and 0xFF).toByte()
            data[10] = (duration shr 8 and 0xFF).toByte()
            length = 11
        }
    }

    class ShowAnimation(
        panelId: Byte,
        animation: Byte,
        color: PanelColor,
        brightnessPercent: Byte,
        speed: Byte,
        repeat: Byte
    ) : Command() {
        init {
            data[2] = CMD_ANIM;
            data[3] = panelId;
            data[4] = animation;
            data[5] = color.r
            data[6] = color.g
            data[7] = color.b
            data[8] = brightnessPercent;
            data[9] = speed;
            data[10] = repeat;
            length = 11
        }
    }

    class ListSensors : Command() {
        val sensors = mutableListOf<Int>()
        init {
            data[2] = CMD_LIST_SENSORS
            length = 3
        }
    }

    class GetFirmwareVersion(sensor: Byte) : Command() {
        init {
            data[2] = CMD_GET_FW_VERSION
            data[3] = sensor
            length = 4
        }
    }


    class GetStats(sensor: Byte) : Command() {
        init {
            data[2] = CMD_GET_STATS
            data[3] = sensor
            length = 4
        }
    }

    class SetDataRate(dataRate: Byte) : Command() {
        init {
            data[2] = CMD_SET_DR
            data[3] = dataRate
            length = 4
        }
    }

    class SensorHit(val value: Int) : Command() {

    }

    class SensorList(val sensors: List<Int>) : Command()

    class Data(val sensors: MutableList<Sensor>) : Command() {
        class Sensor(
            val data: Float,
            val treshold: Float,
            val trigger: Byte,
            val total: Byte,
            val tresholdTrigger: Byte
        ) {}

    }

    companion object {
        const val CMD_PING: Byte = 0x01;
        const val CMD_READ_VERSION: Byte = 0x02;
        const val CMD_NACK: Byte = 0x03;
        const val CMD_START: Byte = 0x04;
        const val CMD_STOP: Byte = 0x05;
        const val CMD_LED: Byte = 0x06;
        const val CMD_ANIM: Byte = 0x07;
        const val CMD_LIST_SENSORS: Byte = 0x08;
        const val CMD_CONFIG: Byte = 0x09;
        const val CMD_TRIGGER: Byte = 0x0A;
        const val CMD_CALIB: Byte = 0x0B;
        const val CMD_SET_THRESHOLD: Byte = 0x0C;
        const val CMD_SET_SERIAL_NUM: Byte = 0x0D;
        const val CMD_GET_FW_VERSION: Byte = 0x0E;
        const val CMD_GET_STATS: Byte = 0x0F;
        const val CMD_SET_DR: Byte = 0x40;
        const val CMD_ERROR: Byte = 0x70;
        private const val CMD_SENSORHIT: Byte = 0x7E
        private const val CMD_DATA: Byte = 0x7F;

        fun serializeCommand(command: Command): Command? {

            when ((command.data[2] - 0x80).toByte()) {
                CMD_READ_VERSION -> println(
                    command.data[3].toString(10) + "." + command.data[4].toString(10) + "." + command.data[5].toString(
                        10
                    )
                )
                CMD_LIST_SENSORS -> {
                    val sensorCount = command.data[3]
                    val sensors = mutableListOf<Int>()
                    for (i in 0 until sensorCount) {
                        val id =
                            command.data[4 * i + 4] + (command.data[4 * i + 5].toInt() shl 8) + (command.data[4 * i + 6].toInt() shl 16) + (command.data[4 * i + 7].toInt() shl 24);

                        sensors.add(id)
                    }
                    return SensorList(sensors)
                }

                CMD_DATA -> {
                    val dataCount = command.data[3]
                    val sensors = mutableListOf<Data.Sensor>()
                    for (i in 0 until dataCount) {
                        val data = (command.data.getUIntAt(4 + 11 * i).toDouble() / 1000f).toFloat()
                        val threshold =
                            (command.data.getUIntAt(8 + 11 * i).toDouble() / 1000f).toFloat()
                        //val data = command.data[4 + 11 * i] +
                        //(command.data[5 + 11 * i].toInt() shl 8) +
                        //    (command.data[6 + 11 * i].toInt() shl 16) +
                        //     (command.data[7 + 11 * i].toInt() shl 24) / 1000f
                        // val threshold = command.data[8 + 11 * i] +
                        //        (command.data[9 + 11 * i].toInt() shl 8) +
                        //      (command.data[10 + 11 * i].toInt() shl 16) +
                        //   (command.data[11 + 11 * i].toInt() shl 24) / 1000f
                        val trigger = command.data[12 + 11 * i]
                        val total = command.data[13 + 11 * i]
                        val thresholdTrigger = command.data[14 + 11 * i]
                        val sensor = Data.Sensor(data, threshold, trigger, total, thresholdTrigger)
                        sensors.add(sensor)
                    }
                    return Data(sensors)
                }
                CMD_SENSORHIT -> {
                    return SensorHit(command.data[3].toInt())
                }
                else -> {
                    Log.debug(Log.MessageGroup.SYSTEM,"Unrecognized command:: ${command.data.toHexString()}")
                }
            }
            return null


        }

        fun ByteArray.toInt(): Int {
            var result = 0
            for (i in this.indices) {
                result = result or (this[i].toInt() shl 8 * i)
            }
            return result
        }


        private infix fun Byte.shiftLeft(i: Int): Byte {
            return (this.toInt() shl i).toByte()
        }

        private fun ByteArray.getUIntAt(idx: Int) =
            ((this[idx].toUInt() and 0xFFu) shl 24) or
                    ((this[idx + 1].toUInt() and 0xFFu) shl 16) or
                    ((this[idx + 2].toUInt() and 0xFFu) shl 8) or
                    (this[idx + 3].toUInt() and 0xFFu)

        fun intToBytes(value: Int): ByteArray {
            return byteArrayOf(
                (value shr 24).toByte(),
                (value shr 16).toByte(),
                (value shr 8).toByte(),
                value.toByte()
            )
        }
    }

    class PanelColor(val r: Byte, val g: Byte, val b: Byte) {
        companion object {
            val PURPLE = PanelColor(255.toByte(), 0, 255.toByte())
            val BLUE = PanelColor(0, 0, 255.toByte())
            val BLACK = PanelColor(0, 0, 0)
            val RED = PanelColor(255.toByte(), 0, 0)
            val GREEN = PanelColor(0, 255.toByte(), 0)
            val YELLOW = PanelColor(255.toByte(), 255.toByte(), 0)
        }

        override fun toString(): String {
            return "($r, $g, $b)"
        }
    }


}

