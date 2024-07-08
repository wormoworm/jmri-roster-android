package uk.tomhomewood.jmriroster.lib.v2

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * Tests the some of the exception handling in RosterApi. HttpExceptions are not tested, as they will be tested in a separate test class using a MockWebserver.
 * This test class tests IOExceptions and any other exceptions, as well as a simple success test case.
 */
@ExperimentalCoroutinesApi
class NetworkErrorTests {
    private val dispatcher = TestCoroutineDispatcher()

    @Test
    fun `When response is true then Result-Success is emitted`() {
        runBlockingTest {
            val lambdaResult = true
            val result = safeApiCall(dispatcher) { lambdaResult }
            assertEquals(Result.Success(lambdaResult), result)
        }
    }

    @Test
    fun `When response throws IOException then Result-Error is emitted`() {
        runBlockingTest {
            val exception = IOException("IO error")
            val result = safeApiCall(dispatcher) {
                throw exception
            }
            assertEquals(Result.Error(ERROR_CODE_NOT_SET, exception.message), result)
        }
    }

    @Test
    fun `When response throws an unknown Exception then Result-Error is emitted`() {
        runBlockingTest {
            val exception = IllegalStateException("Unknown exception")
            val result = safeApiCall(dispatcher) {
                throw exception
            }
            assertEquals(Result.Error(ERROR_CODE_NOT_SET, exception.message), result)
        }
    }
}