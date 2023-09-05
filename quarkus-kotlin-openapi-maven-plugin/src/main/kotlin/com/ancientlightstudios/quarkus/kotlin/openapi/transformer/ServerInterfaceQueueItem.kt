package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Name
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ServerInterfaceQueueItem(requests: Set<Request>) : QueueItem() {


    override fun generate(config: Config, queue: (QueueItem) -> Unit): KotlinFile {

        val serverInterface = KotlinClass(Name.ClassName(config.interfaceName + "Server"))
        serverInterface.addAnnotation(
            Name.ClassName("Path"),
            Name.VariableName("value") to "/"
        )

        return KotlinFile(serverInterface, config.packageName + ".server", listOf(
            "javax.ws.rs.Path",
        ))
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
