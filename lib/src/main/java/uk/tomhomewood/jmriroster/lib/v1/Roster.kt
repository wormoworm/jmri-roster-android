package uk.tomhomewood.jmriroster.lib.v1

import retrofit2.http.GET
import retrofit2.http.Path

interface Roster{
    @GET("v1/roster")
    suspend fun getRoster(): RosterResponse
    @GET("v1/locomotive/{id}")
    suspend fun getRosterEntry(@Path("id") id: String): RosterEntryResponse
}