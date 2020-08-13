package uk.tomhomewood.jmriroster.lib.v1

import android.widget.ImageView
import coil.api.load
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

const val IMAGE_URL_FORMAT ="%sv1/locomotive/%s/image/%d"

interface RosterApiInterface{
    suspend fun getRoster(): ResultWrapper<RosterResponse>

    suspend fun getRosterEntry(id: String): ResultWrapper<RosterEntryResponse>

    fun loadRosterEntryImage(id: String, size: Int, imageView: ImageView)
}

class RosterApi(private val baseUrl: String, private val dispatcher: CoroutineDispatcher = Dispatchers.IO):
    RosterApiInterface {

    var roster: Roster = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()
        .create(Roster::class.java)

    override suspend fun getRoster(): ResultWrapper<RosterResponse> {
        return safeApiCall(dispatcher) { roster.getRoster() }
    }

    override suspend fun getRosterEntry(id: String): ResultWrapper<RosterEntryResponse> {
        return safeApiCall(dispatcher) { roster.getRosterEntry(id) }
    }

    override fun loadRosterEntryImage(id: String, size: Int, imageView: ImageView){
        imageView.load(IMAGE_URL_FORMAT.format(baseUrl, id, size))
    }

    private suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): ResultWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> ResultWrapper.Error(
                        message = throwable.message
                    )
                    is HttpException -> {
                        val code = throwable.code()
                        val errorMessage = convertErrorBody(throwable)
                        ResultWrapper.Error(
                            code,
                            errorMessage
                        )
                    }
                    else -> {
                        ResultWrapper.Error(
                            code = -1,
                            message = throwable.message
                        )
                    }
                }
            }
        }
    }

    private fun convertErrorBody(throwable: HttpException): String? {
        return throwable.response()?.errorBody()?.string()
    }
}

sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T): ResultWrapper<T>()
    data class Error(val code: Int? = null, val message: String? = null): ResultWrapper<Nothing>()
}