package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinFile(val name: KotlinTypeName) : ClassAware, MethodAware, EnumAware, InterfaceAware {

    private val imports = mutableSetOf<KotlinTypeName>()
    private val content = KotlinRenderableBlockContainer<KotlinRenderable>()

    fun registerImports(vararg imports: KotlinTypeName) {
        this.imports.addAll(imports)
    }

    fun registerImports(imports: List<KotlinTypeName>) {
        this.imports.addAll(imports)
    }

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
        writeln("package ${name.packageName}")

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
        val collector = ImportCollector(
            name.packageName, listOf(
                "kotlin",
                "kotlin.collections",
                "kotlin.jvm"
            )
        )
        imports.forEach { collector.register(it) }
        collector.registerFrom(content)

        return collector.getImports()
    }
}