package uk.tomhomewood.jmriroster.lib.v1

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/**
 * Tests the models defined in RosterModel.kt
 */
class ModelTests {

    @Test
    fun `Test roster entries with and without names`() {
        val rosterEntryWithName = RosterEntry(
            id = "66957",
            dccAddress = "6957",
            number = null,
            name = "Test name",
            manufacturer = null,
            model = null,
            owner = null,
            comment = null
        )
        assertTrue(rosterEntryWithName.hasName())
        val rosterEntryWithoutName = RosterEntry(
            id = "66957",
            dccAddress = "6957",
            number = null,
            name = null,
            manufacturer = null,
            model = null,
            owner = null,
            comment = null
        )
        assertFalse(rosterEntryWithoutName.hasName())
        val rosterEntryWithEmptyName = RosterEntry(
            id = "66957",
            dccAddress = "6957",
            number = null,
            name = "",
            manufacturer = null,
            model = null,
            owner = null,
            comment = null
        )
        assertFalse(rosterEntryWithEmptyName.hasName())
    }
}