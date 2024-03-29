@file:Suppress("unused")

package com.tjek.sdk.api
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
import com.tjek.sdk.api.models.*
import com.tjek.sdk.api.remote.*
import com.tjek.sdk.api.remote.request.*
import com.tjek.sdk.api.remote.request.APIRequest

object TjekAPI {

    /**
    A request that returns a paginated list of publications, limited by the parameters.

    Parameters:
        - businessIds: Limit the list of publications by the id of the business that published them.
        - storeIds: Limit the list of publications by the ids of the stores they cover.
        - near: Specify a coordinate to return publications in relation to. Also optionally limit the publications to within a max radius from that coordinate.
        - acceptedTypes: Choose which types of publications to return (defaults to all)
        - pagination: The count & cursor of the request's page. Defaults to the first page of 24 publications. `itemCount` must not be more than 100. `startCursor` must not be greater than 1000.

    Returns:
        A list of `PublicationV2`.
     */
    suspend fun getPublications(
        businessIds: Array<Id> = emptyArray(),
        storeIds: Array<Id> = emptyArray(),
        nearLocation: LocationQuery? = null,
        acceptedTypes: Array<PublicationType> = PublicationType.values(),
        pagination: PaginatedRequestV2 = PaginatedRequestV2.firstPage(24)
    ): ResponseType<PaginatedResponse<List<PublicationV2>>> {
        return APIRequest.getPublications(businessIds, storeIds, nearLocation, acceptedTypes, pagination)
    }


    /**
    A request that asks for a specific publication, based on its Id.

    Parameters:
        - publicationId: The Id of the specific publication we are looking for.

    Returns: a response type of `PublicationV2`.
     */
    suspend fun getPublication(publicationId: Id): ResponseType<PublicationV2> {
        return APIRequest.getPublication(publicationId)
    }

    /**
    A request that asks for a specific store, based on its Id.

    Parameters:
        - storeId: The Id of the specific store we are looking for.

    Returns:
        A response type of `StoreV2`.
     */
    suspend fun getStore(storeId: Id): ResponseType<StoreV2> {
        return APIRequest.getStore(storeId)
    }

    /**
    A request that returns a paginated list of stores, limited by the parameters.

    Parameters:
        - offerIds: Limit the list of stores by the ids of the offers it contains.
        - publicationIds: Limit the list of stores by the ids of the publications it has.
        - businessIds: Limit the list of stores by the ids of the businesses that run them.
        - nearLocation: Specify a coordinate to return stores in relation to. Also optionally limit the stores to within a max radius from that coordinate.
        - sortedBy: An array of sort keys, defining which order we want the stores returned in. If left empty the server decides.
        - pagination: The count & cursor of the request's page. Defaults to the first page of 24 stores. `itemCount` must not be more than 100. `startCursor` must not be greater than 1000.

    Returns:
        A response type of a paginated array of `StoreV2`.
     */
    suspend fun getStores(
        offerIds: Array<Id> = emptyArray(),
        publicationIds: Array<Id> = emptyArray(),
        businessIds: Array<Id> = emptyArray(),
        nearLocation: LocationQuery? = null,
        sortOrder: Array<StoresRequestSortOrder> = emptyArray(),
        pagination: PaginatedRequestV2 = PaginatedRequestV2.firstPage(24)
    ): ResponseType<PaginatedResponse<List<StoreV2>>> {
        return APIRequest.getStores(offerIds, publicationIds, businessIds, nearLocation, sortOrder, pagination)
    }

    /**
    A request that asks for a specific offer, based on its Id.

    Parameters:
        - offerId: The Id of the specific offer we are looking for.

    Returns:
        A response type of `OfferV2`.
     */
    suspend fun getOffer(offerId: Id): ResponseType<OfferV2> {
        return APIRequest.getOffer(offerId)
    }

