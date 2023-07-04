package com.tallence.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream

fun read(inputStream: InputStream) = ObjectMapper(YAMLFactory()).readTree(inputStream) as ObjectNode
