package com.bsobat.inventra.manager.sync.drive

import android.content.Context
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.manager.sync.CloudFileMetadata
import com.bsobat.inventra.manager.sync.CloudStorageProvider
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class GoogleDriveProvider actual constructor(
    private val credentials: GoogleDriveCredentials
) : CloudStorageProvider {

    private val context: Context
        get() = contextProvider() as Context

    private val driveService: Drive by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccountName = credentials.displayName
        }

        Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Inventra")
            .build()
    }

    private val folderId: String
        get() = credentials.folderId ?: "root"

    actual override suspend fun uploadFile(localPath: Path, remoteName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val localFile = File(localPath.toString())
            val existingFileId = findFileByName(remoteName)

            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = remoteName
                if (existingFileId == null) {
                    parents = listOf(folderId)
                }
            }

            val mediaContent = FileContent("application/zip", localFile)

            val file = if (existingFileId != null) {
                driveService.files()
                    .update(existingFileId, fileMetadata, mediaContent)
                    .execute()
            } else {
                driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            }

            Result.success(file.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun downloadFile(remoteName: String, localPath: Path): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fileId = findFileByName(remoteName)
                ?: return@withContext Result.failure(IOException("File not found: $remoteName"))

            FileOutputStream(File(localPath.toString())).use { outputStream ->
                driveService.files()
                    .get(fileId)
                    .executeMediaAndDownloadTo(outputStream)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun getFileMetadata(remoteName: String): Result<CloudFileMetadata> = withContext(Dispatchers.IO) {
        try {
            val fileId = findFileByName(remoteName)
                ?: return@withContext Result.failure(IOException("File not found: $remoteName"))

            val file = driveService.files()
                .get(fileId)
                .setFields("modifiedTime, size")
                .execute()

            Result.success(
                CloudFileMetadata(
                    modifiedTime = file.modifiedTime.toString(),
                    size = file.getSize() ?: 0L
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun fileExists(remoteName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            findFileByName(remoteName) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun findFileByName(name: String): String? {
        return try {
            val query = "name='$name' and '$folderId' in parents and trashed=false"
            val result = driveService.files()
                .list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id)")
                .execute()

            result.files.firstOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private lateinit var contextProvider: ContextProvider

        fun initialize(applicationContext: ContextProvider) {
            contextProvider = applicationContext
        }
    }
}