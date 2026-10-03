package com.resonix.media.core.database.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistItemEntityTest {

    @Test
    fun `optional columns default to null and duration to unknown`() {
        val item = PlaylistItemEntity(playlistId = 1L, uri = "content://video/1", position = 0)

        assertNull(item.title)
        assertNull(item.tvgLogo)
        assertNull(item.groupTitle)
        assertNull(item.lastPlayedAt)
        assertEquals(UNKNOWN_DURATION, item.duration)
    }

    @Test
    fun `items are identified by playlist and uri`() {
        val item = PlaylistItemEntity(playlistId = 1L, uri = "content://video/1", position = 0)

        assertEquals(item, item.copy())
        assertEquals(item.hashCode(), item.copy().hashCode())
        assertNotEquals(item, item.copy(playlistId = 2L))
        assertNotEquals(item, item.copy(uri = "content://video/2"))
    }

    @Test
    fun `reordering an item keeps its primary key but changes its position`() {
        val item = PlaylistItemEntity(playlistId = 1L, uri = "content://video/1", position = 0)

        val moved = item.copy(position = 7)

        assertEquals(item.playlistId, moved.playlistId)
        assertEquals(item.uri, moved.uri)
        assertEquals(7, moved.position)
    }

    private companion object {
        const val UNKNOWN_DURATION = -1
    }
}