package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

abstract class KotlinFileContent(val name: Name.ClassName) {

    val annotations = KotlinAnnotationContainer()
    val methods = mutableListOf<KotlinMethod>()

    abstract fun render(writer: CodeWriter)

}