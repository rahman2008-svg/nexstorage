package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {

    // --- Files Queries ---
    @Query("SELECT * FROM files WHERE isDeleted = 0 AND isVault = 0 ORDER BY lastAccessed DESC")
    fun getAllFiles(): Flow<List<DbFile>>

    @Query("SELECT * FROM files WHERE folderId = :folderId AND isDeleted = 0 AND isVault = 0 ORDER BY name ASC")
    fun getFilesByFolder(folderId: Int): Flow<List<DbFile>>

    @Query("SELECT * FROM files WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getRecycleBinFiles(): Flow<List<DbFile>>

    @Query("SELECT * FROM files WHERE isFavorite = 1 AND isDeleted = 0 AND isVault = 0 ORDER BY name ASC")
    fun getFavoriteFiles(): Flow<List<DbFile>>

    @Query("SELECT * FROM files WHERE isOffline = 1 AND isDeleted = 0 AND isVault = 0 ORDER BY name ASC")
    fun getOfflineFiles(): Flow<List<DbFile>>

    @Query("SELECT * FROM files WHERE isVault = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getVaultFiles(): Flow<List<DbFile>>

    @Query("SELECT * FROM files WHERE id = :fileId")
    suspend fun getFileById(fileId: Int): DbFile?

    // --- Folder Queries ---
    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY name ASC")
    fun getFoldersByParent(parentId: Int): Flow<List<DbFolder>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Int): DbFolder?

    @Query("SELECT * FROM folders")
    suspend fun getAllFoldersSync(): List<DbFolder>

    // --- Database Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: DbFile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: DbFolder): Long

    @Update
    suspend fun updateFile(file: DbFile)

    @Update
    suspend fun updateFolder(folder: DbFolder)

    @Delete
    suspend fun deleteFile(file: DbFile)

    @Query("DELETE FROM files WHERE id = :fileId")
    suspend fun deleteFileById(fileId: Int)

    @Delete
    suspend fun deleteFolder(folder: DbFolder)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Int)

    // --- User Session Queries ---
    @Query("SELECT * FROM user_session WHERE id = 1")
    fun getUserSession(): Flow<UserSession?>

    @Query("SELECT * FROM user_session WHERE id = 1")
    suspend fun getUserSessionSync(): UserSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSession(session: UserSession)

    @Update
    suspend fun updateUserSession(session: UserSession)
}
