package com.example.data

import kotlinx.coroutines.flow.Flow
import java.io.File

class StorageRepository(private val storageDao: StorageDao) {

    // --- Files ---
    fun getAllFiles(): Flow<List<DbFile>> = storageDao.getAllFiles()

    fun getFilesByFolder(folderId: Int): Flow<List<DbFile>> = storageDao.getFilesByFolder(folderId)

    fun getRecycleBinFiles(): Flow<List<DbFile>> = storageDao.getRecycleBinFiles()

    fun getFavoriteFiles(): Flow<List<DbFile>> = storageDao.getFavoriteFiles()

    fun getOfflineFiles(): Flow<List<DbFile>> = storageDao.getOfflineFiles()

    fun getVaultFiles(): Flow<List<DbFile>> = storageDao.getVaultFiles()

    suspend fun getFileById(fileId: Int): DbFile? = storageDao.getFileById(fileId)

    suspend fun insertFile(file: DbFile): Long = storageDao.insertFile(file)

    suspend fun updateFile(file: DbFile) = storageDao.updateFile(file)

    suspend fun deleteFile(file: DbFile) = storageDao.deleteFile(file)

    suspend fun deleteFileById(fileId: Int) = storageDao.deleteFileById(fileId)

    // --- Folders ---
    fun getFoldersByParent(parentId: Int): Flow<List<DbFolder>> = storageDao.getFoldersByParent(parentId)

    suspend fun getFolderById(folderId: Int): DbFolder? = storageDao.getFolderById(folderId)

    suspend fun insertFolder(folder: DbFolder): Long = storageDao.insertFolder(folder)

    suspend fun updateFolder(folder: DbFolder) = storageDao.updateFolder(folder)

    suspend fun deleteFolder(folder: DbFolder) = storageDao.deleteFolder(folder)

    suspend fun deleteFolderById(folderId: Int) = storageDao.deleteFolderById(folderId)

    suspend fun getAllFoldersSync(): List<DbFolder> = storageDao.getAllFoldersSync()

    // --- User Session ---
    fun getUserSession(): Flow<UserSession?> = storageDao.getUserSession()

    suspend fun getUserSessionSync(): UserSession? = storageDao.getUserSessionSync()

    suspend fun insertUserSession(session: UserSession) = storageDao.insertUserSession(session)

    suspend fun updateUserSession(session: UserSession) = storageDao.updateUserSession(session)

    // --- Custom logic: Seed sample data if database is empty ---
    suspend fun seedDatabaseIfEmpty() {
        val existingSession = storageDao.getUserSessionSync()
        if (existingSession == null) {
            // Create default session
            storageDao.insertUserSession(
                UserSession(
                    id = 1,
                    email = null,
                    isGuest = false,
                    isOnboarded = false,
                    storagePlan = "Free"
                )
            )

            // Seed mock folders
            val personalFolderId = storageDao.insertFolder(DbFolder(name = "Personal", parentId = 0))
            val workFolderId = storageDao.insertFolder(DbFolder(name = "Work Documents", parentId = 0))
            val mediaFolderId = storageDao.insertFolder(DbFolder(name = "Media Highlights", parentId = 0))
            
            // Subfolders
            storageDao.insertFolder(DbFolder(name = "Tax 2026", parentId = workFolderId.toInt()))
            storageDao.insertFolder(DbFolder(name = "Vacation Photos", parentId = mediaFolderId.toInt()))

            // Seed mock files
            storageDao.insertFile(
                DbFile(
                    name = "Annual_Tax_Report.pdf",
                    sizeBytes = 14500000, // 14.5 MB
                    type = "PDF",
                    folderId = workFolderId.toInt(),
                    isFavorite = true,
                    isOffline = true,
                    createdAt = System.currentTimeMillis() - 86400000 * 5 // 5 days ago
                )
            )

            storageDao.insertFile(
                DbFile(
                    name = "Synthwave_Track.mp3",
                    sizeBytes = 6200000, // 6.2 MB
                    type = "AUDIO",
                    folderId = mediaFolderId.toInt(),
                    isFavorite = true,
                    createdAt = System.currentTimeMillis() - 86400000 * 3
                )
            )

            storageDao.insertFile(
                DbFile(
                    name = "Summer_Vlog.mp4",
                    sizeBytes = 256000000, // 256 MB (Large File)
                    type = "VIDEO",
                    folderId = mediaFolderId.toInt(),
                    createdAt = System.currentTimeMillis() - 86400000 * 2
                )
            )

            storageDao.insertFile(
                DbFile(
                    name = "Profile_Picture.png",
                    sizeBytes = 2400000, // 2.4 MB
                    type = "IMAGE",
                    folderId = personalFolderId.toInt(),
                    createdAt = System.currentTimeMillis() - 86400000 * 10
                )
            )

            // Duplicate files helper: seed duplicates for Finder
            storageDao.insertFile(
                DbFile(
                    name = "Receipt_June.jpg",
                    sizeBytes = 450000, // 450 KB
                    type = "IMAGE",
                    folderId = workFolderId.toInt(),
                    createdAt = System.currentTimeMillis() - 86400000
                )
            )

            storageDao.insertFile(
                DbFile(
                    name = "Receipt_June.jpg", // Same name and size to simulate a perfect duplicate
                    sizeBytes = 450000,
                    type = "IMAGE",
                    folderId = personalFolderId.toInt(),
                    createdAt = System.currentTimeMillis()
                )
            )

            storageDao.insertFile(
                DbFile(
                    name = "Backup_Notes.zip",
                    sizeBytes = 48000000, // 48 MB
                    type = "ZIP",
                    folderId = 0,
                    createdAt = System.currentTimeMillis() - 86400000 * 12
                )
            )

            storageDao.insertFile(
                DbFile(
                    name = "launcher_install.apk",
                    sizeBytes = 12500000, // 12.5 MB
                    type = "APK",
                    folderId = 0,
                    createdAt = System.currentTimeMillis() - 86400000 * 15
                )
            )
        }
    }
}
