package uk.tomhomewood.jmriroster

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.annotation.ExperimentalCoilApi
import kotlinx.coroutines.launch
import uk.tomhomewood.jmriroster.lib.v2.Result
import uk.tomhomewood.jmriroster.lib.v2.RosterApi
import uk.tomhomewood.jmriroster.lib.v2.RosterEntry
import uk.tomhomewood.jmriroster.lib.v2.loadRosterEntryImage

const val TAG = "MainActivity"

class ActivityViewRoster : AppCompatActivity() {

    companion object {
        fun getLaunchIntent(context: Context): Intent {
            return Intent(context, ActivityViewRoster::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_roster)

//        if (BuildConfig.DEBUG) launchViewRosterEntryForResult("66957", 1)

        val rosterList = findViewById<RecyclerView>(R.id.roster_list)
        rosterList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//        rosterList.addItemDecoration(LayoutMarginDecoration(1, resources.getDimensionPixelSize(R.dimen.margin_vertical_medium)))

        val model: RosterViewModel by viewModels()
        rosterList.adapter = RosterAdapter(this, model) {
            rosterEntry -> launchViewRosterEntryForResult(rosterEntry.id, 1)
        }
    }
}

class RosterViewModel : ViewModel() {

    private val rosterApi: RosterApi by lazy {
        RosterApi(BuildConfig.ROSTER_API_URL)
    }

    private val rosterEntriesData: MutableLiveData<List<RosterEntry>> by lazy {
        MutableLiveData<List<RosterEntry>>().also {
            loadRoster()
        }
    }

    private fun loadRoster() {
        viewModelScope.launch {
            when (val response = rosterApi.getRoster()){
                is Result.Success -> {
                    Log.d(TAG, "Got roster, size = "+response.value.rosterEntries.size)
                    rosterEntriesData.value = response.value.rosterEntries
                }
                is Result.Error -> {
                    Log.e(TAG, "Error: "+response.message+", code: "+response.code)
                }
            }
        }
    }

    fun getRosterEntries(): MutableLiveData<List<RosterEntry>> {
        return rosterEntriesData
    }
}

class RosterAdapter(lifecycleOwner: LifecycleOwner, private val rosterViewModel: RosterViewModel, private val rosterItemClickListener: (RosterEntry) -> Unit) : RecyclerView.Adapter<RosterListViewHolder>() {

    init {
        rosterViewModel.getRosterEntries().observe(lifecycleOwner, Observer<List<RosterEntry>> {
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RosterListViewHolder {
        return RosterListViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return rosterViewModel.getRosterEntries().value?.size ?: 0
    }

    override fun onBindViewHolder(viewHolder: RosterListViewHolder, position: Int) {
        val rosterEntry = rosterViewModel.getRosterEntries().value?.get(position)
        if (rosterEntry!=null) {
            viewHolder.bindRosterEntry(rosterEntry)
            viewHolder.itemView.setOnClickListener {
                rosterItemClickListener(rosterEntry)
            }
        }
    }
}

class RosterListViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_roster_list, parent, false)) {

    var rosterId: String = ""

    private val imageSize: Int by lazy {
        parent.resources.getDimensionPixelSize(R.dimen.roster_list_item_height)
    }

    @ExperimentalCoilApi
    fun bindRosterEntry(rosterEntry: RosterEntry) {
        rosterId = rosterEntry.id
        itemView.findViewById<TextView>(R.id.number).text = rosterEntry.number
        itemView.findViewById<TextView>(R.id.name).text = rosterEntry.name
        itemView.findViewById<TextView>(R.id.address).text = rosterEntry.dccAddress
        itemView.findViewById<ImageView>(R.id.image).loadRosterEntryImage(BuildConfig.ROSTER_API_URL, rosterEntry.id, imageSize, R.mipmap.locomotive_default)
    }
}