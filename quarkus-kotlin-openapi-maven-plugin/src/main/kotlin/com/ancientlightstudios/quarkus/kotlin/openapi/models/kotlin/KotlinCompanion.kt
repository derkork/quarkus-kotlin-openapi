package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinCompanion(val className: ClassName?) {
    private val methods = mutableListOf<KotlinMethod>()

    fun render(writer: CodeWriter) = with(writer) {
        write("companion object ")
        if (className != null) {
            write(className.name)
            write(" ")
        }
        writeln("{")
        indent {
            writeln()
            methods.forEach {
                it.render(this)
                writeln()
            }
        }
        writeln("}")
    }

    fun addMethod(name: MethodName, suspend: Boolean, returnType: TypeName? = null, receiverType:TypeName? = null): KotlinMethod {
        val method = KotlinMethod(name, suspend, returnType, receiverType)
        methods.add(method)
        return method
    }
}