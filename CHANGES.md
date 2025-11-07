# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
                 
## [0.7.0] - 2025-11-06
- It is now possible to group inputs with patches to combine multiple source files and patches into one with a defined order.
- Objects with no properties are now properly handled.

## [0.6.1] - 2025-06-17
- Server and client implementation now logs any caught exception

## [0.6.0] - 2025-05-15
- Major refactoring in the generator to allow more features in the future. The produced code is more or less the same
  as before. There are only two minor breaking changes:
  - The order of properties in models generated for schemas with an `allOf` property can be different as before.
    In the previous version, properties included from another schema via `$ref` always appeared at the end. Now the
    order of the `allOf` items is controlling the order of the properties.
  - Creating a new instance of a test client required an ObjectMapper. This was replaced with a new `DependencyContainer`.
    The generator will create this new class next to the client, test client or server classes. It is annotated with 
    `@ApplicationScoped` so you can get it via dependency injection like the ObjectMapper before. 

## [0.5.0] - 2024-12-06
- `ValidationError` contains a new `kind` property that can be used to decide how to handle an invalid request. 
  This is a minor breaking change for custom types and custom validations. 

## [0.4.14] - 2024-11-08
- You can now access the current request method and path in every context by accessing the `requestMethod` and `requestPath`
  properties. This is useful for custom logging requirements or to implement metrics.

## [0.4.13] - 2024-10-29
- Initial public release
