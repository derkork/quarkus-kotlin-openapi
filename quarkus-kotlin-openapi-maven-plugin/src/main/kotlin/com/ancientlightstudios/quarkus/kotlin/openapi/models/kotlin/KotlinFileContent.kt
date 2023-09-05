package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.strafbank.CodeWriter

abstract class KotlinFileContent(val name: Name.ClassName) {

    protected val annotations: MutableList<KotlinAnnotation> = mutableListOf()

    fun addAnnotation(name: Name.ClassName,  vararg  parameters: Pair<Name.VariableName, Any>) {
        annotations.add(KotlinAnnotation(name, *parameters))
    }

    abstract fun render(writer: CodeWriter)

}