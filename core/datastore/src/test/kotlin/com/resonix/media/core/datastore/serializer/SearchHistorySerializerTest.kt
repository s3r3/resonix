package com.resonix.media.core.datastore.serializer

import androidx.datastore.core.CorruptionException
import com.resonix.media.core.model.SearchHistory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHistorySerializerTest {

    @Test
    fun `round trip preserves the stored queries`() {
        runBlocking {
            val history = SearchHistory().addQuery("kotlin").addQuery("compose")
            val output = ByteArrayOutputStream()

            SearchHistorySerializer.writeTo(history, output)
            val restored = SearchHistorySerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(history, restored)
        }
    }

    @Test
    fun `an empty payload is reported as corruption so the default can be restored`() {
        runBlocking {
            val payload = ByteArray(0)

            val failure = runCatching {
                SearchHistorySerializer.readFrom(ByteArrayInputStream(payload))
            }.exceptionOrNull()

            assertTrue(failure is CorruptionException)
        }
    }

    @Test
    fun `unknown keys are ignored so older payloads stay readable`() {
        runBlocking {
            val payload = """{"queries":["kotlin"],"removedInAFutureVersion":true}""".toByteArray()

            assertEquals(SearchHistory(listOf("kotlin")), SearchHistorySerializer.readFrom(ByteArrayInputStream(payload)))
        }
    }

    @Test
    fun `malformed payloads are reported as corruption`() {
        runBlocking {
            val payload = "{ this is not json".toByteArray()

            val failure = runCatching { SearchHistorySerializer.readFrom(ByteArrayInputStream(payload)) }.exceptionOrNull()

            assertTrue(failure is CorruptionException)
        }
    }
}