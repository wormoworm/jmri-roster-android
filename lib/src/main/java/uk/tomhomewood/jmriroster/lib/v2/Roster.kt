package uk.tomhomewood.jmriroster.lib.v2

import retrofit2.http.GET
import retrofit2.http.Path

interface Roster{
    @GET("v2/roster")
    suspend fun getRoster(): RosterResponse
    @GET("v2/locomotive/{id}")
    suspend fun getRosterEntry(@Path("id") id: String): RosterEntryResponse
}