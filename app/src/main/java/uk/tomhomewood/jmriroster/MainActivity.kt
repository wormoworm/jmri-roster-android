package uk.tomhomewood.jmriroster

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import uk.tomhomewood.jmriroster.lib.v1.Result
import uk.tomhomewood.jmriroster.lib.v1.RosterApi
import uk.tomhomewood.jmriroster.lib.v1.RosterEntry

const val TAG = "MainActivity"
const val BASE_URL = "https://roster.tomstrains.co.uk/api/"

/**
 * Uses code from https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val roster = RosterApi(BASE_URL)
        roster.loadRosterEntryImage("66957", 900, findViewById(R.id.image))

        GlobalScope.launch {
            when (val response = roster.getRosterEntry("66789a")){
                is Result.Success -> {
                    Log.d(TAG, "Locomotive name: "+response.value.rosterEntry.name)
                }
                is Result.Error -> {
                    Log.e(TAG, "Error: "+response.message+", code: "+response.code)
                }
            }
        }
    }

    private fun handleRosterEntries(rosterEntries: List<RosterEntry>){
        for (rosterEntry in rosterEntries) {
            Log.d(TAG, "Locomotive ID: "+rosterEntry.id)
        }
    }
}