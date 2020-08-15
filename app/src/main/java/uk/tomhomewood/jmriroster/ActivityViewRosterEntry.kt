package uk.tomhomewood.jmriroster

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import uk.tomhomewood.jmriroster.lib.v1.*

class ActivityViewRosterEntry : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_roster_entry)

        val rosterId = "66789"  //TODO Fetch from extras

        val model: RosterEntryViewModel by viewModels {
            RosterEntryViewModelFactory(rosterId)
        }
        model.getRosterEntry().observe(this, Observer<RosterEntry>{
            rosterEntry -> findViewById<TextView>(R.id.roster_id).text = rosterEntry.name
        })
        findViewById<ImageView>(R.id.image).loadRosterEntryImage(BuildConfig.ROSTER_API_URL, rosterId, 1000)
    }
}

class RosterEntryViewModelFactory(private val rosterId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RosterEntryViewModel(rosterId) as T
    }
}

class RosterEntryViewModel(private val rosterId: String) : ViewModel() {

    private val rosterApi: RosterApi by lazy {
        RosterApi(BuildConfig.ROSTER_API_URL)
    }

    private val rosterEntryData: MutableLiveData<RosterEntry> by lazy {
        MutableLiveData<RosterEntry>().also {
            loadRosterEntry(rosterId)
        }
    }

    private fun loadRosterEntry(id: String) {
        viewModelScope.launch {
            when (val response = rosterApi.getRosterEntry(id = id)){
                is Result.Success -> {
                    rosterEntryData.value = response.value.rosterEntry
                }
                is Result.Error -> {
                    Log.e(TAG, "Error: "+response.message+", code: "+response.code)
                }
            }
        }
    }

    fun getRosterEntry(): MutableLiveData<RosterEntry> {
        return rosterEntryData
    }
}