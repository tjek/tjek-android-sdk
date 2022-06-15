package com.tjek.sdk.api.models

import android.os.Parcel
import android.os.Parcelable
import com.tjek.sdk.api.ValidityDate
import java.time.LocalDateTime
import java.time.ZoneOffset

// Note  run_till (v2) is including. v4/valid_until is excluding. So 2022-12-31T23:00:00.000Z in v4 means when this time occurs, it's expired.

class ValidityPeriod(
    override val start: ValidityDate,
    override val endInclusive: ValidityDate
) : Iterable<ValidityDate>, ClosedRange<ValidityDate>, Parcelable {

    constructor(parcel: Parcel) : this(
        ValidityDate.of(LocalDateTime.ofEpochSecond(parcel.readLong(), 0, ZoneOffset.UTC), ZoneOffset.UTC),
        ValidityDate.of(LocalDateTime.ofEpochSecond(parcel.readLong(), 0, ZoneOffset.UTC), ZoneOffset.UTC)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(start.toEpochSecond())
        parcel.writeLong(endInclusive.toEpochSecond())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ValidityPeriod> {
        override fun createFromParcel(parcel: Parcel): ValidityPeriod {
            return ValidityPeriod(parcel)
        }

        override fun newArray(size: Int): Array<ValidityPeriod?> {
            return arrayOfNulls(size)
        }
    }

    override fun iterator(): Iterator<ValidityDate> {
        return ValidityDateIterator(start, endInclusive, 1)
    }

}

operator fun ValidityDate.rangeTo(other: ValidityDate) = ValidityPeriod(this, other)

class ValidityDateIterator(
    startDate: ValidityDate,
    private val endDateInclusive: ValidityDate,
    private val stepDays: Long): Iterator<ValidityDate> {

    private var currentDate = startDate

    override fun hasNext() = currentDate.plusDays(stepDays) <= endDateInclusive

    override fun next(): ValidityDate {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}