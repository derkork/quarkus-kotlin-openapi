package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.strafbank.CodeWriter

class KotlinInterface(name: Name.ClassName) : KotlinFileContent(name) {

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.forEach {
            it.render(this)
            writeln()
        }

        writeln("interface ${name.name} {")
        indent {
            writeln("// methoden hier")
        }
        write("}")
    }


}