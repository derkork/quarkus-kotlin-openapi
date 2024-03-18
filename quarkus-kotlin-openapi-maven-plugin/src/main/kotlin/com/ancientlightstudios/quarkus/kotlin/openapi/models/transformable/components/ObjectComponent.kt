package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaProperty

class ObjectComponent(val properties: List<TransformableSchemaProperty> = listOf()) : SchemaComponent, StructuralComponent