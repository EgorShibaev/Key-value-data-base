import java.io.File

fun UInt.toBytes(): List<UByte> {
    val bitsRepresentation = toString(2).padStart(32, '0')
    val byte1 = bitsRepresentation.substring(0 until 8).toUByte(2)
    val byte2 = bitsRepresentation.substring(8 until 16).toUByte(2)
    val byte3 = bitsRepresentation.substring(16 until 24).toUByte(2)
    val byte4 = bitsRepresentation.substring(24 until 32).toUByte(2)
    return listOf(byte1, byte2, byte3, byte4)
}

fun fromUBytesToUInt(byte1: UByte, byte2: UByte, byte3: UByte, byte4: UByte): UInt {
    val str1 = byte1.toString(2).padStart(8, '0')
    val str2 = byte2.toString(2).padStart(8, '0')
    val str3 = byte3.toString(2).padStart(8, '0')
    val str4 = byte4.toString(2).padStart(8, '0')
    return (str1 + str2 + str3 + str4).toUInt(2)
}

fun Char.toBytes(): List<UByte> {
    val bitsRepresentation = code.toUShort().toString(2).padStart(16, '0')
    val byte1 = bitsRepresentation.substring(0 until 8).toUByte(2)
    val byte2 = bitsRepresentation.substring(8 until 16).toUByte(2)
    return listOf(byte1, byte2)
}

fun fromUBytesToChar(byte1: UByte, byte2: UByte): Char {
    val str1 = byte1.toString(2).padStart(8, '0')
    val str2 = byte2.toString(2).padStart(8, '0')
    return (str1 + str2).toUShort(2).toInt().toChar()
}

fun readBase(): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val content = File("database").readBytes().map { it.toUByte() }
    var i = 0
    while (i < content.size) {
        val keySize = fromUBytesToUInt(content[i], content[i + 1], content[i + 2], content[i + 3]).toInt()
        i += 4
        val valueSize = fromUBytesToUInt(content[i], content[i + 1], content[i + 2], content[i + 3]).toInt()
        i += 4
        val key = List(keySize) { fromUBytesToChar(content[i + it * 2], content[i + it * 2 + 1]) }
        i += keySize * 2
        val value = List(valueSize) { fromUBytesToChar(content[i + it * 2], content[i + it * 2 + 1]) }
        i += valueSize * 2
        result[key.joinToString(separator = "")] = value.joinToString(separator = "")
    }
    return result
}

fun writeToBase(content: Map<String, String>) {
    val result = content.map {
        val res = mutableListOf<UByte>()
        res.addAll(it.key.length.toUInt().toBytes())
        res.addAll(it.value.length.toUInt().toBytes())
        res.addAll(it.key.map { ch -> ch.toBytes() }.flatten())
        res.addAll(it.value.map { ch -> ch.toBytes() }.flatten())
        res
    }.flatten().map { it.toByte() }

    File("database").writeBytes(result.toByteArray())
}
