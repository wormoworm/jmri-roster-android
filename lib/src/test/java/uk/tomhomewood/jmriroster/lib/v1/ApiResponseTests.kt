package uk.tomhomewood.jmriroster.lib.v1

import com.google.gson.Gson
import com.google.gson.JsonObject
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
    private lateinit var testRoster: JsonObject
    private lateinit var testRosterEntry: JsonObject

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
                            .setBody(testRoster.toString())
                    }
                    // Roster entry "123" will return a standard roster entry response.
                    "/v1/locomotive/123" -> {
                        MockResponse()
                            .setResponseCode(HttpURLConnection.HTTP_OK)
                            .setBody(testRosterEntry.toString())
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

        testRoster = Gson().fromJson("{\"locomotives\": [{\"id\": \"123\",\"dccAddress\": \"123\",\"fileName\": \"123.xml\",\"number\": \"123\",\"name\": \"One two three\",\"manufacturer\": \"Company\",\"model\": \"Model one\",\"owner\": \"John Smith\",\"comment\": \"Line one\\nLine two.\",\"imageFilePath\": \"roster\\/123.jpg\",\"functions\": [{\"number\": 0,\"name\": \"Light\",\"lockable\": true}, {\"number\": 1,\"name\": \"Bell\",\"lockable\": false}]},{\"id\": \"456\",\"dccAddress\": \"456\",\"fileName\": \"456.xml\",\"number\": \"456\",\"name\": \"Four five six\",\"manufacturer\": \"Company\",\"model\": \"Model two\",\"owner\": \"John Smith\",\"comment\": \"Line one\\nLine two.\",\"imageFilePath\": \"roster\\/456.jpg\"}],\"created\": 1589649974,\"modified\": 1589649956,\"loadTime\": 7,\"metadata\": []}", JsonObject::class.java)
        testRosterEntry = Gson().fromJson("{\"locomotive\": {\"id\": \"123\",\"dccAddress\": \"123\",\"fileName\": \"123.xml\",\"number\": \"123\",\"name\": \"One two three\",\"manufacturer\": \"Company\",\"model\": \"Model one\",\"owner\": \"John Smith\",\"comment\": \"Line one\\nLine two.\",\"imageFilePath\": \"roster\\/123.jpg\",\"functions\": [{\"number\": 0,\"name\": \"Light\",\"lockable\": true}, {\"number\": 1,\"name\": \"Bell\",\"lockable\": false}]},\"created\": 1589649951,\"modified\": 1589649941,\"loadTime\": 7,\"metadata\": []}", JsonObject::class.java)
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
        val testLocomotives = testRoster.get("locomotives").asJsonArray
        assertEquals(rosterEntries.size, testLocomotives.size())
        for (i in 0 until testLocomotives.size()){
            val testLocomotive = testLocomotives.get(i).asJsonObject
            assertEquals(rosterEntries[i].id, testLocomotive.get("id").asString)
            assertEquals(rosterEntries[i].id, testLocomotive.get("id").asString)
            assertEquals(rosterEntries[i].dccAddress, testLocomotive.get("dccAddress").asString)
            assertEquals(rosterEntries[i].number, testLocomotive.get("number").asString)
            assertEquals(rosterEntries[i].name, testLocomotive.get("name").asString)
            assertEquals(rosterEntries[i].manufacturer, testLocomotive.get("manufacturer").asString)
            assertEquals(rosterEntries[i].model, testLocomotive.get("model").asString)
            assertEquals(rosterEntries[i].owner, testLocomotive.get("owner").asString)
            assertEquals(rosterEntries[i].comment, testLocomotive.get("comment").asString)
            val testRosterEntryHasFunctions = testLocomotive.has("functions") && testLocomotive.get("functions").asJsonArray.size() > 0
            assertEquals(rosterEntries[i].hasFunctions(), testRosterEntryHasFunctions)
            if (testRosterEntryHasFunctions) {
                assertEquals(rosterEntries[i].getFunctionCount(), testLocomotive.get("functions").asJsonArray.size())
                val testLocomotiveFunctions = testLocomotive.get("functions").asJsonArray
                for (j in 0 until testLocomotiveFunctions.size()){
                    val function = testLocomotiveFunctions.get(j).asJsonObject
                    assertEquals(rosterEntries[i].functions[j].number, function.get("number").asInt)
                    assertEquals(rosterEntries[i].functions[j].name, function.get("name").asString)
                    assertEquals(rosterEntries[i].functions[j].lockable, function.get("lockable").asBoolean)
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
        val locomotive123 = testRosterEntry.get("locomotive").asJsonObject
        val locomotive123Functions = locomotive123.get("functions").asJsonArray

        val rosterEntryResponse = runBlocking {
            rosterApi.getRosterEntry("123")
        }
        assert(rosterEntryResponse is Result.Success)
        val rosterEntry = (rosterEntryResponse as Result.Success).value.rosterEntry
        assertEquals(rosterEntry.id, locomotive123.get("id").asString)
        assertEquals(rosterEntry.dccAddress, locomotive123.get("dccAddress").asString)
        assertEquals(rosterEntry.number, locomotive123.get("number").asString)
        assertEquals(rosterEntry.name, locomotive123.get("name").asString)
        assertEquals(rosterEntry.manufacturer, locomotive123.get("manufacturer").asString)
        assertEquals(rosterEntry.model, locomotive123.get("model").asString)
        assertEquals(rosterEntry.owner, locomotive123.get("owner").asString)
        assertEquals(rosterEntry.comment, locomotive123.get("comment").asString)
        val testRosterEntryHasFunctions = locomotive123.has("functions") && locomotive123.get("functions").asJsonArray.size() > 0
        assertEquals(rosterEntry.hasFunctions(), testRosterEntryHasFunctions)
        if (testRosterEntryHasFunctions) {
            assertEquals(rosterEntry.getFunctionCount(), locomotive123.get("functions").asJsonArray.size())
            for (i in 0 until locomotive123Functions.size()) {
                val function = locomotive123Functions.get(i).asJsonObject
                assertEquals(rosterEntry.functions[i].number, function.get("number").asInt)
                assertEquals(rosterEntry.functions[i].name, function.get("name").asString)
                assertEquals(rosterEntry.functions[i].lockable, function.get("lockable").asBoolean)
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