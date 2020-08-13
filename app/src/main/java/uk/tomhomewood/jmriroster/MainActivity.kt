package uk.tomhomewood.jmriroster

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import uk.tomhomewood.jmriroster.lib.v1.ResultWrapper
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
            when (val response = roster.getRoster()){
                is ResultWrapper.Error -> handleError(response)
                is ResultWrapper.Success -> handleRosterEntries(response.value.rosterEntries)
            }
        }
    }

    private fun handleError(error: ResultWrapper.Error){
        Log.e(TAG, "Error, message: %s, code: %d".format(error.message, error.code ?: -1))
    }

    private fun handleRosterEntry(rosterEntry: RosterEntry){
        Log.d(TAG, "Locomotive F2 name: "+ rosterEntry.functions[2].name)
    }

    private fun handleRosterEntries(rosterEntries: List<RosterEntry>){
        for (rosterEntry in rosterEntries) {
            Log.d(TAG, "Locomotive ID: "+rosterEntry.id)
        }
    }
}