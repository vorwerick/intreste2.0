package utils

fun ByteArray.toHexString() = joinToString(" ") { "%02x".format(it) }