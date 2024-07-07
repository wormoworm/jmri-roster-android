package uk.tomhomewood.jmriroster

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import kotlinx.android.synthetic.main.activity_view_roster_entry.*
import kotlinx.coroutines.launch
import uk.tomhomewood.jmriroster.lib.v2.*
import coil.transform.Transformation
import coil.transition.CrossfadeTransition
import coil.transition.Transition
import com.google.android.material.appbar.CollapsingToolbarLayout

class ActivityViewRosterEntry : AppCompatActivity() {

    companion object {
        const val EXTRA_ROSTER_ID: String = "rosterId"

        fun getLaunchIntent(context: Context, rosterId: String): Intent {
            return Intent(context, ActivityViewRosterEntry::class.java).putExtra(EXTRA_ROSTER_ID, rosterId)
        }
    }

    private lateinit var toolbarLayout: CollapsingToolbarLayout

    private val contentBlockHeadingIds = arrayOf(R.id.block_basic_info_title, R.id.block_comments_title)

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
            toolbarLayout = findViewById(R.id.toolbar_layout)
            RosterImageLoader.get(this, BuildConfig.ROSTER_API_URL).loadRosterEntryImage(rosterId, 1000, findViewById(R.id.image), transition = if (BuildConfig.USE_PALETTE) PaletteTransition(region = RectF(0.1F, 0.1F, 0.9F, 0.9F)){ palette -> applyPaletteForRosterEntry(palette) } else null)
        }
    }

    private fun getRosterIdFromIntent(): String? {
        return intent.getStringExtra(EXTRA_ROSTER_ID)
    }

    private fun bindRosterEntry(rosterEntry: RosterEntry) {
        toolbar.title = rosterEntry.number
        rosterEntry.name?.let {
            findViewById<TextView>(R.id.name_label).visibility = View.VISIBLE
            findViewById<TextView>(R.id.name).visibility = View.VISIBLE
            findViewById<TextView>(R.id.name).text = it
        }
        findViewById<TextView>(R.id.address).text = rosterEntry.dccAddress
        rosterEntry.comment?.let {
            findViewById<ViewGroup>(R.id.block_comments).visibility = View.VISIBLE
            findViewById<TextView>(R.id.comments).text = it
        }
    }

    private fun applyPaletteForRosterEntry(palette: Palette) {
        // Show the colour palette if requested
        if (showColourPalette()) {
            findViewById<ViewGroup>(R.id.palette_demo).visibility = View.VISIBLE
            findViewById<TextView>(R.id.palette_demo_light_vibrant).setBackgroundColor(palette.getLightVibrantColor(0))
            findViewById<TextView>(R.id.palette_demo_vibrant).setBackgroundColor(palette.getVibrantColor(0))
            findViewById<TextView>(R.id.palette_demo_dark_vibrant).setBackgroundColor(palette.getDarkVibrantColor(Color.RED))
            findViewById<TextView>(R.id.palette_demo_light_muted).setBackgroundColor(palette.getLightMutedColor(0))
            findViewById<TextView>(R.id.palette_demo_muted).setBackgroundColor(palette.getMutedColor(0))
            findViewById<TextView>(R.id.palette_demo_dark_muted).setBackgroundColor(palette.getDarkMutedColor(0))
        }
        val primaryPaletteColour = palette.darkVibrantSwatch?.rgb ?: palette.vibrantSwatch?.rgb ?: ContextCompat.getColor(this, R.color.colorPrimary)
        toolbarLayout.setContentScrimColor(primaryPaletteColour)
        window.statusBarColor = primaryPaletteColour

        contentBlockHeadingIds.forEach {
            findViewById<TextView>(it).setTextColor(primaryPaletteColour)
        }
    }

    private fun showColourPalette(): Boolean {
        return BuildConfig.DEBUG && false
    }
}

fun AppCompatActivity.launchViewRosterEntryForResult(rosterId: String, launchCode: Int) {
    this.startActivityForResult(ActivityViewRosterEntry.getLaunchIntent(this, rosterId), launchCode)
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