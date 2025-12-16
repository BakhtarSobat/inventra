package com.bsobat.inventra.manager.sync.drive

import com.bsobat.inventra.manager.sync.CloudFileMetadata
import com.bsobat.inventra.manager.sync.CloudStorageProvider
import okio.Path

expect class GoogleDriveProvider(
    credentials: GoogleDriveCredentials
) : CloudStorageProvider {
    override suspend fun uploadFile(localPath: Path, remoteName: String): Result<String>
    override suspend fun downloadFile(remoteName: String, localPath: Path): Result<Unit>
    override suspend fun getFileMetadata(remoteName: String): Result<CloudFileMetadata>
    override suspend fun fileExists(remoteName: String): Boolean
}

data class GoogleDriveCredentials(
    val accessToken: String,
    val refreshToken: String? = null,
    val folderId: String? = null,
    val accountEmail: String = "",
    val displayName: String = ""
)