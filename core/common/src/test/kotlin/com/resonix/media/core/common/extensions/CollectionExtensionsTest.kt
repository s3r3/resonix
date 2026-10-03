package com.resonix.media.core.common.extensions

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionExtensionsTest {

    @Test
    fun `mapAsync preserves the order of the source list`() {
        runBlocking {
            val result = listOf(1, 2, 3, 4).mapAsync { value -> value * 2 }
            assertEquals(listOf(2, 4, 6, 8), result)
        }
    }

    @Test
    fun `mapAsync transforms elements concurrently`() {
        runBlocking {
            val result = listOf("a", "b").mapAsync { value -> value.uppercase() }
            assertEquals(listOf("A", "B"), result)
        }
    }

    @Test
    fun `mapAsync returns an empty list for an empty source`() {
        runBlocking {
            assertEquals(emptyList<Int>(), emptyList<Int>().mapAsync { it })
        }
    }

    @Test
    fun `mapAsync propagates exceptions thrown by the transform`() {
        runBlocking {
            val result = runCatching {
                listOf(1, 2).mapAsync { value ->
                    if (value == 2) error("boom") else value
                }
            }
            assertEquals("boom", result.exceptionOrNull()?.message)
        }
    }

    @Test
    fun `mapAsync resolves after the slowest transform`() {
        runBlocking {
            val result = coroutineScope {
                listOf(1, 2).mapAsync { value ->
                    async { value }.await()
                }
            }
            assertEquals(listOf(1, 2), result)
        }
    }
}