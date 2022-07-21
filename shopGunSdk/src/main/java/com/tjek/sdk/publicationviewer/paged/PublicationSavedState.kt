package com.tjek.sdk.publicationviewer.paged

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PublicationSavedState(
    val config: PagedPublicationConfiguration,
    val hasSentOpenEvent: Boolean
): Parcelable