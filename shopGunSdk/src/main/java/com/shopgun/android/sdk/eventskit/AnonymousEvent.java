package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonObject;
import com.shopgun.android.sdk.utils.SgnUtils;

import java.util.concurrent.TimeUnit;

import io.realm.RealmModel;

/**
 * New anonymous format for events
 */
public class AnonymousEvent {

    public static final String TAG = AnonymousEvent.class.getSimpleName();

    /* The json_event version scheme to use */
    private static final int VERSION = 2;

    /* Default json_event type = empty json_event */
    public static final int DEFAULT_TYPE = 0;

    /* Predefined events */
    public static final int PAGED_PUBLICATION_OPENED = 1;
    public static final int PAGED_PUBLICATION_PAGE_DISAPPEARED = 2;
    public static final int OFFER_OPENED = 3;
    public static final int CLIENT_SESSION_OPENED = 4;
    public static final int SEARCHED = 5;

    private boolean mDoNotTrack;

    private JsonObject json_event;

    private int type;
    private String id;
    private long timestamp;

    /**
     * @param type integer code for the json_event type. The sdk defines a few basics types
     *             DEFAULT_TYPE = 0;
     *             PAGED_PUBLICATION_OPENED = 1;
     *             PAGED_PUBLICATION_PAGE_DISAPPEARED = 2;
     *             OFFER_OPENED = 3;
     *             CLIENT_SESSION_OPENED = 4;
     *             SEARCHED = 5;
     * @param applicationTrackId is given to you by ShopGun and hardcoded into the manifest as meta data
     *             "com.shopgun.android.sdk.eventskit.application_track_id" for production
     *             "com.shopgun.android.sdk.develop.eventskit.application_track_id" for developing
     *             Note: for internal purposes, assign an empty string
     */
    public AnonymousEvent(int type, String applicationTrackId) {
        id = SgnUtils.createUUID();
        timestamp = timestamp();
        this.type = type;

        json_event = new JsonObject();
        json_event.addProperty("_v", VERSION);
        json_event.addProperty("_i", id);
        json_event.addProperty("_e", type);
        json_event.addProperty("_t", timestamp);
        json_event.addProperty("_a", applicationTrackId);
    }

    private long timestamp() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    public AnonymousEvent add(String property, String value) {
        json_event.addProperty(property, value);
        return this;
    }


    /****** Common fields */

    public AnonymousEvent addUserLocation(String geohash, long timestamp) {
        json_event.addProperty("l.h", geohash);
        json_event.addProperty("l.ht", timestamp);
        return this;
    }

    public AnonymousEvent addUserCountry(String countryCode) {
        json_event.addProperty("l.c", countryCode);
        return this;
    }

    public AnonymousEvent addViewtoken(String viewToken) {
        json_event.addProperty("vt", viewToken);
        return this;
    }


    /****** Fields for predefined events */

    public AnonymousEvent addPublicationOpened(String publicationId) {
        json_event.addProperty("pp.id", publicationId);
        return this;
    }

    public AnonymousEvent addPageOpened(String publicationId, int page) {
        this.addPublicationOpened(publicationId);
        json_event.addProperty("ppp.n", page);
        return this;
    }

    public AnonymousEvent addOfferOpened(String offerId) {
        json_event.addProperty("of.id", offerId);
        return this;
    }

    public AnonymousEvent addSearch(String query, String language) {
        json_event.addProperty("sea.q", query);
        json_event.addProperty("sea.l", language);
        return this;
    }


    /****** Getters and setters */

    public boolean doNotTrack() {
        return mDoNotTrack;
    }

    public void doNotTrack(boolean doNotTrack) {
        mDoNotTrack = doNotTrack;
    }

    @Override
    public String toString() {
        return json_event.toString();
    }

    public int getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getVersion() {
        return VERSION;
    }
}
