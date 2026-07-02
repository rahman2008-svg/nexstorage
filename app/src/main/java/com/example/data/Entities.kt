package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class DbFolder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val parentId: Int = 0, // 0 is root directory
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "files")
data class DbFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sizeBytes: Long,
    val type: String, // "IMAGE", "VIDEO", "AUDIO", "PDF", "ZIP", "APK", "DOC"
    val folderId: Int = 0, // 0 is root directory
    val isFavorite: Boolean = false,
    val isOffline: Boolean = false,
    val isVault: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long = 0,
    val uploadStatus: String = "COMPLETED", // "COMPLETED", "UPLOADING", "PAUSED", "PENDING"
    val uploadProgress: Int = 100, // 0 to 100
    val sharedLink: String? = null,
    val sharedPassword: String? = null,
    val sharedExpiry: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_session")
data class UserSession(
    @PrimaryKey val id: Int = 1, // Singleton row for simple offline state
    val email: String? = null,
    val isGuest: Boolean = false,
    val isOnboarded: Boolean = false,
    val storagePlan: String = "Free", // "Free" (10GB), "Premium" (256GB), "Ultimate" (1TB)
    val pinCode: String? = null,
    val isLocked: Boolean = false,
    val autoBackupPhotos: Boolean = false,
    val autoBackupVideos: Boolean = false,
    val autoBackupContacts: Boolean = false,
    val autoBackupDocuments: Boolean = false,
    val appLanguage: String = "English",
    val themeMode: String = "Light" // "Dark", "Light"
)
