package io.confluent.intellijplugin.common.models

import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.LongSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ReadableStringDeserializerTest {
    private val deserializer = ReadableStringDeserializer()

    @Test
    fun `should keep readable text as text, even when it is 4 bytes long`() {
        assertEquals("1234", deserializer.deserialize("t", "1234".toByteArray()))
        assertEquals("ünïcödé", deserializer.deserialize("t", "ünïcödé".toByteArray()))
    }

    @Test
    fun `should show a binary int key as a number`() {
        assertEquals("41241", deserializer.deserialize("t", IntegerSerializer().serialize("t", 41241)))
    }

    @Test
    fun `should show a binary long key as a number`() {
        assertEquals("983655", deserializer.deserialize("t", LongSerializer().serialize("t", 983655L)))
    }

    @Test
    fun `should show other binary data as hex`() {
        assertEquals("0x00ff10", deserializer.deserialize("t", byteArrayOf(0, -1, 16)))
    }

    @Test
    fun `should keep null as null`() {
        assertNull(deserializer.deserialize("t", null))
    }
}
