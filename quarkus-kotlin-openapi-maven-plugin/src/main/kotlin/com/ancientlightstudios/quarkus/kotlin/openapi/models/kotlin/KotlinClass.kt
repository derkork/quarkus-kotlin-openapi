package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.renderParameterBlock
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinClass(
    name: ClassName,
    private val asDataClass: Boolean = false,
    private val privateConstructor: Boolean = false
) : KotlinFileContent(name) {

    private val parameters = mutableListOf<KotlinMember>()
    private var companion: KotlinCompanion? = null


    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)

        if (asDataClass) {
            write("data ")
        }

        write("class ${name.name}")

        if (privateConstructor) {
            write(" private constructor")
        }

        if (parameters.isNotEmpty() || privateConstructor) {
            write("(")
            renderParameterBlock(parameters) { it.render(this) }
            write(")")
        }

        if (methods.isNotEmpty() || companion != null) {
            writeln(" {")
            indent {
                writeln()
                methods.forEach {
                    it.render(this)
                    writeln()
                }

                companion?.let {
                    it.render(this)
                    writeln()
                }
            }
            write("}")
        }
    }

    fun addMember(
        name: VariableName,
        typeName: TypeName,
        mutable: Boolean = false,
        private: Boolean = true
    ): KotlinMember {
        val result = KotlinMember(name, typeName, mutable, private)
        parameters.add(result)
        return result
    }

    fun withCompanion(name: ClassName? = null, block: KotlinCompanion.() -> Unit) {
        if (this.companion == null) {
            this.companion = KotlinCompanion(name)
        }
        block(this.companion!!)
    }
}
