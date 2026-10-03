package com.resonix.media.core.common.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatExtensionsTest {

    @Test
    fun `round keeps the value when it has no decimals`() {
        assertEquals(3f, 2.6f.round(0), TOLERANCE)
    }

    @Test
    fun `round truncates to the requested number of decimals`() {
        assertEquals(1.23f, 1.23456f.round(2), TOLERANCE)
        assertEquals(1.2f, 1.23456f.round(1), TOLERANCE)
    }

    @Test
    fun `round pads values that need fewer decimals than requested`() {
        assertEquals(1.5f, 1.5f.round(3), TOLERANCE)
    }

    @Test
    fun `round handles negative values`() {
        assertEquals(-1.23f, (-1.23456f).round(2), TOLERANCE)
    }

    @Test
    fun `toString always emits the requested number of decimals`() {
        assertEquals("1.00", 1f.toString(2))
        assertEquals("1.23", 1.23456f.toString(2))
        assertEquals("1", 1.4f.toString(0))
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}