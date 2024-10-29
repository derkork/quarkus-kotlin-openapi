---
layout: page
title: Testing
permalink: /testing
description: "How to use test clients to test your server implementation."
---

# {{ page.title }}
## Table of contents
- [Introduction](#introduction)
- [Generating a test client](#generating-a-test-client)
- [Using the test client with the QuarkusTest test framework](#using-the-test-client-with-the-quarkustest-test-framework)
- [Request and response logging](#request-and-response-logging)
- [Sending invalid requests](#sending-invalid-requests)


## Introduction

Testing your server implementation is crucial to ensure that it behaves as expected. There are many ways in which this can be achieved. We prefer testing the actual REST interface rather than doing unit tests on the individual methods. This is because the REST interface is the contract that the server must fulfill and it is the contract that the clients will use. Testing the actual interface also ensures that the server is correctly configured with authentication, SSL, etc. 

The preferred method of testing a HTTP interface in Quarkus is RestAssured - a java library that makes it easy to test REST services. While RestAssured is a great tool, it is relatively low level and therefore requires a lot of boilerplate code to set up the tests. Therefore Quarkus Kotlin OpenAPI can generate a test client for our OpenAPI that allows us to test the server implementation in a concise and type-safe way while still allowing low-level access to the HTTP request and response when needed.

 You may now ask why we need a special test client rather than the regular client that the plugin can also generate. When testing, we want to not only test the successful cases, but also want to test a wide variety of error cases. The test client is a specialized client that is designed to make it easy to test both the successful and error cases with ease and without having to write a lot of repetitive boilerplate code. The regular client is designed to be used in the actual client code and will make it very difficult to send invalid requests, because in a production client we want to avoid sending invalid requests as much as possible.

## Generating a test client

We can generate a test client very similar to how we create servers and clients - we simply add a new execution to the generator maven plugin:

```xml
<execution>
    <id>generate-test-client</id>
    <phase>generate-test-sources</phase>
    <goals>
        <goal>test-generate</goal>
    </goals>
    <configuration>
        <sources>
            <source>${project.basedir}/src/main/resources/calculation-openapi.yaml</source>
        </sources>
        <interfaceName>CalculationApi</interfaceName>
        <!-- This generates a test client we can use in automated tests -->
        <interfaceType>TEST_CLIENT</interfaceType>
        <packageName>com.ancientlightstudios.example.testing.client</packageName>
    </configuration>
</execution>
```

In our case we use a simple calculation API that can just add two numbers. Note that because the test client will be part of the test sources, that the phase of the execution is now `generate-test-sources` and the goal is now `test-generate`. After running `mvn test-compile`, we get a test client that we can use in our tests.

## Using the test client with the QuarkusTest test framework

Quarkus provides a very neat way of testing the actual server implementation with the `@QuarkusTest` annotation. This annotation starts the Quarkus application in a test mode and allows us to send actual HTTP requests to it. We can use the test client to send these requests. The test client uses RestAssured under the hood so we will need a little bit of setup to use it with QuarkusTest. First we need to know the endpoint of the server. Quarkus can inject this into our test class:


```kotlin
@QuarkusTest
class CalculationApiServerDelegateImplTest {
    
    // inject the server URL
    @TestHTTPResource("")
    lateinit var serverUrl: URL


}
```

We also need an instance of an ObjectMapper, to convert objects into JSON and back. We can use the Jackson ObjectMapper that Quarkus provides:

```kotlin
    @Inject
    lateinit var objectMapper: ObjectMapper
```

Now we can build a function to make an instance of the test client:

```kotlin
fun testClient() = CalculationApiTestClient(objectMapper) {
    RestAssured.given().baseUri(serverUrl.toString())
}
```

In this function we use RestAssured to prepare a request towards the test server. The only required setting is the base URL of the server. But we could also add other settings like authentication, headers, etc if needed. Now with this setup, we can write tests against our API with very little boilerplate code:

```kotlin
@Test
fun `addition of two numbers works`() {
    testClient().sumSafe(SumBody(1, 2))
        .isOkResponse {
            assertThat(safeBody.result).isEqualTo(3)
        }
}
```

In this test we use the `sumSafe` method of the test client to send a request to the server. We can use a Kotlin object to build the request body and don't need to manually build a JSON ourselves, because the test client handles this for us. Finally we check the result with the `.isOkResponse` validation method. For each specified response type the test client will provide such a method. So if the request could also return a 400 error, we would also have a `.isBadRequestResponse` method.

Now this method does a lot more than just checking the for the HTTP status code. It will do a full validation of the response against the OpenAPI specification. This means that it will check the response body, the headers, etc to match what is specified in the OpenAPI for this response type. If the response does not match the OpenAPI specification (e.g. if required fields are missing or any other constraints are violated), the test will fail automatically with a detailed error message. So all we need to do is verify the actual functionality of the server. In this case, we check that the result of adding 1 and 2 is 3.

## Request and response logging

When a test fails, it is usually very important to see what exactly went over the wire, so that we can understand what went wrong. The test client will automatically print the full request and response if any assertion within the block of the response validator fails. So for example, if we change the assertion to:

```kotlin
@Test
fun `addition of two numbers works`() {
    testClient().sumSafe(SumBody(1, 2))
        .isOkResponse {
            assertThat(safeBody.result).isEqualTo(5) // this will fail
        }
}
```

Then we get this output printed when the test is running:

```txt
Request method:	POST
Request URI:	http://localhost:8081/sum
Request params:	<none>
Query params:	<none>
Form params:	<none>
Path params:	<none>
Headers:		Accept=*/*
				Content-Type=application/json
Cookies:		<none>
Multiparts:		<none>
Body:
{
    "a": 1,
    "b": 2
}
HTTP/1.1 200 OK
content-length: 12
Content-Type: application/json
{
    "result": 3
}


org.opentest4j.AssertionFailedError: 
expected: 5
 but was: 3
```

## Sending invalid requests

You may have noticed that the function that we called was named `sumSafe` rather than just `sum`. This is because the test client will contain three flavours to call each method in the OpenAPI spec:

- `<name>Safe` - this is a safe variant, that will only allow structurally valid requests to be sent. It is basically equivalent to the regular client that the plugin can generate. This is the method that you should use for the happy path tests or tests that check for simple violations like `min`/`max` or length violations. Using this method will ensure that the request is structurally valid while requiring the least amount of boilerplate code.
- `<name>Unsafe` - this is an unsafe variant that will allow you to send any request, even if it violates the OpenAPI specification. This can be used to send null values for required fields, leaving out parameters or sending no request body. It still will generate the JSON for you, keeping the amount of boilerplate code low. This is useful for quickly testing required fields.
- `<name>Raw` - this variant will give you low level access to the underlying RestAssured object. This allows you to send pretty much anything you like, but still fixes things like the request method and content type. This is useful for testing totally broken inputs, like invalid JSON or wrong data types (e.g. sending a String where a number is expected).

Let's see this in action and try to send a request that leaves out the second summand:

```kotlin
@Test
fun `addition of two numbers fails if one is missing`() {
    testClient().sumUnsafe {
        body(SumBody.unsafeJson(2, null))
    }
    .isBadRequestResponse {
        assertThat(safeBody.message).isEqualTo("request.body.b : is required")
    }
}
```

Here we use the `sumUnsafe` method to send a request that violates the OpenAPI specification. This takes a lambda that allows us to set the request body ourselves. We use the `unsafeJson` helper function on our generated `SumBody` class to create a JSON object that violates the OpenAPI specification but still get a JSON string with ease. Because this request is supposed to be invalid, we use the `.isBadRequestResponse` method to check that the server responds with a 400 status code and a message that tells us what went wrong.

Finally, let's try to send a request that is totally broken:

```kotlin
fun `addition fails if we send something different than JSON`() {
    testClient().sumRaw {
        body("This is not JSON")
    }.isBadRequestResponse {
        assertThat(safeBody.message).isEqualTo("request.body : is not valid json")
    }
}
```

This time we use the `sumRaw` function. This will still prepare a request with the correct method and path but gives us otherwise full control over the contents of the request. We can send a string that is not JSON at all and check that the server responds with a 400 status code and a message that tells us that the request body is not valid JSON.