    /**
    A request that returns a paginated list of offers, limited by the parameters.

    Parameters:
        - publicationIds: Limit the list of offers by the id of the publication that its in.
        - businessIds: Limit the list of offers by the id of the business that published them.
        - storeIds: Limit the list of offers by the ids of the stores they are in.
        - nearLocation: Specify a coordinate to return offers in relation to. Also optionally limit the offers to within a max radius from that coordinate.
        - pagination: The count & cursor of the request's page. Defaults to the first page of 24 offers. `itemCount` must not be more than 100. `startCursor` must not be greater than 1000.

    Returns:
        A list of `OfferV2` in a paginated object.
     */
    suspend fun getOffers(
        publicationIds: Array<Id> = emptyArray(),
        businessIds: Array<Id> = emptyArray(),
        storeIds: Array<Id> = emptyArray(),
        nearLocation: LocationQuery? = null,
        pagination: PaginatedRequestV2 = PaginatedRequestV2.firstPage(24)
    ): ResponseType<PaginatedResponse<List<OfferV2>>> {
        return APIRequest.getOffers(publicationIds, businessIds, storeIds, nearLocation, pagination)
    }

    /**
    Builds a request that, when performed, will fetch all the active offers matching a `searchString`. The results can optionally be limited to those that have been published by a list of specified `Business` ids, and by their proximity to a location.

    Parameters:
        - matchingSearch: The string to search for.
        - businessIds: Limit the list of offers by the id of the business that published them. Defaults to empty.
        - nearLocation: Specify a coordinate to return offers in relation to. Also optionally limit the offers to within a max radius from that coordinate.
        - pagination: The count & cursor of the request's page. Defaults to the first page of 24 offers. `itemCount` must not be more than 100. `startCursor` must not be greater than 1000.

    Returns:
        A list of `OfferV2` in a paginated object.
     */
    suspend fun getOffers(
        matchingSearch: String,
        businessIds: Array<Id> = emptyArray(),
        nearLocation: LocationQuery? = null,
        pagination: PaginatedRequestV2 = PaginatedRequestV2.firstPage(24)
    ): ResponseType<PaginatedResponse<List<OfferV2>>> {
        return if (matchingSearch.isEmpty())
            getOffers(businessIds = businessIds, nearLocation = nearLocation, pagination = pagination)
        else
            APIRequest.getOffers(matchingSearch, businessIds, nearLocation, pagination)
    }

    /**
    Retrieve an offer from Incito.
    Parameter:
        - incitoOffer: the offer clicked in the incito publication.

    Returns:
        An `OfferV4` object.
     */
    suspend fun getOfferFromIncito(
        incitoOffer: IncitoOffer
    ): ResponseType<OfferV4> {
        return APIRequest.getOfferFromIncito(incitoOffer.viewId, incitoOffer.publicationId)
    }

    /**
    Retrieve an offer from Incito.
    Parameter:
        - viewId: the id of the view clicked in the incito publication
        - publicationId: the id of the incito publication

    Returns:
        An `OfferV4` object.
     */
    suspend fun getOfferFromIncito(
        viewId: IncitoViewId,
        publicationId: Id
    ): ResponseType<OfferV4> {
        return APIRequest.getOfferFromIncito(viewId, publicationId)
    }

    /**
    A request that asks for a specific business, based on its Id.

    Parameters:
        - businessId: The Id of the specific business we are looking for.

    Returns:
        A response type of `BusinessV2`.
     */
    suspend fun getBusiness(businessId: Id): ResponseType<BusinessV2> {
        return APIRequest.getBusiness(businessId)
    }

    /**
     * Fetches all the pages for the specified publication
     */
    suspend fun getPublicationPages(
        publicationId: Id,
        aspectRatio: Double? = null
    ): ResponseType<List<PublicationPageV2>> {
        return APIRequest.getPublicationPages(publicationId, aspectRatio)
    }

    /**
     * Fetch all hotspots for the specified publication.
     * Width and height of the publication are needed in order to position the hotspots correctly.
     */
    suspend fun getPublicationHotspots(
        publicationId: Id,
        width: Double,
        height: Double
    ): ResponseType<List<PublicationHotspotV2>> {
        return APIRequest.getPublicationHotspots(publicationId, width, height)
    }

    /**
     * Get an incito publication.
     */
    suspend fun getIncito(
        id: Id,
        deviceCategory: IncitoDeviceCategory,
        orientation: IncitoOrientation,
        pixelRatio: Float,
        maxWidth: Int,
        featureLabels: List<FeatureLabel>?,
        locale: String?
    ): ResponseType<IncitoData> {
        return APIRequest.getIncito(id, deviceCategory, orientation, pixelRatio, maxWidth, featureLabels, locale)
    }
}