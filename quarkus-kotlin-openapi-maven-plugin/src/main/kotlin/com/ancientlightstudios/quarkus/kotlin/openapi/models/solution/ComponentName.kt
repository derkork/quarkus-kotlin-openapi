package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

data class ComponentName(var name: String, var packageName: String, val strategy: ConflictResolution = ConflictResolution.Generated)

enum class ConflictResolution {

    // Name for a core part of the generated solution. Should not be changed
    Pinned,

    // Name requested by the developer. Should not be changed unless it collides with a pinned name
    Requested,

    // Name selected by the generator. Will be changed if necessary
    Generated

}