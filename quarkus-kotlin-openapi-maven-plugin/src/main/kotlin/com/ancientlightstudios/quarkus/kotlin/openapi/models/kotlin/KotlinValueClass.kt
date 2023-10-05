package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinValueClass(
    name: ClassName,
    private val nestedType: TypeName,
    private val defaultValue: String?
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
            if( defaultValue != null) {
                // TODO: we should replace this with a 'map { it ?: defaultValue }' to avoid parsing the value each time. but this class needs to know jow to convert the value into the required data type
                write("return (this ?: \"$defaultValue\").as")
            } else {
                write("return as")
            }
            nestedType.render(this)
            writeln("(context)")
            indent {
                // TODO: add type validation here if necessary e.g. validateString { it.minLength(5) }
                writeln(".mapNotNull { ${name.name}(it) }")
            }
        }
        writeln("}")

        writeln()
        // this function should only be used for list/array items. otherwise the default value will not be available
        writeln("fun Maybe<String?>.as${name.name}() : Maybe<${name.name}?> = onNotNull { value.as${name.name}(context) }")
    }

    fun withCompanion(name: ClassName? = null, block: KotlinCompanion.() -> Unit) {
        if (this.companion == null) {
            this.companion = KotlinCompanion(name)
        }
        block(this.companion!!)
    }
}
