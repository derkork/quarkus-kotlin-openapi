package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import jakarta.ws.rs.core.Response

fun ResponseCode.asMethodName(): String {
    val httpCode = this as? ResponseCode.HttpStatusCode ?: return "defaultStatus"

    val reason = Response.Status.fromStatusCode(httpCode.value)?.reasonPhrase ?: "status${httpCode.value}"
    return methodNameOf(reason)
}