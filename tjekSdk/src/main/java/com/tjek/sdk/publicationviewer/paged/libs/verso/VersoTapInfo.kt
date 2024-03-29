package com.tjek.sdk.publicationviewer.paged.libs.verso
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
import android.view.View
import com.tjek.sdk.publicationviewer.paged.utils.NumberUtils
import com.tjek.sdk.publicationviewer.paged.libs.zoomlayout.TapInfo
import java.util.*
import kotlin.math.floor

@Suppress("unused", "MemberVisibilityCanBePrivate")
data class VersoTapInfo(
    private val info: TapInfo,
    val fragment: VersoPageViewFragment
) {

    val position: Int = fragment.mPosition
    val pages: IntArray = Arrays.copyOf(fragment.mPages, fragment.mPages.size)
    val pageTapped: Int

    constructor(info: VersoTapInfo) : this(info.info, info.fragment)

    fun getView(): View = info.view

    fun getX(): Float  = info.absoluteX

    fun getY(): Float  = info.absoluteY

    fun getRelativeX(): Float = info.relativeX

    fun getRelativeY(): Float = info.relativeY

    fun getPercentX(): Float = info.percentX

    fun getPercentY(): Float = info.percentY

    fun isContentClicked(): Boolean = info.contentClicked


    override fun toString(): String {
        return java.lang.String.format(
            Locale.ENGLISH,
            strFormat,
            position,
            pageTapped,
            pages.joinToString(),
            getX(),
            getY(),
            getRelativeX(),
            getRelativeY(),
            getPercentX(),
            getPercentY(),
            isContentClicked()
        )
    }

    companion object {
        private const val strFormat =
            "VersoTapInfo[ position:%s, pageTapped:%s, pages:%s, absX:%.0f, absY:%.0f, relX:%.0f, relY:%.0f, percentX:%.2f, percentY:%.2f, contentClicked:%s ]"
        const val NO_CONTENT = -1
    }

    init {
        val pageWidth = 1f / pages.size.toFloat()
        // if the percent is exactly 1.0 then the pagePos will be off by one so we'll clamp the result
        val x: Float = NumberUtils.clamp(0f, getPercentX(), 0.999f)
        val pagePos = floor((x / pageWidth).toDouble()).toInt()
        pageTapped = if (isContentClicked()) pages[pagePos] else NO_CONTENT
    }
}