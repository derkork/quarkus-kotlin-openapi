package com.ancientlightstudios.example;

import com.ancientlightstudios.example.model.MtxRequestSubscriptionModifyUnsafe
import com.ancientlightstudios.example.model.MtxResponse
import com.ancientlightstudios.example.model.MtxResponseSubscription
import com.ancientlightstudios.example.server.NarfInterface
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Path
import java.util.UUID


@ApplicationScoped
@Path("/")
class NarfServer : NarfInterface {
    // lf4j logger
    private val log = org.slf4j.LoggerFactory.getLogger(NarfServer::class.java)

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
    ): MtxResponse {
        // log all input
        log.info("subscription_delete: filterName=$filterName, filter=$filter, applyDefaultFilter=$applyDefaultFilter, ApiEventData=$ApiEventData, X_MATRIXX_SchemaNumber=$X_MATRIXX_SchemaNumber, X_MATRIXX_ExtensionNumber=$X_MATRIXX_ExtensionNumber, deleteDevice=$deleteDevice, deleteSession=$deleteSession, TrafficRouteData=$TrafficRouteData, SearchTerm=$SearchTerm")
        return MtxResponse("subscription_delete", 27, 32, UUID.randomUUID().toString(), 0, "Klasse, alter!", 4711)
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
    ): MtxResponseSubscription {
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
        SearchTerm: String?,
        body: MtxRequestSubscriptionModifyUnsafe?
    ): MtxResponse {
        TODO("Not yet implemented")
    }

}
