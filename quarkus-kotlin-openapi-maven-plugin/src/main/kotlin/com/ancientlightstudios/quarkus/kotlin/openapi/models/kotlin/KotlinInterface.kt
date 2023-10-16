package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinInterface(private val name: ClassName) : KotlinFileContent, AnnotationAware, MethodAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinMethodContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addMethod(method)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, false)
        write("interface ${name.render()}")

        if (methods.isNotEmpty) {
            writeln(" {")
            indent {
                writeln()
                methods.render(this)
                writeln(forceNewLine = false) // in case the item already rendered a line break
                writeln()
            }
            write("}")
        }
    }

}

fun KotlinFile.kotlinInterface(name: ClassName, block: KotlinInterface.() -> Unit) {
    val content = KotlinInterface(name).apply(block)
    addFileContent(content)
}
