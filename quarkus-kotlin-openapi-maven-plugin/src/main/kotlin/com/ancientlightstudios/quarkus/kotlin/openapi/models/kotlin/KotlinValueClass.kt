package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinValueClass(
    name: ClassName,
    private val nestedType: TypeName
) : KotlinFileContent(name) {

    init {
        addAnnotation("JvmInline".rawClassName())
    }

    private var companion: KotlinCompanion? = null

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)

        write("value class ${name.name}(val value: ")
        nestedType.render(this)
        write(")")

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

        writeln()
        writeln()
        writeln("fun String?.as${name.name}(context:String) : Maybe<${name.name}?> {")
        indent {
            write("return as")
            nestedType.render(this)
            writeln("(context)")
            indent {
                // TODO: add type validation here if necessary e.g. validateString { it.minLength(5) }
                writeln(".mapNotNull { ${name.name}(it) }")
            }
        }
        writeln("}")

        writeln()
        writeln("fun Maybe<String?>.as${name.name}() : Maybe<${name.name}?> = onNotNull { value.as${name.name}(context) }")
    }

    fun withCompanion(name: ClassName? = null, block: KotlinCompanion.() -> Unit) {
        if (this.companion == null) {
            this.companion = KotlinCompanion(name)
        }
        block(this.companion!!)
    }
}
