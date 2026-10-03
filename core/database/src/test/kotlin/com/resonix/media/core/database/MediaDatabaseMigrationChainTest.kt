package com.resonix.media.core.database

import androidx.room.migration.Migration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaDatabaseMigrationChainTest {

    private val allMigrations: List<Migration> = listOf(
        MediaDatabase.MIGRATION_1_2,
        MediaDatabase.MIGRATION_2_3,
        MediaDatabase.MIGRATION_3_4,
        MediaDatabase.MIGRATION_4_5,
        MediaDatabase.MIGRATION_5_6,
        MediaDatabase.MIGRATION_6_7,
        MediaDatabase.MIGRATION_7_8,
        MediaDatabase.MIGRATION_8_9,
        MediaDatabase.MIGRATION_9_10,
        MediaDatabase.MIGRATION_10_11,
        MediaDatabase.MIGRATION_11_12,
    )

    @Test
    fun `the migration chain starts at the very first schema version`() {
        assertEquals(1, allMigrations.minOf { it.startVersion })
    }

    @Test
    fun `the migration chain has no gaps`() {
        val ordered = allMigrations.sortedBy { it.startVersion }

        ordered.zipWithNext { previous, next ->
            assertEquals(
                "gap between schema ${previous.endVersion} and ${next.startVersion}",
                previous.endVersion,
                next.startVersion,
            )
        }
    }

    @Test
    fun `the migration chain ends on the current schema version`() {
        assertEquals(CURRENT_SCHEMA_VERSION, allMigrations.maxOf { it.endVersion })
    }

    @Test
    fun `every migration is declared exactly once`() {
        assertEquals(allMigrations.size, allMigrations.map { it.startVersion }.toSet().size)
    }

    @Test
    fun `migrations are registered for every intermediate version`() {
        val registered = (1 until CURRENT_SCHEMA_VERSION).map { version ->
            allMigrations.singleOrNull { it.startVersion == version && it.endVersion == version + 1 }
        }

        assertTrue("missing migration between $registered", registered.all { it != null })
    }

    private companion object {
        /** Must be kept in sync with the `version` declared on [MediaDatabase]. */
        const val CURRENT_SCHEMA_VERSION = 12
    }
}