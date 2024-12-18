package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinFile(val fileName: ClassName) : ClassAware, MethodAware, EnumAware, InterfaceAware {

    private val imports = mutableSetOf<ClassName>()
    private val content = KotlinRenderableBlockContainer<KotlinRenderable>()

    fun registerImports(vararg imports: ClassName) = this.imports.addAll(imports)

    fun registerImports(imports: List<ClassName>) = this.imports.addAll(imports)

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

    fun render(writer: CodeWriter) = with(writer) {
        val allImports = collectImports()

        writeln("// THIS IS A GENERATED FILE. DO NOT EDIT!")
        writeln("package ${fileName.packageName}")

        if (allImports.isNotEmpty()) {
            writeln()
            allImports.forEach {
                writeln("import $it")
            }
        }

        if (content.isNotEmpty) {
            writeln()
            content.render(this)
        }
    }

    private fun collectImports(): List<String> {
        val collector = ImportCollector(fileName.packageName)
        imports.forEach { collector.register(it) }
        collector.registerFrom(content)

        return collector.getImports()
    }
}

fun kotlinFile(fileName: ClassName, block: KotlinFile.() -> Unit) = KotlinFile(fileName).apply(block)