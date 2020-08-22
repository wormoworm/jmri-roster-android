package uk.tomhomewood.jmriroster.lib.v1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.bitmappool.BitmapPool
import coil.map.Mapper
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.RequestResult
import coil.request.SuccessResult
import coil.size.Size
import coil.transform.Transformation
import coil.transition.Transition
import coil.transition.TransitionTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class RosterImageLoader(private val context: Context, private val baseUrl: String) {
    companion object {
        private var rosterImageLoader: RosterImageLoader? = null

        @JvmStatic
        fun get(context: Context, baseUrl: String): RosterImageLoader = rosterImageLoader ?: initialiseRosterImageLoader(context, baseUrl)

        private fun initialiseRosterImageLoader(context: Context, baseUrl: String): RosterImageLoader {
            val newRosterImageLoader = RosterImageLoader(context, baseUrl)
            this.rosterImageLoader = newRosterImageLoader
            return newRosterImageLoader
        }
    }

    private val imageLoader = ImageLoader.Builder(context)
        .componentRegistry{
            add(RosterIdMapper(baseUrl))
        }
        .build()

    @ExperimentalCoilApi
    fun loadRosterEntryImage(id: String, size: Int, imageView: ImageView, @DrawableRes fallbackResId: Int = NO_FALLBACK, transition: Transition? = null, transformations: List<Transformation> = ArrayList()) {

        val imageRequest = ImageRequest.Builder(context)
            .data(RosterEntryImageParams(id, size))
            .target(imageView)
            .allowHardware(!isEmulator())

        Log.d("Palette", "Emulator: "+ isEmulator())
        if (fallbackResId != NO_FALLBACK) imageRequest.error(fallbackResId)
        if (transformations.isNotEmpty()) imageRequest.transformations(transformations)
        if (transition != null) imageRequest.transition(transition)

        imageLoader.enqueue(imageRequest.build())
    }
}

data class RosterEntryImageParams(
    val id: String,
    val size: Int
)

class RosterIdMapper(private val baseUrl: String) : Mapper<RosterEntryImageParams, String> {
    private val imageUrlFormat ="%sv1/locomotive/%s/image/%d"

    override fun map(data: RosterEntryImageParams) = imageUrlFormat.format(baseUrl, data.id, data.size)
}

@ExperimentalCoilApi
fun ImageView.loadRosterEntryImage(baseUrl: String, id: String, size: Int, @DrawableRes fallbackResId: Int = NO_FALLBACK, transition: Transition? = null, transformations: List<Transformation> = ArrayList()) {
    RosterImageLoader.get(context, baseUrl).loadRosterEntryImage(id, size, this, fallbackResId, transition, transformations)
}

@ExperimentalCoilApi
class PaletteTransition(private val delegate: Transition = Transition.NONE, private val region: RectF? = null, private val colourFilter: Palette.Filter? = null, private val onGenerated: (Palette) -> Unit) : Transition {

    override suspend fun transition(target: TransitionTarget, result: ImageResult) {

        // Execute the delegate transition.
        val delegateJob = delegate?.let { delegate ->
            coroutineScope {
                launch(Dispatchers.Main.immediate) {
                    delegate.transition(target, result)
                }
            }
        }

        // Compute the palette on a background thread.
        if (result is SuccessResult) {
            val bitmap = (result.drawable as BitmapDrawable).bitmap
            val palette = withContext(Dispatchers.IO) {
                val builder = Palette.Builder(bitmap)
                if (region != null) {
                    val region = calculateRegionForBitmap(region, bitmap)
                    Log.d("Image", "Region will be (LTRB): ${region.left}, ${region.top}, ${region.right}, ${region.bottom}}")
                    builder.setRegion(region.left, region.top, region.right, region.bottom )
                }
                if (colourFilter != null) {
                    builder.addFilter(colourFilter)
                }
                builder.generate()
            }
            onGenerated(palette)
        }
        delegateJob?.join()
    }

    private fun calculateRegionForBitmap(region: RectF, bitmap: Bitmap): Rect {
        return Rect(
            (region.left * bitmap.width).roundToInt(),
            (region.top * bitmap.height).roundToInt(),
            (region.right * bitmap.width).roundToInt(),
            (region.bottom * bitmap.height).roundToInt()
        )
    }
}

fun isEmulator(): Boolean {
    return Build.FINGERPRINT.contains("generic")
}