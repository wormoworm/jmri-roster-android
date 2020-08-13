package uk.tomhomewood.jmriroster.lib.v1

import com.google.gson.annotations.SerializedName

data class RosterResponse(
    @SerializedName("locomotives")
    val rosterEntries: List<RosterEntry>
)

data class RosterEntryResponse(
    @SerializedName("locomotive")
    val rosterEntries: RosterEntry
)

data class RosterEntry(
    @SerializedName("id")
    val id: String,
    @SerializedName("dccAddress")
    val dccAddress: String,
    @SerializedName("number")
    val number: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("manufacturer")
    val manufacturer: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("owner")
    val owner: String,
    @SerializedName("functions")
    val functions: List<Function>
)

data class Function(
    @SerializedName("number")
    val number: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("lockable")
    val lockable: Boolean
)