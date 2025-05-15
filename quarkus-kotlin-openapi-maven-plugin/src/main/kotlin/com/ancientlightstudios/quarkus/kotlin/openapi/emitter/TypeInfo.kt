package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeReference

data class TypeInfo(val type: KotlinTypeReference, val defaultValue: DefaultValue = DefaultValue.None)