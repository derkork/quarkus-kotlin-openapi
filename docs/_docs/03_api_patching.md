---
layout: page
title: API patching
permalink: /patching
description: "How to patch OpenAPI specifications"
---

# {{ page.title }}

## Table of contents

- [Introduction](#introduction)
- [Configuring patches](#configuring-patches)
  - [Overlays](#overlays)
  - [JSON patches](#json-patches)
  - [JSONata patches](#jsonata-patches)
  - [Complex sources](#complex-sources)
- [Previewing the patch result](#previewing-the-patch-result)

## Introduction

OpenAPI is a very large and somewhat complex standard and it offers a lot of ways to model the same thing. This can lead
to a lot of different ways to describe the same API. Sometimes an API specification only contains the attributes while
putting the constraints into a `description` property rather than using constraint attributes like `maxLength` or
`required`. Sometimes a specification may constrain the data type as `number` when in fact it is always an `integer`. Or
the specification may contain a construct that is not supported by the code generator but could be replaced with a
supported construct without changing the data that actually goes over the wire.

Since we usually get the specifications from a third party and have no control over how this third party chooses to
write the specification, we need a way to patch the specification to make it work for our purposes. And we want to do
this in a way that allows the third party to send us an updated specification without us having to reapply the patches.


## Configuring patches

Quarkus Kotlin OpenAPI supports three types of patches that can be applied on an OpenAPI specification before the
generator creates Kotlin code, which we'll look at in the following sections.

### Overlays

An overlay is simply a merge of two partial OpenAPI specifications. The generator starts with one specification and
merges the contents of one or more additional specifications into the first. The merge will be done fully recursively.
Overlays are best suited to quickly add or change existing properties. For example, let's say we got an OpenAPI spec
which defines a `User` object:

```yaml
...
components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: string
          example: fbed0fb3-b5e4-4de2-b628-db4af24ba859
          description: "A UUID with the user's ID."
          pattern: "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        userName:
          type: string
          maxLength: 255
        displayName:
          type: string
          maxLength: 255
      required:
        - userName
        - displayName
...
```

The `id` property is only set when the `User` is retrieved, not when it is created. So ideally this property should be
marked `readOnly` but the creator of the spec didn't add this. However it would be really nice if we didn't have to work
around this problem in our code. So we would like to patch a `readOnly: true` to the `id` property. With an overlay, we
can do this quickly. We create a new file `server-openapi-overlay.yaml` and this file will only contain the parts we
want to add:

```yaml
components:
  schemas:
    User:
      properties:
        id:
          readOnly: true
```

Now we just need to tell the plugin to apply this overlay by adding it to the `<sources>` section in the plugin's
configuration in `pom.xml`:

```xml
...
<executions>
    <execution>
        ...
        <configuration>
            <sources>
                <source>${project.basedir}/src/main/resources/server-openapi.yaml</source>
                <!-- Apply an overlay on the openapi we got -->
                <source>${project.basedir}/src/main/resources/server-openapi-overlay.yaml</source>
            </sources>
        </configuration>
        ...
    </execution>
</executions>
...
```

The code generator will now merge both files into one and now the `id` property is read-only. This way we can quickly
make minor changes and keep the original OpenAPI spec intact.

Both source files can be prefixed with `openapi://` to describe to the code generator that they contain OpenAPI specs.
This schema is the default for source files and can be omitted. But it helps to make clear what each file contains
and it will be more important when adding other patches as described in the following sections. 

### JSON patches

Overlays are nice for quickly adding or changing properties, but there are some things that cannot be done with them.
The most obvious limitation is that overlays only can add or change existing parts, but cannot delete a part. In the
previous example, the  `id` property is described to be a UUID and also has a regular expression verifying that.

```yaml
id:
  type: string
  example: fbed0fb3-b5e4-4de2-b628-db4af24ba859
  description: "A UUID with the user's ID."
  pattern: "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
```

But actually OpenAPI has a built-in UUID format and our generator would support this. But the way this specification is
written the generator will map this ID to a `kotlin.String` rather than a `java.util.UUID`. So we would like to remove
the `pattern` and instead add a `format: uuid` entry. We can do this with a [JSON patch](https://jsonpatch.com/):

```yaml
# Remove RegEx pattern and add UUID format to the id property of the User schema
- op: remove
  path: /components/schemas/User/properties/id/pattern

- op: add
  path: /components/schemas/User/properties/id/format
  value: uuid
```

We save this patch in a file `server-openapi-jsonpatch.yaml` and add it to the plugin's configuration in `pom.xml`:

```xml
...
<execution>
    ...
    <configuration>
        <sources>
            ...
            <!-- A a JSON-patch to the openapi we got -->
            <source>jsonpatch://${project.basedir}/src/main/resources/server-openapi-jsonpatch.yaml</source>
            ...
        </sources>
        <patches>
        </patches>
    </configuration>
</execution>
...
```
         
JSON patches will be applied in the order they are defined. That means you can mix OpenAPI specs, overlays and patches
in any order necessary.

### JSONata patches

JSONata is a query and transformation language for JSON data. It enables us to do more complex transformations on the
OpenAPI specification. Let's say the creator of our specification was consistent and all ID fields are marked with a
pattern. Patching all these ID fields with JSON patches will require a lot of patches and makes it easy to forget one.
Instead with JSONata we can do this in one go:

```jsonata
/* Find all strings with a UUID pattern and replace it with format: uuid */
$ ~> | **[type='string' & pattern='^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$'] | {"format" : "uuid"},["pattern"] |
```

We can save this JSONata patch into a file named `server-openapi-patch.jsonata` and add it to the plugin's configuration
in `pom.xml`:

```xml
...
<execution>
    ...
    <configuration>
        <sources>
            ...
            <!-- A JSONata patch to the openapi we got -->
            <source>jsonata://${project.basedir}/src/main/resources/server-openapi-patch.jsonata</source>
            ...
        </sources>
    </configuration>
    ...
</execution>
...
```

Like JSON patches, JSONata patches will be applied in the order they are defined. That means you can mix OpenAPI specs, 
overlays and patches in any order necessary. Mixing overlays, JSON patches and JSONata patches allows us to use a patch 
format that is most suitable for the change we want to make.
      
### Complex sources

By default, all sources are applied in the order the are defined. So patches are applied to the result of all previous 
steps. But there are cases where patches should only be applied to some sources before merging them together. 
To do this, the code generator supports a second kind of source.

 ```xml
...
<execution>
    ...
    <configuration>
        <sources>
            <source>${project.basedir}/src/main/resources/server-public-openapi.yaml</source>
            <source>jsonpatch://${project.basedir}/src/main/resources/server-public-openapi-jsonpatch.yaml</source>
            <source>${project.basedir}/src/main/resources/server-public-openapi-overlay.yaml</source>
            <complexSource>
                <sources>
                    <source>${project.basedir}/src/main/resources/server-management-openapi.yaml</source>
                    <source>jsonata://${project.basedir}/src/main/resources/server-management-openapi-patch.jsonata</source>
                </sources>
            </complexSource>
            ...
        </sources>
    </configuration>
    ...
</execution>
...
```
In this example, the public part of the OpenAPI spec is patched using a JSON patch and merged with its overlay. Then
the management part of the OpenAPI spec is patched using a JSONata patch and then merged into the public part. The 
JSONata patch only sees the management part and can't modify anything from the public part. 

This feature can be used, if patches should only be applied to parts of the OpenAPI spec and writing proper matches
(mainly for JSONata) is too complicated.

Sources are applied in the order they are defined. So it's possible to use `source` and `complexSource` in any order.
Nesting `complexSource` inside another `complexSource` is currently not supported.

## Previewing the patch result

When we patch a file, it is very useful to see what the result of all applied patches is. This can be done with a configuration setting in the plugin's configuration in `pom.xml`:

```xml
...
<execution>
    ...
    <configuration>
        <sources>
            ...
        </sources>
        <!-- Write the patched OpenAPI spec to a file, for debugging -->
        <debugOutputFile>${project.build.directory}/debug.output.json</debugOutputFile>
    </configuration>
    ...
</execution>
...
```
Now we can inspect the file `target/debug.output.json` to see the result of all patches applied.