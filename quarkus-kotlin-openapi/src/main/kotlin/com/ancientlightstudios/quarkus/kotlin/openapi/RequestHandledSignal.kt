package com.ancientlightstudios.quarkus.kotlin.openapi

import org.jboss.resteasy.reactive.RestResponse

class RequestHandledSignal(val response: RestResponse<*>) : Exception(null, null, false, false)