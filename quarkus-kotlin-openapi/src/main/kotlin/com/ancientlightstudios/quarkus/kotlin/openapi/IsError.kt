package com.ancientlightstudios.quarkus.kotlin.openapi

interface IsError {
    val errorMessage: String
}

interface IsTimeoutError : IsError
interface IsUnreachableError : IsError

interface IsConnectionResetError : IsError

interface IsUnknownError : IsError {
    val cause: Exception
}

interface IsResponseError : IsError