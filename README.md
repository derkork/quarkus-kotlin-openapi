# Quarkus Kotlin OpenAPI

Quarkus Kotlin OpenAPI is a code generator that generates server and client Kotlin code from an OpenAPI specification. The generated code is intended to run with Quarkus, the framework for writing JVM-based applications.

## Features

- Generate type-safe, high-performance, fully reactive server and client code in Kotlin from an OpenAPI specification with a focus on correctness and developer experience.
- Generate test clients for easy testing of your API implementation.
- Built-in support for patching third party OpenAPI specifications to your needs.
- Partial generation allows you to generate only the parts of the API you need for your application.
- Customizable validation, type mapping and error handling.
- Generated code does not rely on reflection which improves performance and allows the use of native image compilation with GraalVM without any additional configuration.

