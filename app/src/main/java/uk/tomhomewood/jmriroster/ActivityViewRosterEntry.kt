package uk.tomhomewood.jmriroster

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import kotlinx.android.synthetic.main.activity_view_roster_entry.*
import kotlinx.coroutines.launch
import uk.tomhomewood.jmriroster.lib.v1.*

class ActivityViewRosterEntry : AppCompatActivity() {

    companion object {
        const val EXTRA_ROSTER_ID: String = "rosterId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_roster_entry)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val rosterId = getRosterIdFromIntent()
        if (rosterId!=null){
            val model: RosterEntryViewModel by viewModels {
                RosterEntryViewModelFactory(rosterId)
            }
            model.getRosterEntry().observe(this, Observer<RosterEntry>{
                    rosterEntry -> bindRosterEntry(rosterEntry)
            })
            findViewById<ImageView>(R.id.image).loadRosterEntryImage(BuildConfig.ROSTER_API_URL, rosterId, 1000)
        }
    }

    private fun getRosterIdFromIntent(): String? {
        return intent.getStringExtra(EXTRA_ROSTER_ID)
    }

    private fun bindRosterEntry(rosterEntry: RosterEntry) {
        toolbar.title = rosterEntry.number
        findViewById<TextView>(R.id.number).text = rosterEntry.number
        findViewById<TextView>(R.id.name).text = rosterEntry.name
        findViewById<TextView>(R.id.address).text = rosterEntry.dccAddress
    }
}

fun AppCompatActivity.launchViewRosterEntryForResult(rosterId: String, launchCode: Int) {
    this.startActivityForResult(Intent(this, ActivityViewRosterEntry::class.java).putExtra(ActivityViewRosterEntry.EXTRA_ROSTER_ID, rosterId), launchCode)
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