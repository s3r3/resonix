package com.resonix.media.core.datastore.serializer

import com.resonix.media.core.model.LoopMode
import com.resonix.media.core.model.PlayerPreferences
import com.resonix.media.core.model.Resume
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerPreferencesSerializerTest {

    @Test
    fun `round trip preserves customised preferences`() {
        runBlocking {
            val preferences = PlayerPreferences(
                resume = Resume.NO,
                playerBrightness = 0.75f,
                defaultPlaybackSpeed = 1.5f,
                loopMode = LoopMode.ONE,
                seekIncrement = 15,
            )
            val output = ByteArrayOutputStream()

            PlayerPreferencesSerializer.writeTo(preferences, output)
            val restored = PlayerPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(preferences, restored)
        }
    }

    @Test
    fun `the default value matches the model defaults`() {
        runBlocking {
            assertEquals(PlayerPreferences(), PlayerPreferencesSerializer.defaultValue)
        }
    }

    @Test
    fun `payloads omitting fields fall back to the model defaults`() {
        runBlocking {
            val payload = """{"defaultPlaybackSpeed":2.0}""".toByteArray()

            val restored = PlayerPreferencesSerializer.readFrom(ByteArrayInputStream(payload))

            assertEquals(2.0f, restored.defaultPlaybackSpeed, TOLERANCE)
            assertEquals(PlayerPreferences(defaultPlaybackSpeed = 2.0f), restored)
        }
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}