package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

class ArrayValidationComponent(val minSize: Int? = null, val maxSize: Int? = null) : SchemaDefinitionComponent,
    MetaComponent
