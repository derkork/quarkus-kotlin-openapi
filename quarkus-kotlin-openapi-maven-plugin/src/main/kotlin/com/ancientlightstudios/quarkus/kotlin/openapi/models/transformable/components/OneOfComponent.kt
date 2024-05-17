package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage

class OneOfComponent(override val schemas: List<SchemaUsage>, val discriminator: OneOfDiscriminator?) : SomeOfComponent

class OneOfDiscriminator(val property: String, val additionalMappings: Map<String, String>)
