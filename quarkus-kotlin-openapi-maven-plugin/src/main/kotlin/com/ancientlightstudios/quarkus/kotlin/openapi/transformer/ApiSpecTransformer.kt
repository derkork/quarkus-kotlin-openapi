package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import TransformerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec

fun transform(apiSpec: ApiSpec, config: Config): List<KotlinFile> {

    val context = TransformerContext(config)

    if (config.interfaceType == InterfaceType.CLIENT || config.interfaceType == InterfaceType.BOTH) {
        context.enqueue(ClientInterfaceQueueItem(config, apiSpec.requests))
    }

    if (config.interfaceType == InterfaceType.SERVER || config.interfaceType == InterfaceType.BOTH) {
        context.enqueue(ServerInterfaceQueueItem(apiSpec.requests, context))
    }

    return context.run()
}
