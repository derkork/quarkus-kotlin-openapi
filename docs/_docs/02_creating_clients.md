---
layout: page
title: Clients
permalink: /clients
description: "Creating clients with Quarkus Kotlin OpenAPI."
---

#  {{ page.title }}

## Table of contents

- [Introduction](#introduction)
- [Using the client in code](#using-the-client-in-code)
- [Error handling](#error-handling)
- [Client configuration](#client-configuration)


## Introduction

In many projects you will not only create a REST server, but also will need to create clients that consume other REST services. You can use the OpenAPI generator to create a client for your REST service. The general approach is very similar to creating a server, but with a few differences. First, let's configure the generation of a client in the `pom.xml` file.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.ancientlightstudios</groupId>
            <artifactId>quarkus-kotlin-openapi-maven-plugin</artifactId>
            <version>${quarkus.kotlin.openapi.version}</version>
            <executions>
                <!-- Another execution that you may already have for generating a server. -->
                <execution>
                    <id>generate-server</id>
                    ...
                </execution>
                <!-- This execution generates a client. -->
                <execution>
                    <id>generate-client</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <!-- This tells the plugin where to find the OpenAPI spec for the client we want to generate. 
                        We use the Swagger pet store example here, so we can later test our client without having to
                        deploy a server first.
                        -->
                        <sources>
                                <source>${project.basedir}/src/main/resources/pet-store-openapi.yaml</source>
                        </sources>
                        <!-- This tells the plugin the name of the client interface that
                        should be generated for the given OpenAPI spec. -->
                        <interfaceName>UpstreamPetStoreApi</interfaceName>
                        <!-- This tells the plugin what kind of code to generate.
                        In this case, we want to generate a client. -->
                        <interfaceType>CLIENT</interfaceType>
                        <!-- This tells the plugin in which package the generated code should be placed. -->
                        <packageName>com.ancientlightstudios.example.clients.client</packageName>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Using the client in code

After the plugin has generated the client code, we can use the generated client in our code. As an example let's write a small server that offers a reduced version of the PetStore API (e.g. a facade). We can generate the server code similar to how we did it in our [getting started example]({{site.baseurl}}/getting-started).

The client that the plugin generated will be in a class named `UpstreamPetStoreApiClient`. This client is ready for use within Quarkus, so we can simply inject it into our server code like this:

```kotlin
class MyPetStoreApiServerDelegateImpl(
    // inject the client
    private val petStoreApiClient: UpstreamPetStoreApiClient
)
```

Now we can call the client from our server code:

```kotlin
// Call the remote server
val response = petStoreApiClient.findPetsByStatus(FindPetsByStatusStatusParameter.Available)
```

The call looks like a regular function call. The function will have a parameter for each piece of input that goes into the request (e.g. path parameters, query parameters, header parameters, request body). These parameters are type safe (e.g. an optional `string` will be a `String?` parameter while a required `integer` will be an `Int`). and are derived from the OpenAPI spec. The function will return a `Response` object that allows us to access the result of the call. The function is also a `suspend` function, so it will automatically suspend the current coroutine until the call is finished. This allows us to write asynchronous code in a very readable way without having to use an `Uni` and subscribe to it, like we would have to do with Quarkus' built-in REST client.

## Error handling

The nature of every networked call is, that there are a lot of ways in which it can fail. Most libraries tend to skim over this fact and just throw an exception when anything goes wrong, leaving the error handling to the caller. In the real world this often leads to code that implements only the happy case and either foregoes error handling completely or handles it in a very generic way. For some applications this might be acceptable, but many applications need to handle errors in a more sophisticated way. Therefore the Quarkus Kotlin OpenAPI client library has a few features that make error handling easy to do. 

In general, error handling is always done in a structured way without throwing exceptions. This forces the caller to actually handle an error instead of just letting an exception bubble up. This is done by returning a `Result` object from every call. This object can have many different _flavours_ depending on how the call went:

- `HttpResponse` - this flavour means, that we got a HTTP response from the server. This means the response was any response that was defined in the OpenAPI spec for this call. For each response that is defined in the OpenAPI spec, there will be a corresponding flavour of `HttpResponse` that represents this response. For example:
  - `HttpResponse.Ok` - the call was successful and the server returned a 200 OK response.
  - `HttpResponse.BadRequest` - the call was not successful and the server returned a 400 Bad Request response.
  - etc.
- `RequestError` - an error occurred while sending the request. This will be returned if the request could not be sent at all. This flavour again has sub-flavours that represent different kinds of errors that can occur while sending a request. For example:
  - `RequestErrorConnectionReset` - the connection was reset while sending the request.
  - `RequestErrorTimeout` - the request timed out.
  - `RequestErrorUnreachable` - the server could not be reached.
  - `RequestErrorUnknown` - an unknown error occurred. This usually indicates a misconfiguration, SSL certificates being wrong, etc.
 - `ResponseError` - the server returned an answer that did not adhere to the OpenAPI spec. This can happen if the server implementation is buggy or the server is maybe behind a reverse proxy and the proxy answers with an error. 

A nice way to handle all possible responses is a Kotlin `when` statement:

```kotlin
val message = when (response) {
    // Request was successful
    is FindPetsByStatusHttpResponse.Ok -> {
        // Return the pets.Calling ok will immediately stop execution here.
        ok(response.safeBody.map { Pet(it.id, it.name ?: "no name", it.photoUrls ?: listOf()) })
    }

    // We sent a bad request to the remote server. This is usually a bug in our code.
    is FindPetsByStatusHttpResponse.BadRequest -> "We sent a bad request to the remote server. This is usually a bug in our code."

    // We have various possible error cases which can occur when calling the remote server.
    // How we handle these depends on the specific requirements of our application. In our case
    // we'll just extract an error message that we return to our own caller. We could however react
    // to each error case differently (e.g. with different responses). Since our API only has a single
    // simple error response type we'll just return that.
    is FindPetsByStatusError.RequestErrorConnectionReset -> "Connection was reset when calling the remote server."
    is FindPetsByStatusError.RequestErrorTimeout -> "Timeout when calling the remote server."
    is FindPetsByStatusError.RequestErrorUnreachable -> "The remote server could not be reached."
    is FindPetsByStatusError.RequestErrorUnknown -> {
        Log.warn("An unknown error occurred when calling the remote server.", response.cause)
        "An unknown error occurred when calling the remote server (usually indicates misconfiguration, SSL certificates being wrong, etc.)."
    }
    is FindPetsByStatusError.ResponseError -> "The response from the remote server did not adhere to the OpenAPI spec. Reason: ${response.reason}"
}

// Return the error message
internalServerError(GetAvailablePets500Response(message))
```
This not only is very readable, the Kotlin compiler will also warn us if we forget to handle a case, because the `when` statement must be exhaustive and cover all cases. In some cases we may want to do a less fine-grained error handling. Because all errors implement the `IsError` interface, we can do a more streamlined error handling like this:

```kotlin
val message = when (response) {
    // Request was successful
    is FindPetsByStatusHttpResponse.Ok -> {
        // Return the pets.Calling ok will immediately stop execution here.
        ok(response.safeBody.map { Pet(it.id, it.name ?: "no name", it.photoUrls ?: listOf()) })
    }

    // This should usually succeed. In case we get a non 200 silently log this and return an internal error.
    is FindPetsByStatusHttpResponse -> {
        Log.warn("Unexpected response from the remote server: ${response.status} : ${response.unsafeBody}")
        "Internal error."
    }
    is FindPetsByStatusError -> {
        // some other error occurred when talking with the server. We can't really do anything about it
        Log.info("An error occurred when calling the remote server: ${response.errorMessage}")
        "Internal error."
    }
}
// Return the error message
internalServerError(GetAvailablePets500Response(message))     
```

Or even a pretty minimalistic error handling like:

```kotlin
if (response is FindPetsByStatusHttpResponse.Ok) {
    ok(response.safeBody.map { Pet(it.id, it.name ?: "no name", it.photoUrls ?: listOf()) })
}

// Return the error message
internalServerError(GetAvailablePets500Response("Internal error."))     
```

In general it is a good idea though to spend a bit more time on good error handling as errors _will_ occur in production and it is much better to have good log output and error messages when they do.

## Client configuration

Under the hood, all generated clients are Quarkus built-in REST clients. This allows us to configure them like any other Quarkus REST client and use every feature that Quarkus offers for them. For example we can set up endpoint urls, connection timeouts, trusted certificates, etc. in the `application.properties` file:

```properties
quarkus.rest-client."upstream-pet-store-api-client".uri=https://petstore3.swagger.io/api/v3
quarkus.rest-client."upstream-pet-store-api-client".connect-timeout=5000
quarkus.rest-client."upstream-pet-store-api-client".read-timeout=5000
```

The name of the client in the configuration is derived from the generated class name. So because our client class is named `UpstreamPetStoreApiClient`, the configuration key is `upstream-pet-store-api-client`. Quarkus can also log all requests and responses of the client. This can be very helpful for debugging. To enable this, add the following configuration to the `application.properties` file:

```properties
quarkus.log.category."org.jboss.resteasy.reactive.client.logging".level=DEBUG
quarkus.rest-client.logging.scope=request-response
quarkus.rest-client.logging.body-limit=50000
```



