package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object SchemaDefinitionUsageHint : Hint<MutableSet<TransformableSchemaUsage>> {

    val TransformableSchemaDefinition.usage: Set<TransformableSchemaUsage>
        get() = getOrPut(SchemaDefinitionUsageHint) { mutableSetOf() }

    fun TransformableSchemaDefinition.addUsage(usage: TransformableSchemaUsage) {
        if (!getOrPut(SchemaDefinitionUsageHint) { mutableSetOf() }.add(usage)) {
            ProbableBug("Usage ${usage.originPath} already registered for schema definition $originPath")
        }
    }

    fun TransformableSchemaDefinition.removeUsage(usage: TransformableSchemaUsage) {
        if (!getOrPut(SchemaDefinitionUsageHint) { mutableSetOf() }.remove(usage)) {
            ProbableBug("Usage ${usage.originPath} not registered for schema definition $originPath")
        }
    }

    fun TransformableSchemaDefinition.clearUsage() {
        get(SchemaDefinitionUsageHint)?.clear()
    }
}