package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream

fun read(inputStream: InputStream) = ObjectMapper(YAMLFactory()).readTree(inputStream) as ObjectNode

fun ObjectNode.merge(updateNode: ObjectNode): ObjectNode {

    val fieldNames = updateNode.fieldNames()

    while (fieldNames.hasNext()) {
        val updatedFieldName = fieldNames.next()
        val valueToBeUpdated = this[updatedFieldName]
        val updatedValue = updateNode[updatedFieldName]

        when {
            // If the node is an ArrayNode
            valueToBeUpdated != null && updatedValue.isArray -> {
                // running a loop for all elements of the updated ArrayNode
                for (i in 0 until updatedValue.size()) {
                    val updatedChildNode = updatedValue[i]
                    // Create a new Node in the node that should be updated, if there was no corresponding node in it
                    // Use-case - where the updateNode will have a new element in its Array
                    (valueToBeUpdated as ArrayNode).add(updatedChildNode)
                }
            }
            // if the Node is an ObjectNode
            valueToBeUpdated != null && valueToBeUpdated.isObject -> {
                (valueToBeUpdated as ObjectNode).merge(updatedValue as ObjectNode)
            }
            else -> {
                replace(updatedFieldName, updatedValue)
            }
        }
    }

    return this
}