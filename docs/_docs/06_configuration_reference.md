---
layout: page
title: Reference
permalink: /reference
description: "Configuration reference for Quarkus Kotlin OpenAPI."
---

# {{ page.title }}

## Generating only a subset of the API
Sometimes you may receive a huge OpenAPI specification file and you are only interested in a subset of it. The plugin allows you to specify which methods you would like to include per `execution`:

```xml
<execution>
    ...
    <configuration>
        ...
        <!-- If this is specified, only the listed endpoints will be included -->
        <endpoints>
            <!-- Include all methods of the /foo endpoint -->
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
When you have a large API you may want to split the generated code into multiple files so you don't end up with a monster delegate. The plugin allows you to use OpenAPI tags to split the generated code into multiple files:

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

This will generate two interfaces: `MyInterfaceFooDelegate` and `MyInterfaceBarDelegate` which you can implement in your application. If a method does not have a tag, a third interface `MyInterfaceDelegate` will be generated which contains all the methods without a tag. If a method has multiple tags, it will be included in the corresponding interface of the first tag.