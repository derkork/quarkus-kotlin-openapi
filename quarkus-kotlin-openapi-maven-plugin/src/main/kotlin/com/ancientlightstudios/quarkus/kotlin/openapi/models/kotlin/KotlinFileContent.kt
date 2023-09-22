package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.addPath
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

abstract class KotlinFileContent(val name: ClassName) {

    protected val annotations = KotlinAnnotationContainer()
    protected val methods = mutableListOf<KotlinMethod>()

    abstract fun render(writer: CodeWriter)

    fun addMethod(
        name: MethodName,
        suspend: Boolean,
        returnType: TypeName? = null,
        receiverType: TypeName? = null,
    ): KotlinMethod {
        val method = KotlinMethod(name, suspend, returnType, receiverType)
        methods.add(method)
        return method
    }

    fun addAnnotation(name: ClassName, vararg parameters: Pair<VariableName, Any>) {
        annotations.add(name, *parameters)
    }

    fun addPathAnnotation(path: String) {
        annotations.addPath(path)
    }

}