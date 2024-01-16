package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

class ImportCollector(private val basePackage: String) {

    private val imports = mutableSetOf<ClassName>()

    fun register(className: ClassName) {
        imports.add(className)
    }

    fun register(className: List<ClassName>) {
        imports.addAll(className)
    }

    fun register(typeName: TypeName) {
        when (typeName) {
            is TypeName.SimpleTypeName -> register(typeName.name)
            is TypeName.GenericTypeName -> {
                register(typeName.outerType)
                register(typeName.innerType)
            }
        }
    }

    fun registerFrom(renderable: KotlinRenderable) {
        renderable.apply { registerImports() }
    }

    fun registerFrom(renderable: List<KotlinRenderable>) {
        renderable.forEach { registerFrom(it) }
    }

    fun getImports(): List<String> {
        // all packages covered by a wildcard import
        val wildcardImports = imports.filter { it.value == "*" }.map { it.packageName }.toSet()

        return imports
            .filterNot { it.provided } // skip everything provided by the runtime
            .filterNot { it.packageName == basePackage } // skip everything located in the current package
            .filterNot { it.packageName in wildcardImports && it.value != "*" }
            .map { "${it.packageName}.${it.value}" }
            .sorted()
    }

}