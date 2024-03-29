package com.tjek.sdk.api.models
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.graphics.RectF
import android.os.Parcelable
import android.util.SparseArray
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.*
import com.tjek.sdk.api.remote.RawJson
import com.tjek.sdk.publicationviewer.paged.utils.PolygonF
import kotlinx.parcelize.Parcelize
import okio.ByteString
import org.json.JSONObject
import kotlin.math.abs

@Parcelize
data class PublicationHotspotV2(
    val offer: HotspotOfferV2?,
    val pageLocations: SparseArray<PolygonF> = SparseArray()
): Parcelable {

    companion object {
        private const val significantArea: Double = 0.02

        fun fromDecodable(h: PublicationHotspotV2Decodable): PublicationHotspotV2 {
            val pageLocations = SparseArray<PolygonF>()
            val json = JSONObject(h.locations?.utf8() ?: "")
            json.keys().let {
                while (it.hasNext()) {
                    val page = it.next()
                    val intPage = try {
                        Integer.valueOf(page) - 1
                    } catch (e: NumberFormatException) {
                        continue
                    }
                    val location = json.getJSONArray(page)
                    val poly = PolygonF(location.length())
                    for (i in 0 until location.length()) {
                        val point = location.getJSONArray(i)
                        val x = (point.getString(0)).toFloat()
                        val y = (point.getString(1)).toFloat()
                        poly.addPoint(x, y)
                    }
                    pageLocations.append(intPage, poly)
                }
            }

            return PublicationHotspotV2 (
                offer = h.offer?.let { HotspotOfferV2.fromDecodable(it) },
                pageLocations = pageLocations
            )
        }
    }

    fun normalize(width: Double, height: Double) {
        val polygons = ArrayList<PolygonF>()
        for (p in getPages()) {
            polygons.add(pageLocations.get(p))
        }
        for (p in polygons) {
            for (i in 0 until p.npoints) {
                p.ypoints[i] = p.ypoints[i] / height.toFloat()
                p.xpoints[i] = p.xpoints[i] / width.toFloat()
            }
        }
    }

    private fun getPages(): IntArray {
        val pages = IntArray(pageLocations.size())
        for (i in 0 until pageLocations.size()) {
            pages[i] = pageLocations.keyAt(i)
        }
        return pages
    }

    fun getBoundsForPages(pages: IntArray): RectF? {
        var rect: RectF? = null
        val pagesLength = pages.size.toFloat()
        val pageOffset = 1f / pagesLength
        for (i in pages.indices) {
            val page = pages[i]
            val p: PolygonF? = pageLocations.get(page)
            p?.let{
                val r = RectF(p.bounds)
                r.right = r.right / pagesLength
                r.left = r.left / pagesLength
                r.offset(pageOffset * i.toFloat(), 0f)
                if (rect == null) {
                    rect = r
                } else {
                    rect!!.union(r)
                }
            }
        }
        return rect
    }

    fun hasLocationAt(visiblePages: IntArray, clickedPage: Int, x: Float, y: Float): Boolean {
        val p: PolygonF? = pageLocations.get(clickedPage)
        return p != null && p.contains(x, y) && isAreaSignificant(visiblePages, clickedPage)
    }

    private fun isAreaSignificant(visiblePages: IntArray, clickedPage: Int): Boolean {
        return !(visiblePages.size == 1 && pageLocations.size() > 1) || getArea(clickedPage) > significantArea
    }

    private fun getArea(page: Int): Double {
        val p: PolygonF? = pageLocations.get(page)
        return if (p == null) 0.0 else (abs(p.bounds.height()) * abs(p.bounds.width())).toDouble()
    }

}

@Parcelize
data class HotspotOfferV2(
    val id: Id,
    val heading: String,
    val runDateRange: ValidityPeriod,
    val publishDate: PublishDate?,
    val price: PriceV2?,
    val quantity: QuantityV2?
): Parcelable {

    companion object {
        fun fromDecodable(h: HotspotOfferV2Decodable): HotspotOfferV2 {
            // sanity check on the dates
            val fromDate = h.runFromDateStr?.toValidityDate(ValidityDateStrVersion.V2) ?: distantPast()
            val tillDate = h.runTillDateStr?.toValidityDate(ValidityDateStrVersion.V2) ?: distantFuture()

            return HotspotOfferV2(
                id = h.id,
                heading = h.heading,
                runDateRange = minOf(fromDate, tillDate)..maxOf(
                    fromDate,
                    tillDate
                ),
                publishDate = h.publishDateStr?.toValidityDate(ValidityDateStrVersion.V2),
                price = h.price,
                quantity = h.quantity
            )
        }
    }
}


//------------- Classes used for decoding api responses -------------//

@Keep
@JsonClass(generateAdapter = true)
data class HotspotOfferV2Decodable (
    val id: Id,
    val heading: String,
    @Json(name = "run_from")
    val runFromDateStr: ValidityDateStr?,
    @Json(name = "run_till")
    val runTillDateStr: ValidityDateStr?,
    @Json(name = "publish")
    val publishDateStr: ValidityDateStr?,
    @Json(name = "pricing")
    val price: PriceV2?,
    val quantity: QuantityV2?
)

@Keep
@JsonClass(generateAdapter = true)
data class PublicationHotspotV2Decodable (
    val offer: HotspotOfferV2Decodable?,
    @RawJson val locations: ByteString?
)