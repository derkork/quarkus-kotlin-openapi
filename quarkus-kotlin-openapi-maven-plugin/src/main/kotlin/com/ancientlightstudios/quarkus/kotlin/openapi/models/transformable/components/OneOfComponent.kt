package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class OneOfComponent(override val schemas: List<SchemaUsage>, val discriminator: OneOfDiscriminator?) :
    SomeOfComponent {

    companion object {

        fun TransformableSchema.oneOfComponent(): OneOfComponent? {
            val components = components.filterIsInstance<OneOfComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of oneOf component found at schema $originPath")
                else -> components.first()
            }
        }

    }

}

class OneOfDiscriminator(val property: String, val additionalMappings: Map<String, String>)
