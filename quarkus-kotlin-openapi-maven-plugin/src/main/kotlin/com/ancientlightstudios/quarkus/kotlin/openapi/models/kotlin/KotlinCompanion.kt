package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinCompanion(private val identifier: ClassName? = null) : MethodAware {

    private val methods = KotlinMethodContainer()

    override fun addMethod(method: KotlinMethod) {
        methods.addMethod(method)
    }

    fun render(writer: CodeWriter) = with(writer) {
        write("companion object ")
        if (identifier != null) {
            write("${identifier.render()} ")
        }
        writeln("{")
        indent {
            writeln()
            methods.render(this)
            writeln(forceNewLine = false) // in case the item already rendered a line break
            writeln()
        }
        writeln("}")
    }
}

interface CompanionAware {

    fun setCompanion(companion: KotlinCompanion)

}

fun CompanionAware.kotlinCompanion(identifier: ClassName? = null, block: KotlinCompanion.() -> Unit) {
    val content = KotlinCompanion(identifier).apply(block)
    setCompanion(content)
}
