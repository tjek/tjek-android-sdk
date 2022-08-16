package com.tjek.sdk.eventstracker

import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.eventstracker.api.EventStatus
import com.tjek.sdk.eventstracker.api.ShipEventRequest
import java.util.concurrent.TimeUnit

internal class EventShipper(
    private val eventDao: EventDao
) {

    private val tag = "EventShipper"

    suspend fun shipEvents() {
        val toBeShipped = eventDao.getEvents()
        if (toBeShipped.isEmpty()) {
            TjekLogCat.v("$tag: no event to ship at the moment")
            return
        }

        TjekLogCat.v("$tag shipping ${toBeShipped.size}")
        when (val res = ShipEventRequest.shipEvents(toBeShipped)) {
            is ResponseType.Error -> {
                TjekLogCat.e("$tag: ${res.errorType.toFormattedString()}")
                return
            }
            is ResponseType.Success -> {
                // delete ack-ed events
                val ack = res.data!!.events.filter { it.status == EventStatus.ack }.map { it.id }
                if (ack.isNotEmpty()) {
                    eventDao.deleteEvents(ack)
                }
                // check timestamp for nack-ed events and delete them if they're too old
                val nack = res.data.events
                    .filter { it.status == EventStatus.nack }
                    .also { if (it.isNotEmpty()) TjekLogCat.w("$tag nack events $it") }
                    .map { it.id }
                val timeLimit =
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - TimeUnit.HOURS.toSeconds(36)
                val oldNack = toBeShipped.filter { it.timestamp < timeLimit && nack.contains(it.id) }.map { it.id }
                if (nack.isNotEmpty()) {
                    eventDao.deleteEvents(oldNack)
                }
                // other status
                val other =
                    res.data.events
                        .filter { it.status != EventStatus.ack && it.status != EventStatus.nack }
                        .also { if (it.isNotEmpty()) TjekLogCat.w("$tag $it") }
                        .map { it.id }
                if (other.isNotEmpty()) {
                    eventDao.deleteEvents(other)
                }

                // print some logs
                TjekLogCat.v("$tag: Ack=${ack.size}, Nack=${nack.size} (${oldNack.size} too old), other=${other.size}")
            }
        }
    }
}