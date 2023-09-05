package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinClass(name: ClassName) : KotlinFileContent(name) {

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)

        writeln("class ${name.name} {")
        indent {
            writeln()
            methods.forEach {
                it.render(this)
                writeln()
            }
        }
        write("}")
    }
}
