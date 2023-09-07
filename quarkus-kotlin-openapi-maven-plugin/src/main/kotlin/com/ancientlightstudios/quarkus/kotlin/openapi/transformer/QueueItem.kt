package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile

interface QueueItem {
    fun generate(queue: (QueueItem) -> Unit): KotlinFile?
}