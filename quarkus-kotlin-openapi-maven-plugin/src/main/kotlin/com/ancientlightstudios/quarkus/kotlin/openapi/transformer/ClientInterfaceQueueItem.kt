package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Name
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ClientInterfaceQueueItem(requests: Set<Request>) : QueueItem() {
    override fun generate(config: Config, queue: (QueueItem) -> Unit): KotlinFile {

        val clientInterface = KotlinInterface(Name.ClassName(config.interfaceName + "Client"))
        clientInterface.annotations.add(
            Name.ClassName("Path"),
            Name.VariableName("value") to "/"
        )

        return KotlinFile(
            clientInterface, config.packageName + ".client", listOf(
                "javax.ws.rs.Path",
            )
        )
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
