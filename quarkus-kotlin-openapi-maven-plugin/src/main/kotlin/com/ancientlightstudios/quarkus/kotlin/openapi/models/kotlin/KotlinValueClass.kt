package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinValueClass(private val name: ClassName, private val nestedType: TypeName) : KotlinFileContent,
    AnnotationAware, MethodAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinMethodContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addMethod(method)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this)

        write("value class ${name.render()}(val value: ${nestedType.render()})")

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

        // companion
    }

}

fun KotlinFile.kotlinValueClass(name: ClassName, nestedType: TypeName, block: KotlinValueClass.() -> Unit) {
    val content = KotlinValueClass(name, nestedType).apply(block)
    addFileContent(content)
}
