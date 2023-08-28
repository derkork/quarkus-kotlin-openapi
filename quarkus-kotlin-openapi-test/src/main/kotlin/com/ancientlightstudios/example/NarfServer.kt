package com.ancientlightstudios.example;

import com.ancientlightstudios.example.server.NarfInterface

class NarfServer : NarfInterface {
    override suspend fun subscription_delete(
        filterName: String?,
        filter: String?,
        applyDefaultFilter: Boolean?,
        ApiEventData: String?,
        X_MATRIXX_SchemaNumber: String?,
        X_MATRIXX_ExtensionNumber: String?,
        deleteDevice: Boolean?,
        deleteSession: Boolean?,
        TrafficRouteData: String?,
        SearchTerm: String?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun subscription_detail(
        filterName: String?,
        filter: String?,
        applyDefaultFilter: Boolean?,
        ApiEventData: String?,
        X_MATRIXX_SchemaNumber: String?,
        X_MATRIXX_ExtensionNumber: String?,
        querySize: Double?,
        TrafficRouteData: String?,
        filterNonCatalogItem: Boolean?,
        filterInvalidPurchasedOffer: Boolean?,
        filterInvalidBalance: Boolean?,
        SearchTerm: String?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun subscription_modify_info(
        filterName: String?,
        filter: String?,
        applyDefaultFilter: Boolean?,
        ApiEventData: String?,
        X_MATRIXX_SchemaNumber: String?,
        X_MATRIXX_ExtensionNumber: String?,
        TrafficRouteData: String?,
        SearchTerm: String?
    ) {
        TODO("Not yet implemented")
    }
}
