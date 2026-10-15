package io.confluent.intellijplugin.common.models

import org.apache.kafka.common.serialization.Deserializer
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 * Deserializer for the "String" type that doesn't show binary data as "�N".
 * Readable UTF-8 stays text; otherwise a 4/8-byte payload is shown as the int/long that
 * IntegerSerializer/LongSerializer would have written (the usual case for numeric keys),
 * and anything else as hex.
 */
class ReadableStringDeserializer : Deserializer<String> {
    override fun deserialize(topic: String?, data: ByteArray?): String? {
        if (data == null) return null
        decodeUtf8(data)?.takeIf { it.isReadable() }?.let { return it }
        return when (data.size) {
            Int.SIZE_BYTES -> ByteBuffer.wrap(data).int.toString()
            Long.SIZE_BYTES -> ByteBuffer.wrap(data).long.toString()
            else -> "0x" + data.joinToString("") { "%02x".format(it) }
        }
    }

    private fun decodeUtf8(data: ByteArray): String? = try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(data))
            .toString()
    } catch (_: CharacterCodingException) {
        null
    }

    private fun String.isReadable() = none { it.isISOControl() && it != '\n' && it != '\r' && it != '\t' }
}
