package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import jakarta.ws.rs.core.Response

fun Int.statusCodeReason() = Response.Status.fromStatusCode(this)?.reasonPhrase ?: "status${this}"
