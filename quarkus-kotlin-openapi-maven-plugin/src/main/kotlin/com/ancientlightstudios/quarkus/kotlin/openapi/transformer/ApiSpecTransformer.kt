package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec

fun transform(apiSpec: ApiSpec, config: Config): List<KotlinFile> {
    val queue = mutableSetOf<QueueItem>()
    val done = mutableSetOf<QueueItem>()

    if (config.interfaceType == InterfaceType.CLIENT || config.interfaceType == InterfaceType.BOTH) {
        queue.add(ClientInterfaceQueueItem(config, apiSpec.requests))
    }

    if (config.interfaceType == InterfaceType.SERVER || config.interfaceType == InterfaceType.BOTH) {
        queue.add(ServerInterfaceQueueItem(config, apiSpec.requests))
    }

    val result = mutableListOf<KotlinFile>()
    while (queue.isNotEmpty()) {
        val item = queue.first()
        queue.remove(item)

        if (!done.contains(item)) {
            done.add(item)
            val element = item.generate { queue.add(it) }
            if (element != null) {
                result.add(element)
            }
        }
    }

    return result
}
