package uk.tomhomewood.jmriroster.lib.v1

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

const val ERROR_CODE_NOT_SET = -1
const val NO_FALLBACK = -1

interface RosterApiInterface{
    suspend fun getRoster(): Result<RosterResponse>

    suspend fun getRosterEntry(id: String): Result<RosterEntryResponse>
}

class RosterApi(baseUrl: String, private val dispatcher: CoroutineDispatcher = Dispatchers.IO): RosterApiInterface {

    private var roster: Roster = Retrofit.Builder()
        .baseUrl(baseUrl)
//        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(Roster::class.java)

    override suspend fun getRoster(): Result<RosterResponse> {
        return safeApiCall(dispatcher) { roster.getRoster() }
    }

    override suspend fun getRosterEntry(id: String): Result<RosterEntryResponse> {
        return safeApiCall(dispatcher) { roster.getRosterEntry(id) }
    }
}

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> Result.Error(code = ERROR_CODE_NOT_SET, message = throwable.message)
                is HttpException -> Result.Error(throwable.code(), getErrorMessageFromHttpException(throwable))
                else -> Result.Error(code = ERROR_CODE_NOT_SET, message = throwable.message)
            }
        }
    }
}

private fun getErrorMessageFromHttpException(throwable: HttpException): String? {
    return throwable.response()?.errorBody()?.string()
}

sealed class Result<out T> {
    data class Success<out T>(val value: T): Result<T>()
    data class Error(val code: Int? = null, val message: String? = null): Result<Nothing>()
}