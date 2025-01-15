package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.asTypeReference
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Kotlin
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeReference
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Library
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.asTypeReference
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

fun ModelUsage.asUnsafeTypeReference(): KotlinTypeReference = when (instance) {
    is CollectionModelInstance -> Kotlin.List.asTypeReference(instance.items.asUnsafeTypeReference())
    is EnumModelInstance -> instance.ref.name.asTypeReference()
    is MapModelInstance -> Kotlin.Map.asTypeReference(
        Kotlin.String.asTypeReference(),
        instance.items.asUnsafeTypeReference()
    )

    is ObjectModelInstance -> Library.UnsafeJson.asTypeReference(instance.ref.name.asTypeReference())
    is OneOfModelInstance -> Library.UnsafeJson.asTypeReference(instance.ref.name.asTypeReference())
    is PrimitiveTypeModelInstance -> instance.itemType.asTypeReference()
}.run {
    when (isNullable()) {
        true -> this.acceptNull()
        else -> this
    }
}

