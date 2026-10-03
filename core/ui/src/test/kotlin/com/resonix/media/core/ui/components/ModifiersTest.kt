package com.resonix.media.core.ui.components

import androidx.compose.ui.Modifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ModifiersTest {

    @Test
    fun `thenIf applies the block when the condition holds`() {
        val result = Modifier.thenIf(true) { this then TagMarker("applied") }

        assertEquals(TagMarker("applied"), result)
    }

    @Test
    fun `thenIf returns the receiver untouched when the condition does not hold`() {
        val result = Modifier.thenIf(false) { this then TagMarker("applied") }

        assertSame(Modifier, result)
    }

    @Test
    fun `thenIf chains keep the modifier flat for mutually exclusive states`() {
        val result = Modifier
            .thenIf(condition = true) { this then TagMarker("selected") }
            .thenIf(condition = false) { this then TagMarker("unselected") }

        assertEquals(TagMarker("selected"), result)
    }

    private data class TagMarker(val tag: String) : Modifier.Element
}