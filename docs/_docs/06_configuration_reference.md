---
layout: page
title: Reference
permalink: /reference
description: "Configuration reference for Quarkus Kotlin OpenAPI."
---

# {{ page.title }}

## Table of Contents

- [Generating only a subset of the API](#generating-only-a-subset-of-the-api)
- [Splitting a large API into multiple files](#splitting-a-large-api-into-multiple-files)
- [Additional validation checks for values and models](#additional-validation-checks-for-values-and-models)
- [Changing the names of enum items](#changing-the-names-of-enum-items)
- [Changing the names of model classes](#changing-the-names-of-model-classes)
- [Sharing responses between requests](#sharing-responses-between-requests)

## Generating only a subset of the API

Sometimes you may receive a huge OpenAPI specification file and you are only interested in a subset of it. The plugin allows 
you to specify which methods you would like to include per `execution`:

```xml
<execution>
    ...
    <configuration>
        ...
        <!-- If this is specified, only the listed endpoints will be included -->
        <endpoints>
            <!-- Include all methods of the /sum endpoint -->
            <endpoint>/sum</endpoint>
            <!-- Include only the GET method of the /user/{id} endpoint -->
            <endpoint>/user/{id}:get</endpoint>
            <!-- Include only the POST and PUT methods of the /user endpoint -->
            <endpoint>/user:post,put</endpoint>
        </endpoints>
    </configuration>
</execution>
```

## Splitting a large API into multiple files

When you have a large API you may want to split the generated code into multiple files so you don't end up with a monster delegate. 
The plugin allows you to use OpenAPI tags to split the generated code into multiple files:

```xml
<execution>
    ...
    <configuration>
        ...
        <interfaceName>MyInterface</interfaceName>
        <!-- If true, the generated code will be split into multiple files based on the OpenAPI tags -->
        <splitByTags>true</splitByTags>
        ...
    </configuration>
</execution>
```

Now you can use OpenAPI tags to split the generated code into multiple files:

```yaml
...
paths:
  /foo:
    get:
      ...
      tags:
        - foo
  /bar:
    post:
      ...
      tags:
        - bar
```

This will generate two interfaces: `MyInterfaceFooDelegate` and `MyInterfaceBarDelegate` which you can implement in your application. 
If a method does not have a tag, a third interface `MyInterfaceDelegate` will be generated which contains all the methods without a tag. 
If a method has multiple tags, it will be included in the corresponding interface of the first tag.

## Additional validation checks for values and models

OpenAPI already defines a few validation rules like value type (`number`, `string`, `object`, etc.), nullable and more specific 
constraints for certain types (e.g. `maxLength`, `minLength`, `pattern` for string values). While this is ok for basic checks,
it's not enough for complex or cross value checks.

To do something like this, the generator supports the `x-constraints` property which can be added to any schema definition
and can be used in combination with the validation rules defined by OpenAPI. The value of this property can be a string 
or a list of strings. For each string you have to provide a function to perform your validation logic with this signature:

```kotlin
fun DefaultValidator.<function-name>(value: <value-type>) {
    // call fail(...) to raise a validation error    
}
```
                     
Let's create a little example to see this in action

```yaml
type: string
minLength: 5
x-constraints:
  - withO
  - allLower
```
                                           
For this schema you have to provide two validation functions. The functions must be in the same package as the generated
server or client code or in one of the packages specified via `additionalImports` in the plugin configuration.

```kotlin
fun DefaultValidator.withO(value: String) {
    if (!value.contains('o', ignoreCase = true)) {
        fail("must contain the letter 'o'", ErrorKind.Invalid)
    }
}

fun DefaultValidator.allLower(value: String) {
    if (value.lowercase() != value) {
        fail("must only be lowercase", ErrorKind.Invalid)
    }
}
```
 
The generated code would check that if a value was provided (because this schema defines a nullable type) 
- the length is at least 5
- the function `withO` doesn't fail
- the function `allLower` doesn't fail

In the example above, both checks could be realized by just using the `pattern` feature of OpenAPI. But imagine you 
have to check that a given date is at least 5 days in the future. This is not possible with OpenAPI but could be 
realized with this feature.

Furthermore, in both function the type of the value was a simple string. But that's just because the schema defines 
a string type. By adding the `x-constraints` property to a schema of type `object`, `array` or any other type, the type
of the value parameter changes accordingly.

## Changing the names of enum items

The generator always tries to produce nice names for classes and properties. But sometimes the result is just not good
enough. For example the following enum schemas

```yaml
    PlainEnum:
      type: number
      format: int32
      enum:
        - 1
        - 2
        - 4
        - 8

    StringEnum:
      type: string
      enum:
        - x-flag-upper
        - x-flag-lower
```

will be converted into these kotlin enums

```kotlin
enum class PlainEnum(val value: Int) {
    _1(1),
    _2(2),
    _4(4),
    _8(8);
}

enum class StringEnum(val value: String) {
    XFlagUpper("x-flag-upper"),
    XFlagLower("x-flag-lower");
}
```

Unfortunately the generator has no additional information to generate better labels and changing the OpenAPI specification
is not always possible, especially if it is provided by another party. For this, the generator supports the `x-enum-item-names` 
property which can be added to enum schema definitions. The value of this property is a map with string keys and string values.
The generator will use this map when generating labels for enum items.

```yaml
    PlainEnum:
      type: number
      format: int32
      enum:
        - 1
        - 2
        - 4
        - 8
      x-enum-item-names:
        1: One
        4: Four
        
    StringEnum:
      type: string
      enum:
        - x-flag-upper
        - x-flag-lower
      x-enum-item-names:
        x-flag-upper: Upper
        x-flag-lower: Lower
```

will now be converted into these kotlin enums

```kotlin
enum class PlainEnum(val value: Int) {
    One(1),
    _2(2),
    Four(4),
    _8(8);
}

enum class StringEnum(val value: String) {
    Upper("x-flag-upper"),
    Lower("x-flag-lower");
}
```

As you can see, this only changes the label not the value of an enum item. And you don't have to specify a value for all
items if you just want to modify some of them.

## Changing the names of model classes
                       
Similar to the names of enum items, the generator tries to generate meaningful names for model classes. If a schema is 
defined in the `schemas` section of the OpenAPI specification and referenced via `$ref` this name will be preferred if
a model must be generated for this schema. In other cases, the generator uses the context where a schema is defined to 
find a name. 
            
If you are not happy with the generated name of a model you can force the generator to use a different one. Just add
the `x-model-name` property to a schema. The value of the property is a string, and it will be used as the name for 
this model if possible (the generator can still modify the name if it clashes with other generated files).

In the same way, the name for the wrapper class of a `oneOf` option can be changed with the `x-container-model-name` 
property. The value of this property is a string as well.

The following example uses both options

```yaml
    Vehicle:
      oneOf:
        - type: object
          # this is a ship
          properties:
            numberOfDecks:
              type: integer
        - type: object
          # this is a car
          properties:
            numberOfWheels:
              type: integer
          x-model-name: Car
          x-container-model-name: CarOption
```

This `oneOf` schema will produce the following kotlin elements

```kotlin
sealed interface Vehicle {
    ...
}

data class VehicleVehicleOption1(val value: VehicleOption1) : Vehicle {
    ...
}

data class CarOption(val value: Car) : Vehicle {
    ...
}
```

As you can see, the model and the container for the first option was generated, while the model (`Car`) and container
(`CarOption`) for the second option is as specified by the two properties.
                       
## Sharing responses between requests
                                     
It's possible in OpenAPI to define common responses and reuse them in multiple requests.
                                                                           
In the following example, two requests use the same response to indicate a 400 error. 

```yaml
paths:
  /test1:
    get:
      operationId: test1
      responses:
        '400':
          $ref: '#/components/responses/Generic400'
        
  /test2:
    get:
      operationId: test2
      responses:
        '400':
          $ref: '#/components/responses/Generic400'
        
components:
  responses:
    Generic400:
      description: Bad Request
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorInfo'
```

The generated request context will look like this

```kotlin
class Test1RequestContext(...) {
    ...
    fun badRequest(body: ErrorInfo): Nothing = status(400, "application/json", body.asJson().asString(dependencyContainer.objectMapper))
    ...
}

class Test2RequestContext(...) {
    ...
    fun badRequest(body: ErrorInfo): Nothing = status(400, "application/json", body.asJson().asString(dependencyContainer.objectMapper))
    ...
}
```

In both classes a similar method was generated to send a 400 error to the caller. But the compiler doesn't know
that they are the same. This can be changed with the `x-generic-response-name` property. The value of this property
is a string and specifies the name of an interface that should be generated for this response.

So changing the above example to

```yaml
components:
  responses:
    Generic400:
      description: Bad Request
      x-generic-response-name: GenericBadRequestResponse
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorInfo'
```
                
will change the generated code to

```kotlin
interface GenericBadRequestResponse {

    fun badRequest(body: ErrorInfo): Nothing
}

class Test1RequestContext(...): GenericBadRequestResponse {
    ...
    override fun badRequest(body: ErrorInfo): Nothing = status(400, "application/json", body.asJson().asString(dependencyContainer.objectMapper))
    ...
}

class Test2RequestContext(...): GenericBadRequestResponse {
    ...
    override fun badRequest(body: ErrorInfo): Nothing = status(400, "application/json", body.asJson().asString(dependencyContainer.objectMapper))
    ...
}
```
                                                  
A new interface with this method was generated and is implemented by both context classes. This can be useful to implement
crosscutting aspects. For example, the following method is now available for all requests with this response 

```kotlin
fun <C> C.doSomething() where C: GenericBadRequestResponse {
    // can now generate a bad request response 
    badRequest(ErrorInfo(...))
}

override suspend fun Test1RequestContext.test1(): Nothing {
    ...
    doSomething()
    ...
}
```
                  
Adding multiple interfaces to the `where` clause is also possible and a request needs all the specified
responses in order to use this method.

```kotlin
fun <C> C.doSomethingElse() where C: GenericForbiddenResponse, C: GenericUnauthorizedResponse { }
```

The `x-generic-response-name` property can be used at any response not only on those under `/components/responses` as in
the example above. But you have to make sure, all responses with the same property value are identically. This means
same body model and same headers. Otherwise, you will run into compiler errors.