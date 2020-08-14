package uk.tomhomewood.jmriroster.lib.v1

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class ApiResponseTests {

    private var mockWebServer = MockWebServer()
    private lateinit var rosterApi: RosterApi
    private lateinit var rosterEntry123: JsonObject

    private var serverReturn500: Boolean = false

    private val dispatcher = object : Dispatcher() {
        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {
            if (serverReturn500){
                return MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            }
            else {
                return when (request.path) {
                    // Roster entry "123" will return a standard roster entry response.
                    "/v1/locomotive/123" -> {
                        MockResponse()
                            .setResponseCode(HttpURLConnection.HTTP_OK)
                            .setBody(rosterEntry123.toString())
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

        rosterEntry123 = Gson().fromJson("{\"locomotive\": {\"id\": \"123\",\"dccAddress\": \"123\",\"fileName\": \"123.xml\",\"number\": \"123\",\"name\": \"One two three\",\"manufacturer\": \"Company\",\"model\": \"Model one\",\"owner\": \"John Smith\",\"comment\": \"Line one\\nLine two.\",\"imageFilePath\": \"roster\\/123.jpg\",\"functions\": [{\"number\": 0,\"name\": \"Light\",\"lockable\": true}, {\"number\": 1,\"name\": \"Bell\",\"lockable\": false}]},\"created\": 1589649951,\"modified\": 1589649941,\"loadTime\": 7,\"metadata\": []}", JsonObject::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testRosterEntry() {
        // Extract the values we will test against from the master locomotive JsonObject.
        val locomotive123 = rosterEntry123.get("locomotive").asJsonObject
        val locomotive123Functions = locomotive123.get("functions").asJsonArray
        val locomotive123FunctionF0 = locomotive123Functions.get(0).asJsonObject
        val locomotive123FunctionF1 = locomotive123Functions.get(1).asJsonObject

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
        assertEquals(rosterEntry.functions.size, locomotive123Functions.size())
        assertEquals(rosterEntry.functions[0].number, locomotive123FunctionF0.get("number").asInt)
        assertEquals(rosterEntry.functions[0].name, locomotive123FunctionF0.get("name").asString)
        assertEquals(rosterEntry.functions[0].lockable, locomotive123FunctionF0.get("lockable").asBoolean)
    }

    @Test
    fun testRosterEntryNotFound() {
        val rosterEntryResponse = runBlocking {
            rosterApi.getRosterEntry("12345")
        }
        assert(rosterEntryResponse is Result.Error)
        assert((rosterEntryResponse as Result.Error).code==HttpURLConnection.HTTP_NOT_FOUND)
    }

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