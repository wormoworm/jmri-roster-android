package uk.tomhomewood.jmriroster.lib.v2

import com.squareup.moshi.Json

data class RosterResponse(
    @field:Json(name = "locomotives")
    val rosterEntries: List<RosterEntry>
)

data class RosterEntryResponse(
    @field:Json(name = "locomotive")
    val rosterEntry: RosterEntry
)

data class RosterEntry(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "dccAddress")
    val dccAddress: String,
    @field:Json(name = "number")
    val number: String?,
    @field:Json(name = "name")
    val name: String?,
    @field:Json(name = "manufacturer")
    val manufacturer: String?,
    @field:Json(name = "model")
    val model: String?,
    @field:Json(name = "owner")
    val owner: String?,
    @field:Json(name = "comment")
    val comment: String?,
    @field:Json(name = "functions")
    val functions: List<Function>? = ArrayList()
){
    fun hasName(): Boolean {
        return name !=null && name.isNotEmpty()
    }

    fun getFunctionCount(): Int {
        return functions?.size ?: 0
    }

    fun hasFunctions(): Boolean {
        return getFunctionCount() > 0
    }
}

data class Function(
    @field:Json(name = "number")
    val number: Int,
    @field:Json(name = "name")
    val name: String,
    @field:Json(name = "lockable")
    val lockable: Boolean
)