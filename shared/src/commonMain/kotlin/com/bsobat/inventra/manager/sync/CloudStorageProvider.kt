package com.bsobat.inventra.manager.sync

import okio.Path

interface CloudStorageProvider {
    suspend fun uploadFile(localPath: Path, remoteName: String): Result<String>
    suspend fun downloadFile(remoteName: String, localPath: Path): Result<Unit>
    suspend fun getFileMetadata(remoteName: String): Result<CloudFileMetadata>
    suspend fun fileExists(remoteName: String): Boolean
}