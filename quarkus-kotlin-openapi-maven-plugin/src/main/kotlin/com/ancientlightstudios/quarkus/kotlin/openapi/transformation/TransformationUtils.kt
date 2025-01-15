package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import jakarta.ws.rs.core.Response

fun ResponseCode.asMethodName(): String {
    val httpCode = this as? ResponseCode.HttpStatusCode ?: return "default"

    val reason = Response.Status.fromStatusCode(httpCode.value)?.reasonPhrase ?: "status${httpCode.value}"
    return methodNameOf(reason)
}

fun ModelInstance.unwrapModelClass(): ModelClass? = when (this) {
    is CollectionModelInstance -> items.instance.unwrapModelClass()
    is EnumModelInstance -> ref
    is MapModelInstance -> items.instance.unwrapModelClass()
    is ObjectModelInstance -> ref
    is OneOfModelInstance -> ref
    is PrimitiveTypeModelInstance -> null
}
