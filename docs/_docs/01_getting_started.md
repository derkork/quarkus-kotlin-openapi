---
layout: page
title: Getting started
permalink: /getting-started
description: "Getting started with the code generator."
---

# {{ page.title }}

## Table of Contents

- [Introduction](#introduction)
- [Requirements](#requirements)
- [Running the code generator to generate a server implementation](#running-the-code-generator-to-generate-a-server-implementation)
- [Implementing the server API](#implementing-the-server-api)
- [Running the server](#running-the-server)
- [Conclusion](#conclusion)

## Introduction

This is a short guide to get you started with the Quarkus Kotlin OpenAPI code generator. We will generate a server
implementation from an OpenAPI specification and implement a simple REST API with the generated code. You can
find [the full example code here]({{site.repo}}/tree/main/examples/getting-started).

## Requirements

To use the code generator, we will need the following:

- A Quarkus project with Kotlin support.
- Maven 3.8.6 or later.
- Java 17 or later.
- Kotlin 1.8.21 or later.

## Running the code generator to generate a server implementation

The code generator is a Maven plugin that we can run as part of our build. To run it, we simply add the plugin to our
`pom.xml` and configure it to generate the code we need. Here is an example configuration that generates a server
implementation from an OpenAPI specification:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>com.ancientlightstudios</groupId>
            <artifactId>quarkus-kotlin-openapi-maven-plugin</artifactId>
            <version>${quarkus.kotlin.openapi.version}</version>
            <executions>
                <!-- Like many maven plugins, this plugin supports
                 multiple executions, so we can generate servers,
                 clients, test clients or any combination of these
                 for one or multiple OpenAPI specs. -->
                <execution>
                    <id>generate-server</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <!-- This tells the plugin where to find the OpenAPI spec. -->
                        <sources>
                            <source>${project.basedir}/src/main/resources/hello-world-openapi.json</source>
                        </sources>
                        <!-- This tells the plugin the name of the interface that
                        should be generated for the given OpenAPI spec. -->
                        <interfaceName>HelloWorldApi</interfaceName>
                        <!-- This tells the plugin what kind of code to generate.
                        In this case, we want to generate a server. -->
                        <interfaceType>SERVER</interfaceType>
                        <!-- This tells the plugin in which package the generated code should be placed. -->
                        <packageName>com.ancientlightstudios.example.getting.started.server</packageName>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

With this setup the plugin will generate a server implementation for the given OpenAPI spec as part of your normal maven
build. We can simply run `mvn compile` to generate and compile the code.

## Implementing the server API

The plugin will now have generated three things:

- A Jax-RS server implementation that implements the OpenAPI interface. In our case this is named
  `HelloWorldApiServer` (the name is derived from the interface name).
- A delegate interface that defines a Kotlin API representing the interface. In our case this is named
  `HelloWorldApiServerDelegate`.
- Various model classes that represent the data structures defined in the OpenAPI spec (e.g. requests, responses, etc.).

To implement the server API, we need to implement the `HelloWorldApiServerDelegate` interface. Let's first look at the
generated interface:

```kotlin
interface HelloWorldApiServerDelegate {
    suspend fun GetHelloWorldContext.getHelloWorld(): Nothing
}
```

You may notice a few peculiarities here:

- The interface is a suspending function. This allows us to write asynchronous code in a neat and concise way. No `Uni`,
  no `CompletionStage`, no `Future`, just plain Kotlin coroutines.
- The function is part of a receiver context object (`GetHelloWorldContext`). This object contains all the information
  about the current request, like the request parameters, the request body, the response, etc. This allows us to write
  our code in a very clean and concise way while being completely type-safe. We will see how this works in a moment.
- The function returns `Nothing`. This is because we want to have full type safety and correctness for our API. So we
  cannot use a `RestResponse<*>` or similar thing as a return type as this would allow us to return invalid responses.
  Instead, we use functions on the `GetHelloWorldContext` object to set the response status, headers, body, etc. We will
  see how this works in a moment as well.

Now let's implement the server API. Here is a simple implementation that reads a name from the request and returns a
greeting:

```kotlin
// Mark this class as a CDI bean so that Quarkus can inject it into the generated Jax-RS server.
@ApplicationScoped
class HelloWorldApiServerDelegateImpl : HelloWorldApiServerDelegate {

    override suspend fun GetHelloWorldContext.getHelloWorld(): Nothing {
        // verify that the request is valid, if not return a bad request with some error message.
        val validRequest = request.validOrElse { errors ->
            // the errors are a list of validation errors. We join them to a single string for the response.
            // as our OpenAPI only provides for a single error message.
            val errorMessage = errors.joinToString(",") {
                // the "path" contains the path to the invalid field, the "message" contains the error message.
                it.path + ": " + it.message
            }
            badRequest(GetHelloWorld400Response(errorMessage))
        }
        // generate the response, in this case we just return a hello world message including the "who"
        // parameter from the request.
        ok(GetHelloWorld200Response("Hello ${validRequest.who}!"))
    }
}
```

This implementation is very simple. It first checks if the request is valid. You may now wonder where the `request`
variable comes from. It is part of the `GetHelloWorldContext` object that is the receiver object for the function. If we
look into its definition, we can see that it is defined as `request: Maybe<GetHelloWorldRequest>`.

Now what is `Maybe`? It is a type that represents a request that may or may not be present. If someone sends a request
to our API, this request can be malformed in all kinds of ways. For example required parameters may be missing,
parameters may have the wrong type, enums may have wrong values, etc. This makes it hard to provide a type-safe and
null-safe API to our server implementation. For example if the `who` parameter from our hello world interface is
missing, we would need to represent this parameter as a nullable `String?` variable. However, the OpenAPI spec says that
this parameter is required, so it should be a non-nullable `String`.

We solve this with the `Maybe` type. This wraps a potentially malformed request and provides a way to check the request
for validity. If the request is valid, the `validOrElse` function returns a valid request object with correct types and
nullability information. This way the rest of our implementation can rely on the request being valid in a safe way that
is actually enforced by the compiler. The `validOrElse` function allows us to specify a block of code that should be
executed, if the request is not valid. This function receives a list of validation errors that can be used to generate a
response. Because the response depends on the OpenAPI specification, our implementation will need to map these errors to
a response that is valid according to the OpenAPI spec. In our case we just join the errors to a single string and
return a 400 response with this string as the error message.

Note the call to `badRequest` in the error block. This will actually stop the execution of the function and return a 400
response to the client. This function is also part of the generated `GetHelloWorldContext` object and will ensure that
the bad request response is actually valid according to the OpenAPI spec. Now we have validated our request, so we can
actually generate the response. In this case we just return a hello world message that includes the `who` parameter from
the request. We use the `ok` function to generate a 200 response with the given body. This function works similar to the
`badRequest` function, but generates a 200 response instead of a 400 response. Again this will stop the execution of the
function and return the response to the client (this is actually enforced by the compiler, because the function returns
`Nothing`).

## Running the server

Now we have implemented our server API. We can now run our Quarkus application with `mvn quarkus:dev` and test our API
with a tool like `curl`. Let's first try to call the API with a missing parameter:

```shell
curl -v  http://localhost:8080/hello-world
> GET /hello-world HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.4.0
> Accept: */*
> 
< HTTP/1.1 400 Bad Request
< content-length: 42
< Content-Type: application/json
< 
{"error":"request.query.who: is required"}
```

We can see how our validation works. The API returns a 400 response with an error message that says that the `who`
parameter is required.
Now let's try an empty parameter:

```shell
curl -v  http://localhost:8080/hello-world?who=
> GET /hello-world?who= HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.4.0
> Accept: */*
> 
< HTTP/1.1 400 Bad Request
< content-length: 69
< Content-Type: application/json
< 
{"error":"request.query.who: minimum length of 1 expected, but is 0"}
```

Again we get a 400 response with a proper error message. Note how we don't need to implement this error message
ourselves. The code generator creates all validation from the OpenAPI spec.

Now let's try a valid request:

```shell
curl -v  http://localhost:8080/hello-world?who=world
> GET /hello-world?who=world HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.4.0
> Accept: */*
> 
< HTTP/1.1 200 OK
< content-length: 26
< Content-Type: application/json
< 
{"message":"Hello world!"}
```

We get a 200 response with the expected message. 

## Conclusion

Now this is a very simple example, but you can see how the generated
code allows you to write your server implementation in a very clean and concise way while still being completely
type-safe and correct according to the OpenAPI-specification. The code generator takes care of all the boilerplate code
and validation, so you can focus on writing your business logic. There are many more features that the code generator
provides, like generating clients, test clients, partial generation, etc. Check the sidebar for more information on
these topics.