package com.ancientlightstudios.quarkus.kotlin.openapi

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

abstract class GenerateMojo : AbstractMojo() {
    /**
     * The current Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    lateinit var project: MavenProject

    /**
     * The list of the source files.
     */
    @Parameter(required = true)
    lateinit var sources: List<String>

    /**
     * The list of JSON-Patch files to apply to the OpenAPI specification.
     */
    @Parameter
    var patches: List<String> = listOf()

    /**
     * Path where the debug output should be written.
     */
    @Parameter
    var debugOutputFile: String? = null

    /**
     * The package name of the generated classes.
     */
    @Parameter(defaultValue = "com.example.openapi")
    lateinit var packageName: String

    /**
     * Additional package/class imports that should be included. These can contain custom validators or custom type converters.
     */
    @Parameter
    var additionalImports: List<String> = listOf()

    /**
     * The name of the interface to generate.
     */
    @Parameter(defaultValue = "ExampleInterface")
    lateinit var interfaceName: String

    /**
     * Path prefix to be prepended to generated endpoints.
     */
    @Parameter(defaultValue = "")
    var pathPrefix: String = ""

    /**
     * The directory where the generated sources should be put
     */
    abstract var outputDirectory: File

    @Parameter
    var endpoints: List<String> = listOf()

    @Parameter
    var splitByTags: Boolean = false

    @Parameter
    var typeMappings: List<String> = listOf()

    @Parameter
    var contentTypeMappings: List<String> = listOf()

    @Parameter
    lateinit var interfaceType: InterfaceType

    /**
     * A list of additional provider classes which should be added as @RegisterProvider annotations to the generated interface.
     */
    @Parameter
    var additionalProviders: List<String> = listOf()

    /**
     * If true, generated files will be overwritten even if they are newer than the source files.
     */
    @Parameter(defaultValue = "false")
    var forceOverwriteGeneratedFiles: Boolean = false

    override fun execute() {
        val config = Config(
            sources,
            patches,
            debugOutputFile,
            interfaceName,
            packageName,
            additionalImports,
            outputDirectory.path,
            pathPrefix,
            endpoints,
            splitByTags,
            typeMappings,
            contentTypeMappings,
            interfaceType,
            additionalProviders,
            forceOverwriteGeneratedFiles
        )

        Generator(config).generate()

        registerSourceRoot()
    }

    abstract fun registerSourceRoot()

}