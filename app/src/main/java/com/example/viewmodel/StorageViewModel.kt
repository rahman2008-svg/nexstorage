package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StorageDatabase.getDatabase(application)
    private val repository = StorageRepository(database.storageDao())

    // --- Onboarding & User State ---
    val userSession: StateFlow<UserSession?> = repository.getUserSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Current Folder Navigation ---
    private val _currentFolderId = MutableStateFlow(0) // 0 is root
    val currentFolderId: StateFlow<Int> = _currentFolderId.asStateFlow()

    private val _currentFolderStack = MutableStateFlow<List<DbFolder>>(emptyList())
    val currentFolderStack: StateFlow<List<DbFolder>> = _currentFolderStack.asStateFlow()

    // --- Navigation State switcher ---
    // Screens: "ONBOARDING", "MAIN", "LOCKED"
    private val _currentMainScreenTab = MutableStateFlow("HOME") // "HOME", "FILES", "BACKUP", "SHARED", "PROFILE", "ADMIN", "DUPLICATE", "LARGE", "BIN"
    val currentMainScreenTab: StateFlow<String> = _currentMainScreenTab.asStateFlow()

    private val _selectedFileCategory = MutableStateFlow("ALL")
    val selectedFileCategory: StateFlow<String> = _selectedFileCategory.asStateFlow()

    fun setSelectedFileCategory(category: String) {
        _selectedFileCategory.value = category
    }

    // --- Files and Folders Listing ---
    val allFiles: StateFlow<List<DbFile>> = repository.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteFiles: StateFlow<List<DbFile>> = repository.getFavoriteFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineFiles: StateFlow<List<DbFile>> = repository.getOfflineFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultFiles: StateFlow<List<DbFile>> = repository.getVaultFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recycleBinFiles: StateFlow<List<DbFile>> = repository.getRecycleBinFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Folders in the current folder
    val currentFolders: StateFlow<List<DbFolder>> = _currentFolderId
        .flatMapLatest { id -> repository.getFoldersByParent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Files in the current folder (excluding deleted, vault, and active uploads)
    val currentFiles: StateFlow<List<DbFile>> = _currentFolderId
        .flatMapLatest { id -> repository.getFilesByFolder(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search & Filtering ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow("NAME_ASC") // "NAME_ASC", "SIZE_DESC", "DATE_DESC"
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    // --- Active Upload Simulation System ---
    private val _activeUploads = MutableStateFlow<List<DbFile>>(emptyList())
    val activeUploads: StateFlow<List<DbFile>> = _activeUploads.asStateFlow()

    private val uploadJobs = mutableMapOf<Int, Job>()

    // --- Media Players & Viewers state ---
    private val _playingMusic = MutableStateFlow<DbFile?>(null)
    val playingMusic: StateFlow<DbFile?> = _playingMusic.asStateFlow()

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _viewingVideo = MutableStateFlow<DbFile?>(null)
    val viewingVideo: StateFlow<DbFile?> = _viewingVideo.asStateFlow()

    private val _viewingPdf = MutableStateFlow<DbFile?>(null)
    val viewingPdf: StateFlow<DbFile?> = _viewingPdf.asStateFlow()

    // --- Vault Status ---
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _wrongPinError = MutableStateFlow<String?>(null)
    val wrongPinError: StateFlow<String?> = _wrongPinError.asStateFlow()

    // --- Backup Simulator State ---
    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _backupProgress = MutableStateFlow(0)
    val backupProgress: StateFlow<Int> = _backupProgress.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // --- Onboarding Operations ---
    fun completeOnboarding(email: String?, isGuest: Boolean, plan: String) {
        viewModelScope.launch {
            val session = repository.getUserSessionSync() ?: UserSession()
            repository.updateUserSession(
                session.copy(
                    email = email,
                    isGuest = isGuest,
                    isOnboarded = true,
                    storagePlan = plan,
                    isLocked = false
                )
            )
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            val session = repository.getUserSessionSync()
            if (session != null) {
                repository.updateUserSession(session.copy(themeMode = theme))
            }
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val session = repository.getUserSessionSync()
            if (session != null) {
                repository.updateUserSession(session.copy(appLanguage = lang))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Cancel any uploads
            uploadJobs.values.forEach { it.cancel() }
            uploadJobs.clear()
            _activeUploads.value = emptyList()

            // Reset database or session
            val session = repository.getUserSessionSync()
            if (session != null) {
                repository.updateUserSession(
                    UserSession(
                        id = 1,
                        email = null,
                        isGuest = false,
                        isOnboarded = false,
                        storagePlan = "Free"
                    )
                )
            }
            _currentFolderId.value = 0
            _currentFolderStack.value = emptyList()
            _currentMainScreenTab.value = "HOME"
            _isVaultUnlocked.value = false
        }
    }

    // --- Folder Navigation ---
    fun navigateToFolder(folder: DbFolder) {
        _currentFolderId.value = folder.id
        val stack = _currentFolderStack.value.toMutableList()
        stack.add(folder)
        _currentFolderStack.value = stack
    }

    fun navigateBack() {
        val stack = _currentFolderStack.value.toMutableList()
        if (stack.isNotEmpty()) {
            stack.removeAt(stack.size - 1)
            _currentFolderStack.value = stack
            _currentFolderId.value = if (stack.isNotEmpty()) stack.last().id else 0
        } else {
            _currentFolderId.value = 0
        }
    }

    fun navigateToRoot() {
        _currentFolderId.value = 0
        _currentFolderStack.value = emptyList()
    }

    fun changeTab(tab: String) {
        _currentMainScreenTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
    }

    // --- Folder Operations ---
    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.insertFolder(
                DbFolder(
                    name = name,
                    parentId = _currentFolderId.value
                )
            )
        }
    }

    fun renameFolder(folder: DbFolder, newName: String) {
        viewModelScope.launch {
            repository.updateFolder(folder.copy(name = newName))
        }
    }

    fun deleteFolder(folder: DbFolder) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
        }
    }

    // --- File Operations ---
    fun setFileFavorite(file: DbFile, isFav: Boolean) {
        viewModelScope.launch {
            repository.updateFile(file.copy(isFavorite = isFav))
        }
    }

    fun setFileOffline(file: DbFile, isOffline: Boolean) {
        viewModelScope.launch {
            repository.updateFile(file.copy(isOffline = isOffline))
        }
    }

    fun renameFile(file: DbFile, newName: String) {
        viewModelScope.launch {
            repository.updateFile(file.copy(name = newName))
        }
    }

    fun moveFile(file: DbFile, targetFolderId: Int) {
        viewModelScope.launch {
            repository.updateFile(file.copy(folderId = targetFolderId))
        }
    }

    fun toggleVaultFile(file: DbFile, isVault: Boolean) {
        viewModelScope.launch {
            repository.updateFile(file.copy(isVault = isVault))
        }
    }

    fun recycleFile(file: DbFile) {
        viewModelScope.launch {
            repository.updateFile(
                file.copy(
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun restoreFile(file: DbFile) {
        viewModelScope.launch {
            repository.updateFile(
                file.copy(
                    isDeleted = false,
                    deletedAt = 0
                )
            )
        }
    }

    fun deleteFilePermanently(file: DbFile) {
        viewModelScope.launch {
            repository.deleteFile(file)
        }
    }

    fun clearRecycleBin() {
        viewModelScope.launch {
            val list = recycleBinFiles.value
            list.forEach {
                repository.deleteFile(it)
            }
        }
    }

    // --- Security PIN & Lock ---
    fun setPinCode(pin: String?) {
        viewModelScope.launch {
            val session = repository.getUserSessionSync()
            if (session != null) {
                repository.updateUserSession(session.copy(pinCode = pin, isLocked = pin != null))
            }
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            val session = repository.getUserSessionSync()
            if (session != null) {
                val currentPin = session.pinCode ?: "1234" // Default mock PIN if enabled without PIN
                repository.updateUserSession(
                    session.copy(
                        pinCode = if (enabled) currentPin else null,
                        isLocked = enabled
                    )
                )
            }
        }
    }

    fun unlockApp(pin: String): Boolean {
        val session = userSession.value
        if (session != null && session.pinCode == pin) {
            viewModelScope.launch {
                repository.updateUserSession(session.copy(isLocked = false))
            }
            _wrongPinError.value = null
            return true
        } else {
            _wrongPinError.value = "Incorrect PIN. Please try again."
            return false
        }
    }

    fun lockAppManual() {
        viewModelScope.launch {
            val session = repository.getUserSessionSync()
            if (session != null && session.pinCode != null) {
                repository.updateUserSession(session.copy(isLocked = true))
            }
        }
    }

    fun unlockVault(pin: String): Boolean {
        val session = userSession.value
        val correctPin = session?.pinCode ?: "1234" // defaults to 1234 for vault if PIN is not yet set
        if (pin == correctPin) {
            _isVaultUnlocked.value = true
            _wrongPinError.value = null
            return true
        } else {
            _wrongPinError.value = "Incorrect PIN. Vault remains locked."
            return false
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    // --- File Share links ---
    fun generateShareLink(file: DbFile, passwordProtected: Boolean, expiryDays: Int) {
        viewModelScope.launch {
            val rand = (100000..999999).random()
            val link = "https://nexstorage.link/s/${file.name.replace(" ", "_")}_$rand"
            val password = if (passwordProtected) "Pass${(100..999).random()}" else null
            val expiry = System.currentTimeMillis() + (86400000 * expiryDays)
            
            repository.updateFile(
                file.copy(
                    sharedLink = link,
                    sharedPassword = password,
                    sharedExpiry = expiry
                )
            )
        }
    }

    fun removeShareLink(file: DbFile) {
        viewModelScope.launch {
            repository.updateFile(
                file.copy(
                    sharedLink = null,
                    sharedPassword = null,
                    sharedExpiry = null
                )
            )
        }
    }

    // --- Simulated Upload Engine ---
    fun initiateUpload(fileName: String, fileType: String, sizeBytes: Long) {
        viewModelScope.launch {
            val currentFolder = _currentFolderId.value
            
            // 1. Insert file with UPLOADING status in the current folder
            val fileId = repository.insertFile(
                DbFile(
                    name = fileName,
                    sizeBytes = sizeBytes,
                    type = fileType,
                    folderId = currentFolder,
                    uploadStatus = "UPLOADING",
                    uploadProgress = 0,
                    createdAt = System.currentTimeMillis()
                )
            ).toInt()

            // 2. Start progress updates coroutine
            val job = launchUploadProgressJob(fileId)
            uploadJobs[fileId] = job
        }
    }

    private fun launchUploadProgressJob(fileId: Int): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            var progress = 0
            while (progress < 100) {
                delay(400) // update every 400ms
                val file = repository.getFileById(fileId) ?: break
                
                if (file.uploadStatus == "PAUSED") {
                    // Just wait or yield
                    delay(300)
                    continue
                }
                if (file.uploadStatus != "UPLOADING") {
                    break // Cancelled or deleted
                }

                progress += (5..15).random()
                if (progress > 100) progress = 100

                val updatedFile = file.copy(
                    uploadProgress = progress,
                    uploadStatus = if (progress == 100) "COMPLETED" else "UPLOADING"
                )
                repository.updateFile(updatedFile)
            }
            uploadJobs.remove(fileId)
        }
    }

    fun pauseUpload(file: DbFile) {
        viewModelScope.launch {
            repository.updateFile(file.copy(uploadStatus = "PAUSED"))
        }
    }

    fun resumeUpload(file: DbFile) {
        viewModelScope.launch {
            repository.updateFile(file.copy(uploadStatus = "UPLOADING"))
            val job = launchUploadProgressJob(file.id)
            uploadJobs[file.id] = job
        }
    }

    fun cancelUpload(file: DbFile) {
        viewModelScope.launch {
            uploadJobs[file.id]?.cancel()
            uploadJobs.remove(file.id)
            repository.deleteFile(file)
        }
    }

    // --- Simulated Auto-Backup System ---
    fun toggleBackupCategory(category: String, enabled: Boolean) {
        viewModelScope.launch {
            val session = repository.getUserSessionSync() ?: return@launch
            val updated = when (category) {
                "PHOTOS" -> session.copy(autoBackupPhotos = enabled)
                "VIDEOS" -> session.copy(autoBackupVideos = enabled)
                "CONTACTS" -> session.copy(autoBackupContacts = enabled)
                "DOCUMENTS" -> session.copy(autoBackupDocuments = enabled)
                else -> session
            }
            repository.updateUserSession(updated)

            if (enabled) {
                simulateBackupSync()
            }
        }
    }

    private fun simulateBackupSync() {
        if (_isBackingUp.value) return
        viewModelScope.launch {
            _isBackingUp.value = true
            _backupProgress.value = 0
            while (_backupProgress.value < 100) {
                delay(300)
                _backupProgress.value += (10..20).random()
                if (_backupProgress.value >= 100) {
                    _backupProgress.value = 100
                }
            }
            delay(1000)
            _isBackingUp.value = false
        }
    }

    // --- Media Players Controls ---
    fun playMusic(file: DbFile?) {
        _playingMusic.value = file
        _isMusicPlaying.value = file != null
    }

    fun setMusicPlaying(isPlaying: Boolean) {
        _isMusicPlaying.value = isPlaying
    }

    fun viewVideo(file: DbFile?) {
        _viewingVideo.value = file
    }

    fun viewPdf(file: DbFile?) {
        _viewingPdf.value = file
    }

    // --- Settings & Cache Operations ---
    fun clearCache() {
        // Mock success, can also toast or notify UI state
    }

    // --- Duplicate & Large File Helpers ---
    fun getDuplicateFiles(): List<DbFile> {
        val files = allFiles.value.filter { !it.isDeleted && !it.isVault && it.uploadStatus == "COMPLETED" }
        // Group by lowercase name and size
        val groups = files.groupBy { Pair(it.name.lowercase(), it.sizeBytes) }
        return groups.filter { it.value.size > 1 }.values.flatten()
    }

    fun getLargeFiles(): List<DbFile> {
        // Files larger than 10MB
        return allFiles.value.filter { !it.isDeleted && !it.isVault && it.sizeBytes > 10 * 1024 * 1024 && it.uploadStatus == "COMPLETED" }
    }

    suspend fun getAllFoldersSync(): List<DbFolder> {
        return repository.getAllFoldersSync()
    }
}
