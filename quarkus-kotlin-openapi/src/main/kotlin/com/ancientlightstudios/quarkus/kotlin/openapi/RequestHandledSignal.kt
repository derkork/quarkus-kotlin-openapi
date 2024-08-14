package com.ancientlightstudios.quarkus.kotlin.openapi

import org.jboss.resteasy.reactive.RestResponse
import java.util.concurrent.CancellationException

class RequestHandledSignal(val response: RestResponse<*>) :
    CancellationException(
        "This is a signal that the request was handled. This should not be catched. If catched, " +
                "rethrow it, otherwise things will not work as intended."
    ) {

    // prevent creation of stack trace, as we don't need it and it hurts performance
    override fun fillInStackTrace(): Throwable {  // copied from Arrow
        // Prevent Android <= 6.0 bug.
        stackTrace = emptyArray()
        // We don't need stacktrace on shift, it hurts performance.
        return this
    }
}
