package service.serial.protocol

import utils.Log

object MessageResolver {

    private const val TMsg_EOF: Byte = 0xF0.toByte();
    private const val TMsg_BS: Byte = 0xF1.toByte();
    private const val TMsg_BS_EOF: Byte = 0xF2.toByte();

    fun createSendPacket(command: Command): ByteArray {

        val totalLen = checkSum(command.length, command.data)
        val packet = ByteArray(256)
        val check = byteStuffCopy(packet, totalLen, command.data);
        if (check == -10) {
            return ByteArray(0)
        }

        val finalPacket = ByteArray(check)
        for (i in 0 until check) {
            finalPacket[i] = packet[i]
        }
        return finalPacket
    }

    fun resolve(packet: ByteArray): Command? {
        val command = Command()
        packet.forEachIndexed { index: Int, byte: Byte ->
            if (byte != TMsg_BS) {
                command.data[index] = byte

            }
            checkAndRemove(command)

        }

        return Commands.serializeCommand(command)
    }

    private fun checkAndRemove(command: Command): Int {

        var chk: Int = 0
        var i = 0


        while (i < command.length) {
            chk += command.data[i]
            i++
        }

        command.length--
        return 0

    }

    private fun checkSum(len: Int, buffer: ByteArray): Int {
        var chk: Int = 0
        var i = 0


        while (i < len) {
            chk -= buffer[i]
            i++
        }

        buffer[i] = chk.toByte()
        return len + 1
    }

    private fun byteStuffCopy(dest: ByteArray, len: Int, buffer: ByteArray): Int {
        var i: Int = 0
        var count = 0

        while (i < len) {
            count += byteStuffCopyByte(dest, count, buffer[i]);
            i += 1
        }

        if (count >= dest.size) {
            Log.error(
                this.javaClass.name,
                "Index " + count + " is higher than array " + dest.size + " - skipping command -> " + dest.toHexString()
            )
            return -10
        }
        dest[count] = TMsg_EOF;
        count += 1
        return count
    }

    private fun byteStuffCopyByte(dest: ByteArray, destIndex: Int, source: Byte): Int {
        var ret = 2;

        when (source) {
            TMsg_EOF -> {
                dest[destIndex + 0] = TMsg_BS;
                dest[destIndex + 1] = TMsg_BS_EOF
            }
            TMsg_BS -> {
                dest[destIndex + 0] = TMsg_BS;
                dest[destIndex + 1] = TMsg_BS;
            }
            else -> {
                dest[destIndex + 0] = source;
                ret = 1;
            }
        }
        return ret;
    }

    fun ByteArray.toHexString() = joinToString(" ") { "%02x".format(it) }
}