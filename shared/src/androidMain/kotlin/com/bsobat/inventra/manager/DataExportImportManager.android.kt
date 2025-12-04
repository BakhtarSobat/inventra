package com.bsobat.inventra.manager

import okio.Path
import okio.FileSystem
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

actual suspend fun DataExportImportManager.createZipFromDirectory(sourceDir: Path, zipFile: Path) {
    ZipOutputStream(zipFile.toFile().outputStream()).use { zip ->
        FileSystem.SYSTEM.listRecursively(sourceDir).forEach { path ->
            if (FileSystem.SYSTEM.metadata(path).isRegularFile) {
                val relativePath = path.toString().removePrefix("$sourceDir/")
                zip.putNextEntry(ZipEntry(relativePath))
                FileSystem.SYSTEM.read(path) {
                    readByteArray().let { zip.write(it) }
                }
                zip.closeEntry()
            }
        }
    }
}

actual suspend fun DataExportImportManager.extractZipToDirectory(zipFile: Path, targetDir: Path) {
    ZipInputStream(zipFile.toFile().inputStream()).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            val targetFile = targetDir.resolve(entry.name)
            if (entry.isDirectory) {
                FileSystem.SYSTEM.createDirectories(targetFile)
            } else {
                targetFile.parent?.let { FileSystem.SYSTEM.createDirectories(it) }
                FileSystem.SYSTEM.write(targetFile) {
                    write(zip.readBytes())
                }
            }
            entry = zip.nextEntry
        }
    }
}