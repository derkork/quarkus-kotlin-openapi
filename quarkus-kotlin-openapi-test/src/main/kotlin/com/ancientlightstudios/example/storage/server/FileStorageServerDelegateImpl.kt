package com.ancientlightstudios.example.storage.server

import com.ancientlightstudios.example.storage.server.model.FileUploadResponse
import com.ancientlightstudios.example.storage.server.model.OperationError
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.ValidationError
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class FileStorageServerDelegateImpl : FileStorageServerDelegate {

    override suspend fun uploadFile(request: Maybe<UploadFileRequest>): UploadFileResponse {
        val validRequest = request.validOrElse { return UploadFileResponse.badRequest(it.to400Response()) }
        println("storing file FOOO until ${validRequest.body.expireDate}")
        return UploadFileResponse.ok(FileUploadResponse(UUID.randomUUID()))
    }

    override suspend fun downloadFile(request: Maybe<DownloadFileRequest>): DownloadFileResponse {
        val validRequest = request.validOrElse { return DownloadFileResponse.badRequest(it.to400Response()) }
        println("downloading file ${validRequest.id}")
        return DownloadFileResponse.ok(byteArrayOf(1, 2, 10, 40))
    }

    override suspend fun deleteFile(request: Maybe<DeleteFileRequest>): DeleteFileResponse {
        val validRequest = request.validOrElse { return DeleteFileResponse.badRequest(it.to400Response()) }
        println("deleting file ${validRequest.id}")
        return DeleteFileResponse.noContent()
    }

    override suspend fun changeExpireDate(request: Maybe<ChangeExpireDateRequest>): ChangeExpireDateResponse {
        val validRequest = request.validOrElse { return ChangeExpireDateResponse.badRequest(it.to400Response()) }
        println("changing expire date of file ${validRequest.id} to ${validRequest.body.expireDate}")
        return ChangeExpireDateResponse.noContent()
    }

    override suspend fun revokeToken(request: Maybe<RevokeTokenRequest>): RevokeTokenResponse {
        val validRequest = request.validOrElse { return RevokeTokenResponse.badRequest(it.to400Response()) }
        println("revoking token ${validRequest.body.token}")
        return RevokeTokenResponse.ok()
    }

    private fun List<ValidationError>.to400Response() =
        OperationError(joinToString("\n") { "${it.path}: ${it.message}" })

}