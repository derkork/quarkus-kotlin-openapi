package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request

class ClientInterfaceQueueItem(private val config: Config, private val requests: Set<Request>) : QueueItem {

    override fun generate(): KotlinFile {

        val clientInterface = KotlinInterface("${config.interfaceName}Client".className()).apply {
            addPathAnnotation("/")
        }

        return KotlinFile(clientInterface, "${config.packageName}.client").apply {
            imports.addAll(jakartaRestImports())
        }
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
