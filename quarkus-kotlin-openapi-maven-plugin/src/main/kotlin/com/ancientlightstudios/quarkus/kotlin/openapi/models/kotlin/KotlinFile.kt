package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

class KotlinFile(val packageName: String, val fileName: ClassName) : ClassAware, MethodAware, EnumAware, InterfaceAware,
    ValueClassAware {

    private val imports = mutableSetOf<String>()
    private val content = KotlinRenderableBlockContainer<KotlinRenderable>()

    fun registerImport(import: String, wildcardImport: Boolean = false) =
        apply { imports.add(import + if (wildcardImport) ".*" else "") }

    override fun addClass(clazz: KotlinClass) {
        content.addItem(clazz)
    }

    override fun addEnum(enum: KotlinEnum) {
        content.addItem(enum)
    }

    override fun addInterface(interfaze: KotlinInterface) {
        content.addItem(interfaze)
    }

    override fun addMethod(method: KotlinMethod) {
        content.addItem(method)
    }

    override fun addValueClass(valueClass: KotlinValueClass) {
        content.addItem(valueClass)
    }

    fun render(writer: CodeWriter) = with(writer) {
        writeln("// THIS IS A GENERATED FILE. DO NOT EDIT!")
        writeln("package $packageName")

        if (imports.isNotEmpty()) {
            writeln()
            imports.sorted().forEach {
                writeln("import $it")
            }
        }

        if (content.isNotEmpty) {
            writeln()
            content.render(this)
        }
    }
}

fun kotlinFile(packageName: String, fileName: ClassName, block: KotlinFile.() -> Unit) =
    KotlinFile(packageName, fileName).apply(block)