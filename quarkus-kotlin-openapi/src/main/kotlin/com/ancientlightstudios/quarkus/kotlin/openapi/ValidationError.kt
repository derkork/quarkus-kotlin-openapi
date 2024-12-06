package com.ancientlightstudios.quarkus.kotlin.openapi

data class ValidationError(val message: String, val path: String, val kind: ErrorKind)

@JvmInline
value class ErrorKind(val value: String) {

    companion object {
        val Missing = ErrorKind("missing")
        val Incompatible = ErrorKind("incompatible")
        val Invalid = ErrorKind("invalid")
        val Unknown = ErrorKind("unknown")
    }

}

