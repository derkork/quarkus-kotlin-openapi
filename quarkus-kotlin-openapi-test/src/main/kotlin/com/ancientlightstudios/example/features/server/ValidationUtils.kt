package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.OperationError
import com.ancientlightstudios.quarkus.kotlin.openapi.ValidationError

fun List<ValidationError>.toOperationError() = OperationError(map { "${it.path}: ${it.message}" })
