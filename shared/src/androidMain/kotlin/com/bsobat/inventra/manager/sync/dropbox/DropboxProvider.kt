package com.bsobat.inventra.manager.sync.dropbox

import com.bsobat.inventra.manager.sync.CloudFileMetadata
import com.bsobat.inventra.manager.sync.CloudStorageProvider
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import java.io.FileInputStream
import java.io.FileOutputStream

actual class DropboxProvider actual constructor(
    private val credentials: DropboxCredentials
) : CloudStorageProvider {

    private val client: DbxClientV2 by lazy {
        val config = DbxRequestConfig.newBuilder("Inventra").build()
        DbxClientV2(config, credentials.accessToken)
    }

    private val fileSystem = FileSystem.SYSTEM

    actual override suspend fun uploadFile(localPath: Path, remoteName: String): Result<String> {
        return try {
            fileSystem.read(localPath) {
                val inputStream = FileInputStream(localPath.toFile())
                client.files().uploadBuilder("/$remoteName")
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream)
            }
            Result.success("/$remoteName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun downloadFile(remoteName: String, localPath: Path): Result<Unit> {
        return try {
            val outputStream = FileOutputStream(localPath.toFile())
            client.files().downloadBuilder("/$remoteName")
                .download(outputStream)
            outputStream.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun getFileMetadata(remoteName: String): Result<CloudFileMetadata> {
        return try {
            val metadata = client.files().getMetadata("/$remoteName")
            Result.success(
                CloudFileMetadata(
                    name = metadata.name,
                    size = (metadata as? com.dropbox.core.v2.files.FileMetadata)?.size ?: 0,
                    modifiedTime = (metadata as? com.dropbox.core.v2.files.FileMetadata)?.clientModified?.time ?: 0
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun fileExists(remoteName: String): Boolean {
        return try {
            client.files().getMetadata("/$remoteName")
            true
        } catch (e: Exception) {
            false
        }
    }
}
