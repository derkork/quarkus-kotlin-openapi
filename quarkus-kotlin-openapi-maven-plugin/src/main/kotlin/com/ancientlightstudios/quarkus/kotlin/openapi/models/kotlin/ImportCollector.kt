package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

class ImportCollector(private val basePackage: String, private val providedPackages: List<String>) {

    private val imports = mutableSetOf<ImportData>()

    fun register(name: String, packageName: String) {
        imports.add(ImportData(packageName, name))
    }
    
    fun register(typeName: KotlinTypeName) {
        imports.add(ImportData(typeName.packageName, typeName.name))
    }

    fun register(typeReference: KotlinTypeReference) {
        when (typeReference) {
            is KotlinSimpleTypeReference -> imports.add(ImportData(typeReference.packageName, typeReference.name))
            is KotlinParameterizedTypeReference -> {
                imports.add(ImportData(typeReference.outerType.packageName, typeReference.outerType.name))
                typeReference.innerTypes.forEach { register(it) }
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
        val wildcardImports = imports.filter { it.objectName == "*" }.map { it.packageName }.toSet()

        return imports.asSequence()
            // everything with a missing package. most likely methods
            .filterNot { it.packageName.isBlank() }
            // skip everything located in the current package
            .filterNot { it.packageName == basePackage }
            // skip everything provided by the runtime
            .filterNot { it.packageName in providedPackages }
            // skip everything covered by a wildcard package, unless it's the wildcard itself
            .filterNot { it.packageName in wildcardImports && it.objectName != "*" }
            // create the import statement. in case of a subclass, use the base class for the import
            .mapTo(mutableSetOf()) { "${it.packageName}.${it.objectName.substringBefore('.')}" }
            .sorted()
            .toList()
    }

    private data class ImportData(val packageName: String, val objectName: String)
}
