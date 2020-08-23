package uk.tomhomewood.jmriroster.lib.v1

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class ApiResponseTests {

    private var mockWebServer = MockWebServer()
    private lateinit var rosterApi: RosterApi
    private lateinit var testRoster: RosterResponse
    private lateinit var testRosterEntry: RosterEntryResponse
    private lateinit var moshi: Moshi
    private lateinit var rosterAdapter: JsonAdapter<RosterResponse>
    private lateinit var rosterEntryAdapter: JsonAdapter<RosterEntryResponse>


    private var serverReturn500: Boolean = false

    private val dispatcher = object : Dispatcher() {
        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {
            if (serverReturn500){
                return MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            }
            else {
                return when (request.path) {
                    // Requesting the roster will return a sample roster.
                    "/v1/roster" -> {
                        MockResponse()
                            .setResponseCode(HttpURLConnection.HTTP_OK)
                            .setBody(rosterAdapter.toJson(testRoster))
                    }
                    // Roster entry "123" will return a standard roster entry response.
                    "/v1/locomotive/123" -> {
                        MockResponse()
                            .setResponseCode(HttpURLConnection.HTTP_OK)
                            .setBody(rosterEntryAdapter.toJson(testRosterEntry))
                    }
                    // Roster entry "000" does not exist, so a 404 will be returned.
                    "/v1/locomotive/000" -> {
                        MockResponse()
                            .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                            .setBody("{\"error:\":\"Locomotive with ID 000 not found\"}")
                    }
                    // Covers any other possible requests.
                    else ->
                        MockResponse()
                            .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                }
            }
        }
    }

    @Before
    fun setup() {
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start()

        rosterApi = RosterApi(mockWebServer.url("/").toString())

        moshi = Moshi.Builder().build()
        rosterAdapter = moshi.adapter(RosterResponse::class.java)
        rosterEntryAdapter = moshi.adapter(RosterEntryResponse::class.java)

        testRoster = rosterAdapter.fromJson("{\"locomotives\": [{\"id\": \"123\",\"dccAddress\": \"123\",\"fileName\": \"123.xml\",\"number\": \"123\",\"name\": \"One two three\",\"manufacturer\": \"Company\",\"model\": \"Model one\",\"owner\": \"John Smith\",\"comment\": \"Line one\\nLine two.\",\"imageFilePath\": \"roster\\/123.jpg\",\"functions\": [{\"number\": 0,\"name\": \"Light\",\"lockable\": true}, {\"number\": 1,\"name\": \"Bell\",\"lockable\": false}]},{\"id\": \"456\",\"dccAddress\": \"456\",\"fileName\": \"456.xml\",\"number\": \"456\",\"name\": \"Four five six\",\"manufacturer\": \"Company\",\"model\": \"Model two\",\"owner\": \"John Smith\",\"comment\": \"Line one\\nLine two.\",\"imageFilePath\": \"roster\\/456.jpg\"}],\"created\": 1589649974,\"modified\": 1589649956,\"loadTime\": 7,\"metadata\": []}")!!
        testRosterEntry = rosterEntryAdapter.fromJson("{\"locomotive\": {\"id\": \"123\",\"dccAddress\": \"123\",\"fileName\": \"123.xml\",\"number\": \"123\",\"name\": \"One two three\",\"manufacturer\": \"Company\",\"model\": \"Model one\",\"owner\": \"John Smith\",\"comment\": \"Line one\\nLine two.\",\"imageFilePath\": \"roster\\/123.jpg\",\"functions\": [{\"number\": 0,\"name\": \"Light\",\"lockable\": true}, {\"number\": 1,\"name\": \"Bell\",\"lockable\": false}]},\"created\": 1589649951,\"modified\": 1589649941,\"loadTime\": 7,\"metadata\": []}")!!
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    /**
     * Tests the normal roster case: A roster exists and the API responds correctly with code 200.
     */
    @Test
    fun testRoster() {
        val rosterResponse = runBlocking {
            rosterApi.getRoster()
        }
        assert(rosterResponse is Result.Success)
        val rosterEntries = (rosterResponse as Result.Success).value.rosterEntries
        assertEquals(rosterEntries.size, testRoster.rosterEntries.size)
        for (i in testRoster.rosterEntries.indices){
            val testLocomotive = testRoster.rosterEntries[i]
            assertEquals(rosterEntries[i].id, testLocomotive.id)
            assertEquals(rosterEntries[i].dccAddress, testLocomotive.dccAddress)
            assertEquals(rosterEntries[i].number, testLocomotive.number)
            assertEquals(rosterEntries[i].name, testLocomotive.name)
            assertEquals(rosterEntries[i].manufacturer, testLocomotive.manufacturer)
            assertEquals(rosterEntries[i].model, testLocomotive.model)
            assertEquals(rosterEntries[i].owner, testLocomotive.owner)
            assertEquals(rosterEntries[i].comment, testLocomotive.comment)
            val testRosterEntryHasFunctions = testLocomotive.hasFunctions() && testLocomotive.getFunctionCount() > 0
            assertEquals(rosterEntries[i].hasFunctions(), testRosterEntryHasFunctions)
            if (testRosterEntryHasFunctions) {
                assertEquals(rosterEntries[i].getFunctionCount(), testLocomotive.getFunctionCount())
                val testLocomotiveFunctions = testLocomotive.functions
                for (j in testLocomotiveFunctions.indices){
                    val function = testLocomotiveFunctions[j]
                    assertEquals(rosterEntries[i].functions[j].number, function.number)
                    assertEquals(rosterEntries[i].functions[j].name, function.name)
                    assertEquals(rosterEntries[i].functions[j].lockable, function.lockable)
                }
            }
        }
    }

    /**
     * Tests handling of 500 internal server error when requesting the roster.
     */
    @Test
    fun testRosterInternalServerError() {
        serverReturn500 = true
        val rosterResponse = runBlocking {
            rosterApi.getRoster()
        }
        serverReturn500 = false
        assert(rosterResponse is Result.Error)
        assert((rosterResponse as Result.Error).code==HttpURLConnection.HTTP_INTERNAL_ERROR)
    }

    /**
     * Tests the normal roster entry case: The requested roster entry exists and the API responds correctly with code 200.
     */
    @Test
    fun testRosterEntry() {
        // Extract the values we will test against from the master locomotive JsonObject.
        val locomotive123 = testRosterEntry.rosterEntry
        val locomotive123Functions = locomotive123.functions

        val rosterEntryResponse = runBlocking {
            rosterApi.getRosterEntry("123")
        }
        assert(rosterEntryResponse is Result.Success)
        val rosterEntry = (rosterEntryResponse as Result.Success).value.rosterEntry
        assertEquals(rosterEntry.id, locomotive123.id)
        assertEquals(rosterEntry.dccAddress, locomotive123.dccAddress)
        assertEquals(rosterEntry.number, locomotive123.number)
        assertEquals(rosterEntry.name, locomotive123.name)
        assertEquals(rosterEntry.manufacturer, locomotive123.manufacturer)
        assertEquals(rosterEntry.model, locomotive123.model)
        assertEquals(rosterEntry.owner, locomotive123.owner)
        assertEquals(rosterEntry.comment, locomotive123.comment)
        val testRosterEntryHasFunctions = locomotive123.hasFunctions() && locomotive123.getFunctionCount() > 0
        assertEquals(rosterEntry.hasFunctions(), testRosterEntryHasFunctions)
        if (testRosterEntryHasFunctions) {
            assertEquals(rosterEntry.getFunctionCount(), locomotive123.getFunctionCount())
            for (i in locomotive123Functions.indices) {
                val function = locomotive123Functions[i]
                assertEquals(rosterEntry.functions[i].number, function.number)
                assertEquals(rosterEntry.functions[i].name, function.name)
                assertEquals(rosterEntry.functions[i].lockable, function.lockable)
            }
        }
    }

    /**
     * Tests handling a 404 not found when requesting a roster entry that does not exist.
     */
    @Test
    fun testRosterEntryNotFound() {
        val rosterEntryResponse = runBlocking {
            rosterApi.getRosterEntry("000")
        }
        assert(rosterEntryResponse is Result.Error)
        assert((rosterEntryResponse as Result.Error).code==HttpURLConnection.HTTP_NOT_FOUND)
    }

    /**
     * Tests handling of 500 internal server error when requesting a valid roster entry.
     */
    @Test
    fun testRosterEntryInternalServerError() {
        serverReturn500 = true
        val rosterEntryResponse = runBlocking {
            rosterApi.getRosterEntry("66957")
        }
        serverReturn500 = false
        assert(rosterEntryResponse is Result.Error)
        assert((rosterEntryResponse as Result.Error).code==HttpURLConnection.HTTP_INTERNAL_ERROR)
    }
}