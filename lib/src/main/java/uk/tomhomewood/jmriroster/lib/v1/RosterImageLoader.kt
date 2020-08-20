package uk.tomhomewood.jmriroster.lib.v1

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.bitmappool.BitmapPool
import coil.map.Mapper
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation

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

    fun loadRosterEntryImage(id: String, size: Int, imageView: ImageView, @DrawableRes fallbackResId: Int = NO_FALLBACK, transformations: List<Transformation> = ArrayList()) {

        val imageRequest = ImageRequest.Builder(context)
            .data(RosterEntryImageParams(id, size))
            .target(imageView)

        if (fallbackResId != NO_FALLBACK) imageRequest.error(fallbackResId)
        if (transformations.isNotEmpty()) imageRequest.transformations(transformations)

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

fun ImageView.loadRosterEntryImage(baseUrl: String, id: String, size: Int, @DrawableRes fallbackResId: Int = NO_FALLBACK, transformations: List<Transformation> = ArrayList()) {
    RosterImageLoader.get(context, baseUrl).loadRosterEntryImage(id, size, this, fallbackResId, transformations)
}

abstract class PaletteTransformation: Transformation {
    override fun key() = "PaletteTransformation" + System.currentTimeMillis()

    override suspend fun transform(pool: BitmapPool, input: Bitmap, size: Size): Bitmap {
        paletteAvailable(Palette.from(input).generate())
        return input
    }

    abstract fun paletteAvailable(palette: Palette)
}