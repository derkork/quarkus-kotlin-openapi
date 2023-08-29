package com.ancientlightstudios.quarkus.kotlin.openapi

sealed interface Result<T> {
    class Success<T>(val value:T):Result<T>
    class Failure(val errors:List<ValidationError>):Result<Nothing>
}