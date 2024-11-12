package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaComponent

class ComponentInspection<T : SchemaComponent>(val component: T)