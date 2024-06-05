package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

class ImportCollector(private val basePackage: String) {

    private val imports = mutableSetOf<ImportData>()

    fun register(className: ClassName) {
        imports.add(ImportData(className.packageName, className.value, className.provided))
    }

    fun register(methodName: MethodName) {
        imports.add(ImportData(methodName.packageName, methodName.value, methodName.provided))
    }

    @JvmName("registerClasses")
    fun register(className: List<ClassName>) {
        className.forEach { register(it) }
    }

    fun register(typeName: TypeName) {
        when (typeName) {
            is TypeName.SimpleTypeName -> register(typeName.name)
            is TypeName.GenericTypeName -> {
                register(typeName.outerType)
                register(typeName.innerType)
            }

            is TypeName.DelegateTypeName -> {
                typeName.receiverType?.let { register(it) }
                register(typeName.parameterTypes)
                register(typeName.returnType)
            }
        }
    }

    @JvmName("registerTypes")
    fun register(typeName: List<TypeName>) {
        typeName.forEach { register(it) }
    }

    fun registerFrom(renderable: KotlinRenderable) {
        renderable.apply { registerImports() }
    }

    fun registerFrom(renderable: List<KotlinRenderable>) {
        renderable.forEach { registerFrom(it) }
    }

    fun getImports(): List<String> {
        // all packages covered by a wildcard import
        val wildcardImports = imports.filter { it.objectName == "*" }.map { it.packageName }.toSet()

        return imports.asSequence()
            // skip everything provided by the runtime
            .filterNot { it.provided }
            // everything with an invalid package. most likely methods
            .filterNot { it.packageName.isBlank() }
            // skip everything located in the current package
            .filterNot { it.packageName == basePackage }
            // skip everything covered by a wildcard package, unless it's the wildcard itself
            .filterNot { it.packageName in wildcardImports && it.objectName != "*" }
            // create the import statement. in case of a subclass, use the base class for the import
            .mapTo(mutableSetOf()) { "${it.packageName}.${it.objectName.substringBefore('.')}" }
            .sorted()
            .toList()
    }

    private data class ImportData(val packageName: String, val objectName: String, val provided: Boolean)
}