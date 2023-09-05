package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile

abstract class QueueItem {
    abstract fun generate(config: Config, queue: (QueueItem) -> Unit): KotlinFile?
}