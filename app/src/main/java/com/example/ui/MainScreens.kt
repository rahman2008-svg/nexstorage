package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DbFile
import com.example.data.DbFolder
import com.example.data.UserSession
import com.example.ui.theme.*
import com.example.viewmodel.StorageViewModel
import kotlinx.coroutines.launch

// --- ONBOARDING NAVIGATION ---
@Composable
fun OnboardingScreen(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) } // 1: Welcome, 2: Permissions, 3: Account, 4: Plan Selection

    // Local account inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("Free") } // "Free", "Premium", "Ultimate"

    // Local Simulated Permissions Checked Status
    var photoPermChecked by remember { mutableStateOf(false) }
    var filePermChecked by remember { mutableStateOf(false) }
    var notifyPermChecked by remember { mutableStateOf(false) }

    when (step) {
        1 -> {
            WelcomeStep(onContinue = { step = 2 })
        }
        2 -> {
            PermissionsStep(
                photoGranted = photoPermChecked,
                onTogglePhoto = { photoPermChecked = it },
                fileGranted = filePermChecked,
                onToggleFile = { filePermChecked = it },
                notifyGranted = notifyPermChecked,
                onToggleNotify = { notifyPermChecked = it },
                onContinue = { step = 3 }
            )
        }
        3 -> {
            AccountStep(
                email = emailInput,
                onEmailChange = { emailInput = it },
                password = passwordInput,
                onPasswordChange = { passwordInput = it },
                onContinue = { isGuest ->
                    if (isGuest) {
                        step = 4
                    } else {
                        // Email signup validation
                        if (emailInput.contains("@") && passwordInput.length >= 6) {
                            step = 4
                        }
                    }
                }
            )
        }
        4 -> {
            PlanSelectionStep(
                selectedPlan = selectedPlan,
                onSelectPlan = { selectedPlan = it },
                onComplete = {
                    viewModel.completeOnboarding(
                        email = if (emailInput.isEmpty()) null else emailInput,
                        isGuest = emailInput.isEmpty(),
                        plan = selectedPlan
                    )
                }
            )
        }
    }
}

@Composable
fun WelcomeStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(SapphireBlue, CyberTeal)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CloudCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to NexStorage",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your secure, high-speed unified vault for local files, automated cloud backups, duplicate clearing, and private encryption keys.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Get Started Action Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("get_started_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun PermissionsStep(
    photoGranted: Boolean,
    onTogglePhoto: (Boolean) -> Unit,
    fileGranted: Boolean,
    onToggleFile: (Boolean) -> Unit,
    notifyGranted: Boolean,
    onToggleNotify: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Security,
            contentDescription = null,
            tint = CyberTeal,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Grant Permissions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Enable system access to manage storage and synchronize your directory seamlessly.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Photos Permission Card
        PermissionCard(
            title = "Photos & Videos Access",
            description = "Needed to catalog media directories and sync backup files.",
            granted = photoGranted,
            onToggle = onTogglePhoto,
            icon = Icons.Filled.PhotoLibrary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // File Manager Permission Card
        PermissionCard(
            title = "All Files & Documents",
            description = "Required for fully integrated file management and local downloads.",
            granted = fileGranted,
            onToggle = onToggleFile,
            icon = Icons.Filled.FolderOpen
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Permission Card
        PermissionCard(
            title = "Upload & Cloud Alerts",
            description = "Get notified of background backups and file sharing statuses.",
            granted = notifyGranted,
            onToggle = onToggleNotify,
            icon = Icons.Filled.NotificationsActive
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    onToggle: (Boolean) -> Unit,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }

            Switch(
                checked = granted,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SapphireBlue,
                    checkedTrackColor = SapphireBlue.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun AccountStep(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onContinue: (Boolean) -> Unit // returns isGuest
) {
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null,
            tint = SapphireBlue,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Create Storage Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                onEmailChange(it)
                errorMsg = null
            },
            label = { Text("Email Address") },
            placeholder = { Text("yourname@example.com") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                onPasswordChange(it)
                errorMsg = null
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMsg != null) {
            Text(
                text = errorMsg ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isEmpty() || !email.contains("@")) {
                    errorMsg = "Please enter a valid email address."
                } else if (password.length < 6) {
                    errorMsg = "Password must be at least 6 characters."
                } else {
                    onContinue(false)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "OR",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Guest Mode Trigger
        OutlinedButton(
            onClick = {
                onContinue(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Continue as Guest (Limited 10GB)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlanSelectionStep(
    selectedPlan: String,
    onSelectPlan: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.OfflineBolt,
            contentDescription = null,
            tint = SafeAmber,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Select Storage Plan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        PlanCard(
            name = "Free",
            storage = "10 GB",
            price = "$0.00 / month",
            description = "Ideal for essential document backups and basic file sharing.",
            selected = selectedPlan == "Free",
            onClick = { onSelectPlan("Free") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PlanCard(
            name = "Premium",
            storage = "256 GB",
            price = "$2.99 / month",
            description = "Our most popular package for photo galleries and videos.",
            selected = selectedPlan == "Premium",
            onClick = { onSelectPlan("Premium") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PlanCard(
            name = "Ultimate",
            storage = "1 TB",
            price = "$9.99 / month",
            description = "Maximum redundancy for complete device systems.",
            selected = selectedPlan == "Ultimate",
            onClick = { onSelectPlan("Ultimate") }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Complete Configuration", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun PlanCard(
    name: String,
    storage: String,
    price: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            2.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "$name Plan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    Text(text = storage, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                Text(text = price, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

// --- SECURE PIN LOCK PAD SCREEN ---
@Composable
fun SecurePinLockScreen(
    title: String,
    errorMsg: String?,
    onPinEntered: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = SafeAmber,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // PIN Bubbles Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            for (i in 1..4) {
                val isFilled = enteredPin.length >= i
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            CircleShape
                        )
                )
            }
        }

        if (errorMsg != null) {
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Numeric Keypad Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "Delete")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    row.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .clickable {
                                    when (digit) {
                                        "Clear" -> {
                                            enteredPin = ""
                                        }

                                        "Delete" -> {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                        }

                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += digit
                                                if (enteredPin.length == 4) {
                                                    onPinEntered(enteredPin)
                                                    enteredPin = "" // clear on submit
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (digit == "Delete") {
                                Icon(imageVector = Icons.Default.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurface)
                            } else {
                                Text(
                                    text = digit,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (digit == "Clear") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- MAIN DASHBOARD BOTTOM SWITCHER HUB ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardHub(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val tab by viewModel.currentMainScreenTab.collectAsStateWithLifecycle()
    val session by viewModel.userSession.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Media file viewer triggers
    val activePlayingMusic by viewModel.playingMusic.collectAsStateWithLifecycle()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val activeViewingVideo by viewModel.viewingVideo.collectAsStateWithLifecycle()
    val activeViewingPdf by viewModel.viewingPdf.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val sleekColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SleekBlue600,
                    selectedTextColor = SleekBlue600,
                    indicatorColor = SleekBlue100,
                    unselectedIconColor = SleekTextSlate400,
                    unselectedTextColor = SleekTextSlate400
                )

                NavigationBarItem(
                    selected = tab == "HOME",
                    onClick = { viewModel.changeTab("HOME") },
                    icon = { Icon(imageVector = if (tab == "HOME") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = sleekColors
                )
                NavigationBarItem(
                    selected = tab == "FILES",
                    onClick = { viewModel.changeTab("FILES") },
                    icon = { Icon(imageVector = if (tab == "FILES") Icons.Filled.FolderOpen else Icons.Outlined.Folder, contentDescription = "Files") },
                    label = { Text("Files", fontSize = 11.sp) },
                    colors = sleekColors
                )
                NavigationBarItem(
                    selected = tab == "BACKUP",
                    onClick = { viewModel.changeTab("BACKUP") },
                    icon = { Icon(imageVector = if (tab == "BACKUP") Icons.Filled.CloudUpload else Icons.Outlined.CloudQueue, contentDescription = "Backup") },
                    label = { Text("Backup", fontSize = 11.sp) },
                    colors = sleekColors
                )
                NavigationBarItem(
                    selected = tab == "SHARED",
                    onClick = { viewModel.changeTab("SHARED") },
                    icon = { Icon(imageVector = if (tab == "SHARED") Icons.Filled.Link else Icons.Outlined.Link, contentDescription = "Shared") },
                    label = { Text("Shared", fontSize = 11.sp) },
                    colors = sleekColors
                )
                NavigationBarItem(
                    selected = tab == "PROFILE",
                    onClick = { viewModel.changeTab("PROFILE") },
                    icon = { Icon(imageVector = if (tab == "PROFILE") Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = sleekColors
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (tab) {
                "HOME" -> HomeScreenTab(viewModel, onNavigateToTab = { viewModel.changeTab(it) })
                "FILES" -> FilesScreenTab(viewModel, snackbarHostState)
                "BACKUP" -> BackupScreenTab(viewModel)
                "SHARED" -> SharedScreenTab(viewModel)
                "PROFILE" -> ProfileScreenTab(viewModel)
                "ADMIN" -> AdminDashboardScreen(viewModel)
                "DUPLICATE" -> DuplicateFinderScreen(viewModel, snackbarHostState)
                "LARGE" -> LargeFileFinderScreen(viewModel, snackbarHostState)
                "BIN" -> RecycleBinScreen(viewModel, snackbarHostState)
            }

            // Floating Audio overlay player
            if (activePlayingMusic != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    SimulatedMusicPlayer(
                        file = activePlayingMusic!!,
                        isPlaying = isMusicPlaying,
                        onPlayPauseToggle = { viewModel.setMusicPlaying(it) },
                        onClose = { viewModel.playMusic(null) }
                    )
                }
            }

            // Floating Video overlay viewport
            if (activeViewingVideo != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    SimulatedVideoPlayer(
                        file = activeViewingVideo!!,
                        onClose = { viewModel.viewVideo(null) }
                    )
                }
            }

            // Floating PDF Reader overlay
            if (activeViewingPdf != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    SimulatedPdfViewer(
                        file = activeViewingPdf!!,
                        onClose = { viewModel.viewPdf(null) }
                    )
                }
            }
        }
    }
}

// --- 🏠 TAB: HOME SCREEN ---
@Composable
fun HomeScreenTab(
    viewModel: StorageViewModel,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allFilesList by viewModel.allFiles.collectAsStateWithLifecycle()
    val session by viewModel.userSession.collectAsStateWithLifecycle()

    // Calculate Storage Consumed
    val planQuotaBytes = when (session?.storagePlan) {
        "Premium" -> 256L * 1024L * 1024L * 1024L
        "Ultimate" -> 1024L * 1024L * 1024L * 1024L
        else -> 10L * 1024L * 1024L * 1024L // Free: 10 GB
    }
    val usedBytes = allFilesList.filter { !it.isDeleted && !it.isVault && it.uploadStatus == "COMPLETED" }.sumOf { it.sizeBytes }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome and Plan Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = session?.email ?: "Guest User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SafeAmber.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${session?.storagePlan ?: "Free"} Plan",
                    color = SafeAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Storage visual usage meter
        StorageGauge(usedBytes = usedBytes, totalBytes = planQuotaBytes)

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Categories heading and grid
        Text(
            text = "Categories",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryCard(
                label = "Docs",
                icon = Icons.Default.Description,
                bgColor = Color(0xFFFFF2E6),
                iconColor = Color(0xFFEA580C),
                onClick = {
                    viewModel.setSelectedFileCategory("PDF")
                    viewModel.changeTab("FILES")
                },
                modifier = Modifier.weight(1f)
            )

            CategoryCard(
                label = "Photos",
                icon = Icons.Default.Image,
                bgColor = Color(0xFFEFF6FF),
                iconColor = Color(0xFF2563EB),
                onClick = {
                    viewModel.setSelectedFileCategory("IMAGE")
                    viewModel.changeTab("FILES")
                },
                modifier = Modifier.weight(1f)
            )

            CategoryCard(
                label = "Videos",
                icon = Icons.Default.Videocam,
                bgColor = Color(0xFFF5F3FF),
                iconColor = Color(0xFF7C3AED),
                onClick = {
                    viewModel.setSelectedFileCategory("VIDEO")
                    viewModel.changeTab("FILES")
                },
                modifier = Modifier.weight(1f)
            )

            CategoryCard(
                label = "Music",
                icon = Icons.Default.MusicNote,
                bgColor = Color(0xFFECFDF5),
                iconColor = Color(0xFF059669),
                onClick = {
                    viewModel.setSelectedFileCategory("AUDIO")
                    viewModel.changeTab("FILES")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Optimize shortcuts (Recycle Bin, Duplicates, Large Files)
        Text(
            text = "Storage Optimization Tools",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OptimizeCard(
                title = "Recycle Bin",
                count = "${viewModel.recycleBinFiles.collectAsStateWithLifecycle().value.size} Files",
                icon = Icons.Filled.DeleteSweep,
                color = SoftRed,
                onClick = { onNavigateToTab("BIN") },
                modifier = Modifier.weight(1f)
            )

            OptimizeCard(
                title = "Duplicates",
                count = "${viewModel.getDuplicateFiles().size} Files",
                icon = Icons.Filled.FileCopy,
                color = SapphireBlue,
                onClick = { onNavigateToTab("DUPLICATE") },
                modifier = Modifier.weight(1f)
            )

            OptimizeCard(
                title = "Large Files",
                count = "${viewModel.getLargeFiles().size} Files",
                icon = Icons.Filled.FolderZip,
                color = SafeAmber,
                onClick = { onNavigateToTab("LARGE") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Files
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "View All",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onNavigateToTab("FILES") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val recentFiles = allFilesList.filter { !it.isDeleted && !it.isVault && it.uploadStatus == "COMPLETED" }.take(5)
        if (recentFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No recent files uploaded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentFiles.forEach { file ->
                    RecentFileItem(file = file, onClick = {
                        when (file.type) {
                            "PDF" -> viewModel.viewPdf(file)
                            "VIDEO" -> viewModel.viewVideo(file)
                            "AUDIO" -> viewModel.playMusic(file)
                            else -> viewModel.viewPdf(file) // Fallback default text preview
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun OptimizeCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = count, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RecentFileItem(file: DbFile, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(getFileTypeColor(file.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFileTypeIcon(file.type),
                    contentDescription = null,
                    tint = getFileTypeColor(file.type)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${file.type} • ${formatSize(file.sizeBytes)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryCard(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextSlate600,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- 📁 TAB: FILES MANAGER ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesScreenTab(
    viewModel: StorageViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currentFolderId by viewModel.currentFolderId.collectAsStateWithLifecycle()
    val stack by viewModel.currentFolderStack.collectAsStateWithLifecycle()

    val subFolders by viewModel.currentFolders.collectAsStateWithLifecycle()
    val filesInFolder by viewModel.currentFiles.collectAsStateWithLifecycle()
    val activeUploadsList by viewModel.activeUploads.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Dialog state controllers
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }

    var showUploadSelectionDialog by remember { mutableStateOf(false) }

    // Dropdown options
    var activeFileMenu by remember { mutableStateOf<DbFile?>(null) }
    var activeFolderMenu by remember { mutableStateOf<DbFolder?>(null) }

    // Dialogs for operations
    var fileToRename by remember { mutableStateOf<DbFile?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var fileToMove by remember { mutableStateOf<DbFile?>(null) }
    var folderToRename by remember { mutableStateOf<DbFolder?>(null) }
    var folderRenameInput by remember { mutableStateOf("") }

    var fileToShare by remember { mutableStateOf<DbFile?>(null) }
    var sharePasswordProtect by remember { mutableStateOf(false) }
    var shareExpiryDays by remember { mutableStateOf(7) }

    var fileDetailsToShow by remember { mutableStateOf<DbFile?>(null) }

    // Categorized Category Filtering Button states
    val activeCategorySelection by viewModel.selectedFileCategory.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("ALL") } // "ALL", "IMAGE", "VIDEO", "AUDIO", "PDF", "ZIP", "APK"

    LaunchedEffect(activeCategorySelection) {
        selectedCategory = activeCategorySelection
    }

    LaunchedEffect(selectedCategory) {
        viewModel.setSelectedFileCategory(selectedCategory)
    }

    // Safe drawing variables
    val allFilesList by viewModel.allFiles.collectAsStateWithLifecycle()

    // Filter and Sort Lists
    val filteredFiles = remember(filesInFolder, searchQuery, selectedCategory, sortOrder) {
        var list = filesInFolder.filter { file ->
            val matchesSearch = file.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "ALL" || file.type == selectedCategory
            matchesSearch && matchesCategory
        }

        when (sortOrder) {
            "NAME_ASC" -> list = list.sortedBy { it.name.lowercase() }
            "SIZE_DESC" -> list = list.sortedByDescending { it.sizeBytes }
            "DATE_DESC" -> list = list.sortedByDescending { it.createdAt }
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Search bar & folder creation row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search files...", fontSize = 14.sp) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            // Create folder button
            IconButton(
                onClick = { showCreateFolderDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Icon(imageVector = Icons.Filled.CreateNewFolder, contentDescription = "Create Folder", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Scrolling Filter Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("ALL", "IMAGE", "VIDEO", "AUDIO", "PDF", "ZIP", "APK")
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Breadcrumbs Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Root",
                fontSize = 14.sp,
                color = if (currentFolderId == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (currentFolderId == 0) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.clickable { viewModel.navigateToRoot() }
            )
            stack.forEachIndexed { index, folder ->
                Text(
                    text = " > ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = folder.name,
                    fontSize = 14.sp,
                    color = if (index == stack.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (index == stack.size - 1) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.clickable {
                        // Go back to this parent folder level
                        val targetStack = stack.take(index + 1)
                        viewModel.navigateToFolder(folder)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sorting order picker
            Box {
                var showSortMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(imageVector = Icons.Filled.Sort, contentDescription = "Sort Options")
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    DropdownMenuItem(text = { Text("Name (A-Z)") }, onClick = { viewModel.setSortOrder("NAME_ASC"); showSortMenu = false })
                    DropdownMenuItem(text = { Text("Size (Large-Small)") }, onClick = { viewModel.setSortOrder("SIZE_DESC"); showSortMenu = false })
                    DropdownMenuItem(text = { Text("Date (Newest-Oldest)") }, onClick = { viewModel.setSortOrder("DATE_DESC"); showSortMenu = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FILES + FOLDERS SCROLLING AREA
        Box(modifier = Modifier.weight(1f)) {
            if (subFolders.isEmpty() && filteredFiles.isEmpty() && activeUploadsList.isEmpty()) {
                // Empty state placeholder
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "This folder is empty.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Create a folder or upload a file using the blue action button.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Folders grid-like list
                    items(subFolders) { folder ->
                        FolderItemRow(
                            folder = folder,
                            onNavigate = { viewModel.navigateToFolder(folder) },
                            onOptions = { activeFolderMenu = folder }
                        )
                    }

                    // 2. Active uploading files
                    val activeFiles = allFilesList.filter { it.uploadStatus != "COMPLETED" && it.folderId == currentFolderId }
                    items(activeFiles) { file ->
                        ActiveUploadItemRow(
                            file = file,
                            onPause = { viewModel.pauseUpload(file) },
                            onResume = { viewModel.resumeUpload(file) },
                            onCancel = { viewModel.cancelUpload(file) }
                        )
                    }

                    // 3. Regular Files
                    items(filteredFiles) { file ->
                        FileItemRow(
                            file = file,
                            onClick = {
                                when (file.type) {
                                    "PDF" -> viewModel.viewPdf(file)
                                    "VIDEO" -> viewModel.viewVideo(file)
                                    "AUDIO" -> viewModel.playMusic(file)
                                    else -> viewModel.viewPdf(file)
                                }
                            },
                            onOptions = { activeFileMenu = file }
                        )
                    }
                }
            }

            // Bottom Right Floating Action upload button
            FloatingActionButton(
                onClick = { showUploadSelectionDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Operations")
            }
        }
    }

    // --- DIALOGS & SHEET MODALS CODES ---

    // File Action Dropdown Bottom Sheet
    if (activeFileMenu != null) {
        val file = activeFileMenu!!
        AlertDialog(
            onDismissRequest = { activeFileMenu = null },
            title = { Text(text = file.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        activeFileMenu = null
                        when (file.type) {
                            "PDF" -> viewModel.viewPdf(file)
                            "VIDEO" -> viewModel.viewVideo(file)
                            "AUDIO" -> viewModel.playMusic(file)
                            else -> viewModel.viewPdf(file)
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.OpenInNew, contentDescription = null, tint = SapphireBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Open", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        fileToRename = file
                        renameInput = file.name
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = SafeAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Rename", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        fileToMove = file
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.DriveFileMove, contentDescription = null, tint = CyberTeal)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Move File", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        // Simple local copying
                        val parts = file.name.split(".")
                        val baseName = parts.firstOrNull() ?: "Copy"
                        val extension = if (parts.size > 1) ".${parts.last()}" else ""
                        viewModel.initiateUpload(
                            fileName = "${baseName}_copy$extension",
                            fileType = file.type,
                            sizeBytes = file.sizeBytes
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("File cloned. Copy operation simulating in background.")
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, tint = SapphireBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Copy", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        fileToShare = file
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Share (QR / Link)", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        // Toggle Offline
                        viewModel.setFileOffline(file, !file.isOffline)
                        scope.launch {
                            snackbarHostState.showSnackbar(if (file.isOffline) "Removed from Offline cache." else "Saved to Offline folder.")
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = if (file.isOffline) Icons.Filled.CloudOff else Icons.Filled.CloudDone, contentDescription = null, tint = CyberTeal)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (file.isOffline) "Remove Offline" else "Save Offline", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        // Toggle Vault security
                        viewModel.toggleVaultFile(file, true)
                        scope.launch {
                            snackbarHostState.showSnackbar("File safely locked inside Secure Vault.")
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = SafeAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Move to Secure Vault", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        // Zip compression simulator
                        val zipName = if (file.name.contains(".")) "${file.name.substringBeforeLast(".")}.zip" else "${file.name}.zip"
                        viewModel.initiateUpload(zipName, "ZIP", (file.sizeBytes * 0.75).toLong())
                        scope.launch {
                            snackbarHostState.showSnackbar("Compressing file... Simulated ZIP archive generating.")
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.FolderZip, contentDescription = null, tint = SapphireBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Compress (ZIP)", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        fileDetailsToShow = file
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Details", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFileMenu = null
                        viewModel.recycleFile(file)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "File moved to Recycle Bin.",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreFile(file)
                            }
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = SoftRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Delete", color = SoftRed)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeFileMenu = null }) { Text("Close") }
            }
        )
    }

    // Folder Actions Dialog Menu
    if (activeFolderMenu != null) {
        val folder = activeFolderMenu!!
        AlertDialog(
            onDismissRequest = { activeFolderMenu = null },
            title = { Text(text = "Folder Operations: ${folder.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        activeFolderMenu = null
                        folderToRename = folder
                        folderRenameInput = folder.name
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = SafeAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Rename Folder", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = {
                        activeFolderMenu = null
                        viewModel.deleteFolder(folder)
                        scope.launch {
                            snackbarHostState.showSnackbar("Folder deleted successfully.")
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = SoftRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Delete Folder", color = SoftRed)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeFolderMenu = null }) { Text("Close") }
            }
        )
    }

    // File Move Selection dialog
    if (fileToMove != null) {
        val file = fileToMove!!
        val scopeSyncFolders = remember { mutableStateOf<List<DbFolder>>(emptyList()) }
        LaunchedEffect(Unit) {
            scopeSyncFolders.value = viewModel.getAllFoldersSync()
        }

        AlertDialog(
            onDismissRequest = { fileToMove = null },
            title = { Text("Move File to Folder") },
            text = {
                Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    TextButton(onClick = {
                        viewModel.moveFile(file, 0)
                        fileToMove = null
                        scope.launch { snackbarHostState.showSnackbar("File moved to Root directory.") }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.FolderOpen, contentDescription = null, tint = SapphireBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Root Directory", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    scopeSyncFolders.value.filter { it.id != file.folderId }.forEach { folder ->
                        TextButton(onClick = {
                            viewModel.moveFile(file, folder.id)
                            fileToMove = null
                            scope.launch { snackbarHostState.showSnackbar("File moved to '${folder.name}'.") }
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Folder, contentDescription = null, tint = SapphireBlue)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(folder.name, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { fileToMove = null }) { Text("Cancel") }
            }
        )
    }

    // Create folder dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    label = { Text("Folder Name") },
                    placeholder = { Text("Work, Personal, Receipts...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (folderNameInput.isNotBlank()) {
                        viewModel.createFolder(folderNameInput)
                        folderNameInput = ""
                        showCreateFolderDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // File Rename Dialog
    if (fileToRename != null) {
        val file = fileToRename!!
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("File Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameInput.isNotBlank()) {
                        viewModel.renameFile(file, renameInput)
                        fileToRename = null
                        renameInput = ""
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) { Text("Cancel") }
            }
        )
    }

    // Folder Rename Dialog
    if (folderToRename != null) {
        val folder = folderToRename!!
        AlertDialog(
            onDismissRequest = { folderToRename = null },
            title = { Text("Rename Folder") },
            text = {
                OutlinedTextField(
                    value = folderRenameInput,
                    onValueChange = { folderRenameInput = it },
                    label = { Text("Folder Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (folderRenameInput.isNotBlank()) {
                        viewModel.renameFolder(folder, folderRenameInput)
                        folderToRename = null
                        folderRenameInput = ""
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToRename = null }) { Text("Cancel") }
            }
        )
    }

    // Upload Simulated File dialog selection
    if (showUploadSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showUploadSelectionDialog = false },
            title = { Text("Simulated Storage Upload") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose a file template to upload into this folder:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))

                    UploadSimCard(name = "Tax_Form_2026.pdf", type = "PDF", size = 18400000, icon = Icons.Filled.PictureAsPdf, onClick = {
                        viewModel.initiateUpload("Tax_Form_2026.pdf", "PDF", 18400000)
                        showUploadSelectionDialog = false
                    })

                    UploadSimCard(name = "Podcast_Episode.mp3", type = "AUDIO", size = 42500000, icon = Icons.Filled.MusicNote, onClick = {
                        viewModel.initiateUpload("Podcast_Episode.mp3", "AUDIO", 42500000)
                        showUploadSelectionDialog = false
                    })

                    UploadSimCard(name = "Drone_Capture.mp4", type = "VIDEO", size = 185000000, icon = Icons.Filled.VideoFile, onClick = {
                        viewModel.initiateUpload("Drone_Capture.mp4", "VIDEO", 185000000)
                        showUploadSelectionDialog = false
                    })

                    UploadSimCard(name = "Family_Portrait.png", type = "IMAGE", size = 4800000, icon = Icons.Filled.Image, onClick = {
                        viewModel.initiateUpload("Family_Portrait.png", "IMAGE", 4800000)
                        showUploadSelectionDialog = false
                    })

                    UploadSimCard(name = "AppPatch.apk", type = "APK", size = 15000000, icon = Icons.Filled.Android, onClick = {
                        viewModel.initiateUpload("AppPatch.apk", "APK", 15000000)
                        showUploadSelectionDialog = false
                    })
                }
            },
            confirmButton = {
                TextButton(onClick = { showUploadSelectionDialog = false }) { Text("Close") }
            }
        )
    }

    // File Sharing Dialog Options (QR Code & Expiry)
    if (fileToShare != null) {
        val file = fileToShare!!
        val clipboard = LocalClipboardManager.current

        AlertDialog(
            onDismissRequest = { fileToShare = null },
            title = { Text("Share Protected File Link") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (file.sharedLink != null) {
                        // Display active generated link and QR
                        Text(text = "Secure share link activated:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = file.sharedLink ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    clipboard.setText(AnnotatedString(file.sharedLink ?: ""))
                                    Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy link")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (file.sharedPassword != null) {
                            Text(
                                text = "🔒 Protected Password: ${file.sharedPassword}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Secure Scan QR Code", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
                        
                        // Vector-drawn QR Code Canvas Component
                        QrCodeCanvas(
                            link = file.sharedLink ?: "",
                            modifier = Modifier
                                .size(160.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.removeShareLink(file); fileToShare = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Deactivate Sharing & Revoke", color = Color.White)
                        }
                    } else {
                        // Configurator options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Password Security Lock", fontSize = 14.sp)
                            Switch(checked = sharePasswordProtect, onCheckedChange = { sharePasswordProtect = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Link Expiration Period", fontSize = 14.sp)
                            Box {
                                var showDaysMenu by remember { mutableStateOf(false) }
                                TextButton(onClick = { showDaysMenu = true }) {
                                    Text("$shareExpiryDays Days", fontWeight = FontWeight.Bold)
                                }
                                DropdownMenu(expanded = showDaysMenu, onDismissRequest = { showDaysMenu = false }) {
                                    DropdownMenuItem(text = { Text("1 Day") }, onClick = { shareExpiryDays = 1; showDaysMenu = false })
                                    DropdownMenuItem(text = { Text("7 Days") }, onClick = { shareExpiryDays = 7; showDaysMenu = false })
                                    DropdownMenuItem(text = { Text("30 Days") }, onClick = { shareExpiryDays = 30; showDaysMenu = false })
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.generateShareLink(file, sharePasswordProtect, shareExpiryDays)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Encrypted Share Link")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { fileToShare = null }) { Text("Done") }
            }
        )
    }

    // File details dialog
    if (fileDetailsToShow != null) {
        val file = fileDetailsToShow!!
        AlertDialog(
            onDismissRequest = { fileDetailsToShow = null },
            title = { Text("File Properties") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PropertyRow(label = "Name:", value = file.name)
                    PropertyRow(label = "Type Classification:", value = file.type)
                    PropertyRow(label = "Physical Size:", value = formatSize(file.sizeBytes))
                    PropertyRow(label = "Absolute Path:", value = "nexstorage://home/folder_${file.folderId}/${file.name}")
                    PropertyRow(label = "Date Uploaded:", value = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(file.createdAt)))
                    PropertyRow(label = "Offline Cached Status:", value = if (file.isOffline) "Yes (Fully Available Offline)" else "No")
                }
            },
            confirmButton = {
                TextButton(onClick = { fileDetailsToShow = null }) { Text("Ok") }
            }
        )
    }
}

@Composable
fun UploadSimCard(name: String, type: String, size: Long, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(getFileTypeColor(type).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = getFileTypeColor(type), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "$type • ${formatSize(size)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
fun FolderItemRow(folder: DbFolder, onNavigate: () -> Unit, onOptions: () -> Unit) {
    Card(
        onClick = onNavigate,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                tint = SapphireBlue,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Directory Folder",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onOptions) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Folder Settings")
            }
        }
    }
}

@Composable
fun FileItemRow(file: DbFile, onClick: () -> Unit, onOptions: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(getFileTypeColor(file.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFileTypeIcon(file.type),
                    contentDescription = null,
                    tint = getFileTypeColor(file.type)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = file.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (file.isFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Filled.Star, contentDescription = "Favorite", tint = SafeAmber, modifier = Modifier.size(14.dp))
                    }
                    if (file.isOffline) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Filled.CloudOff, contentDescription = "Offline Cache", tint = CyberTeal, modifier = Modifier.size(14.dp))
                    }
                }
                Text(
                    text = "${file.type} • ${formatSize(file.sizeBytes)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onOptions) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "File Settings")
            }
        }
    }
}

@Composable
fun ActiveUploadItemRow(
    file: DbFile,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    val isPaused = file.uploadStatus == "PAUSED"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    progress = { file.uploadProgress / 100f },
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = SapphireBlue
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = file.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = if (isPaused) "Paused (${file.uploadProgress}%)" else "Uploading (${file.uploadProgress}%)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = if (isPaused) onResume else onPause) {
                    Icon(
                        imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = "Pause Resume",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onCancel) {
                    Icon(imageVector = Icons.Filled.Cancel, contentDescription = "Cancel", tint = SoftRed, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { file.uploadProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SapphireBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// Helper types mapping
fun getFileTypeIcon(type: String): ImageVector {
    return when (type) {
        "PDF" -> Icons.Filled.PictureAsPdf
        "VIDEO" -> Icons.Filled.Movie
        "AUDIO" -> Icons.Filled.MusicNote
        "IMAGE" -> Icons.Filled.Image
        "ZIP" -> Icons.Filled.FolderZip
        "APK" -> Icons.Filled.Android
        else -> Icons.Filled.InsertDriveFile
    }
}

fun getFileTypeColor(type: String): Color {
    return when (type) {
        "PDF" -> SoftRed
        "VIDEO" -> CyberTeal
        "AUDIO" -> EmeraldGreen
        "IMAGE" -> SapphireBlue
        "ZIP" -> SafeAmber
        "APK" -> Color(0xFF3DDC84)
        else -> TextSecondary
    }
}

// --- ☁️ TAB: BACKUP SCREEN ---
@Composable
fun BackupScreenTab(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.userSession.collectAsStateWithLifecycle()
    val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()
    val backupProgress by viewModel.backupProgress.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Backup,
            contentDescription = null,
            tint = SapphireBlue,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Automated Backups",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Keep your telephone device fully cloned. Active selected categories will auto-sync to NexStorage secure databases in the background.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Live backup sync animation
        if (isBackingUp) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Syncing folders to cloud databases...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { backupProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Upload Progress: $backupProgress%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Toggles List
        BackupToggleItem(
            title = "Photos Backup",
            description = "Camera rolls and screenshot directories.",
            enabled = session?.autoBackupPhotos ?: false,
            onToggle = { viewModel.toggleBackupCategory("PHOTOS", it) },
            icon = Icons.Filled.PhotoLibrary
        )

        Spacer(modifier = Modifier.height(12.dp))

        BackupToggleItem(
            title = "Videos Backup",
            description = "Recorded video directories.",
            enabled = session?.autoBackupVideos ?: false,
            onToggle = { viewModel.toggleBackupCategory("VIDEOS", it) },
            icon = Icons.Filled.VideoCameraBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        BackupToggleItem(
            title = "Contacts List Backup",
            description = "VCF directory contact files.",
            enabled = session?.autoBackupContacts ?: false,
            onToggle = { viewModel.toggleBackupCategory("CONTACTS", it) },
            icon = Icons.Filled.Contacts
        )

        Spacer(modifier = Modifier.height(12.dp))

        BackupToggleItem(
            title = "Documents & PDFs Backup",
            description = "Download catalogs and offline PDFs.",
            enabled = session?.autoBackupDocuments ?: false,
            onToggle = { viewModel.toggleBackupCategory("DOCUMENTS", it) },
            icon = Icons.Filled.SnippetFolder
        )
    }
}

@Composable
fun BackupToggleItem(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SapphireBlue,
                    checkedTrackColor = SapphireBlue.copy(alpha = 0.3f)
                )
            )
        }
    }
}

// --- 🔗 TAB: SHARED LINKS MANAGEMENT ---
@Composable
fun SharedScreenTab(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val allFilesList by viewModel.allFiles.collectAsStateWithLifecycle()
    val sharedFiles = remember(allFilesList) {
        allFilesList.filter { it.sharedLink != null && !it.isDeleted }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.Link, contentDescription = null, tint = EmeraldGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Shared File Links",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Below are the active cryptographic paths open to the public web from your storage space.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (sharedFiles.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No active sharing links generated yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(sharedFiles) { file ->
                    SharedItemCard(
                        file = file,
                        onRevoke = { viewModel.removeShareLink(file) }
                    )
                }
            }
        }
    }
}

@Composable
fun SharedItemCard(file: DbFile, onRevoke: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(getFileTypeColor(file.type).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = getFileTypeIcon(file.type), contentDescription = null, tint = getFileTypeColor(file.type), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "${file.type} • ${formatSize(file.sizeBytes)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = file.sharedLink ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(file.sharedLink ?: ""))
                        Toast.makeText(context, "Copied shared URL!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
            )

            if (file.sharedPassword != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔐 Password Protected: ${file.sharedPassword}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeAmber
                )
            }

            if (file.sharedExpiry != null) {
                val rem = file.sharedExpiry - System.currentTimeMillis()
                val daysRem = (rem / 86400000).coerceAtLeast(0)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⏰ Expiration: Expires in $daysRem days",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRevoke,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Revoke Secure Link Access", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

// --- 👤 TAB: USER PROFILE & SETTINGS ---
@Composable
fun ProfileScreenTab(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.userSession.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var inputPinCode by remember { mutableStateOf("") }

    var showSecureVaultView by remember { mutableStateOf(false) }
    var vaultPinInput by remember { mutableStateOf("") }
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val vaultFilesList by viewModel.vaultFiles.collectAsStateWithLifecycle()

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Card Profile Overview
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SapphireBlue, CyberTeal)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = session?.email ?: "Guest mode activated",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Subscription level: ${session?.storagePlan ?: "Free"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Secure Vault Category Row (PIN Protected hidden directories)
        Text(text = "Militarized Secure Vault", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
        Card(
            onClick = { showSecureVaultView = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SafeAmber.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SafeAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = SafeAmber)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Access Encrypted Secure Vault", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Protected with AES-256 local keys.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SafeAmber)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Application Settings list
        Text(text = "App Configurations", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // PIN Code Lock toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Pattern, contentDescription = null, tint = SapphireBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "App Lock PIN Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = if (session?.pinCode != null) "Active PIN: ${session?.pinCode}" else "Disabled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = session?.pinCode != null,
                        onCheckedChange = {
                            if (it) {
                                showPinDialog = true
                            } else {
                                viewModel.setPinCode(null)
                            }
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Language selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = SapphireBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "App Language", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = session?.appLanguage ?: "English", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Box {
                        var showLangMenu by remember { mutableStateOf(false) }
                        TextButton(onClick = { showLangMenu = true }) {
                            Text(session?.appLanguage ?: "English")
                        }
                        DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
                            DropdownMenuItem(text = { Text("English") }, onClick = { viewModel.updateLanguage("English"); showLangMenu = false })
                            DropdownMenuItem(text = { Text("Spanish") }, onClick = { viewModel.updateLanguage("Spanish"); showLangMenu = false })
                            DropdownMenuItem(text = { Text("Bengali") }, onClick = { viewModel.updateLanguage("Bengali"); showLangMenu = false })
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Cache Clear button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.clearCache()
                            Toast.makeText(context, "Storage Cache Cleared!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.CleaningServices, contentDescription = null, tint = SapphireBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Clear Local Storage Cache", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Free up device RAM cache.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Admin Dashboard access
        Text(text = "Storage Management Panel", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
        Card(
            onClick = { viewModel.changeTab("ADMIN") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CyberTeal.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.QueryStats, contentDescription = null, tint = CyberTeal)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Launch Administration Console", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Read live subscriber analytics data.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = CyberTeal)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout Action
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Logout from NexStorage", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // PIN Settings Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Lock PIN") },
            text = {
                OutlinedTextField(
                    value = inputPinCode,
                    onValueChange = { if (it.length <= 4) inputPinCode = it },
                    label = { Text("PIN Code (Numbers)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("1234") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (inputPinCode.length == 4) {
                        viewModel.setPinCode(inputPinCode)
                        inputPinCode = ""
                        showPinDialog = false
                    }
                }) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false; inputPinCode = "" }) { Text("Cancel") }
            }
        )
    }

    // SECURE VAULT DIALOG FLOW
    if (showSecureVaultView) {
        AlertDialog(
            onDismissRequest = { showSecureVaultView = false; viewModel.lockVault() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = SafeAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AES-256 Vault")
                }
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    if (!isVaultUnlocked) {
                        SecurePinLockScreen(
                            title = "Enter PIN to Unlock Secure Vault",
                            errorMsg = viewModel.wrongPinError.collectAsStateWithLifecycle().value,
                            onPinEntered = { pin ->
                                viewModel.unlockVault(pin)
                            },
                            modifier = Modifier.height(300.dp)
                        )
                    } else {
                        // Vault List files!
                        Text(
                            text = "Secure Vault Files list:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (vaultFilesList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No vault files locked inside yet.\n(Move files to vault from the directory files menu).",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(260.dp)
                            ) {
                                items(vaultFilesList) { file ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = getFileTypeIcon(file.type), contentDescription = null, tint = SafeAmber)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(text = formatSize(file.sizeBytes), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { viewModel.toggleVaultFile(file, false) }) {
                                            Icon(imageVector = Icons.Filled.LockOpen, contentDescription = "Unlock File", tint = SafeAmber)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSecureVaultView = false; viewModel.lockVault() }) { Text("Close Vault") }
            }
        )
    }
}

// --- ADMIN PANELS TELEMETRY VIEW SCREEN ---
@Composable
fun AdminDashboardScreen(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val totalFiles by viewModel.allFiles.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.changeTab("PROFILE") }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Administration Console",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Overall Server metrics rows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminMetricCard(title = "Total Active Users", value = "15,482", icon = Icons.Filled.People, color = SapphireBlue, modifier = Modifier.weight(1f))
            AdminMetricCard(title = "Total Storage Consumed", value = "43.2 TB", icon = Icons.Filled.QueryStats, color = CyberTeal, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminMetricCard(title = "Upload Requests", value = "102,482", icon = Icons.Filled.CloudUpload, color = EmeraldGreen, modifier = Modifier.weight(1f))
            AdminMetricCard(title = "Download Requests", value = "340,920", icon = Icons.Filled.CloudDownload, color = SafeAmber, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subscription Share list progress bars
        Text(text = "Subscriber Distributions", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SubscriptionProgressRow(label = "Free Tiers (10 GB)", value = 0.65f, valueText = "65%")
                SubscriptionProgressRow(label = "Premium Packages (256 GB)", value = 0.25f, valueText = "25%")
                SubscriptionProgressRow(label = "Ultimate Packages (1 TB)", value = 0.10f, valueText = "10%")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Support Report tickets
        Text(text = "Active Server Logs & Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ServerLogTicket(tag = "OK", service = "AWS Backup Sync", desc = "All automated backlogs synchronized successfully with 0 lost packages.")
                ServerLogTicket(tag = "INFO", service = "AES-256 Vault Router", desc = "Pin Lock authorization successfully requested for user guest_045.")
                ServerLogTicket(tag = "WARN", service = "Duplicate Finder Engine", desc = "System isolated high load file indexing in directories /work/tax_reports.")
            }
        }
    }
}

@Composable
fun AdminMetricCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SubscriptionProgressRow(label: String, value: Float, valueText: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = valueText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { value },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = SapphireBlue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ServerLogTicket(tag: String, service: String, desc: String) {
    val tagColor = when (tag) {
        "OK" -> EmeraldGreen
        "WARN" -> SafeAmber
        else -> SapphireBlue
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(tagColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = tag, color = tagColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = service, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --- OPTIMIZATION: RECYCLE BIN SCREEN ---
@Composable
fun RecycleBinScreen(
    viewModel: StorageViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val binFiles by viewModel.recycleBinFiles.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.changeTab("HOME") }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recycle Bin",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.weight(1f))

            if (binFiles.isNotEmpty()) {
                TextButton(
                    onClick = {
                        viewModel.clearRecycleBin()
                        scope.launch { snackbarHostState.showSnackbar("Recycle bin fully emptied permanently.") }
                    }
                ) {
                    Text("Empty Bin", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Files placed in the Recycle Bin will be automatically permanently deleted after 30 days.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (binFiles.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Recycle bin is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(binFiles) { file ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(getFileTypeColor(file.type).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = getFileTypeIcon(file.type), contentDescription = null, tint = getFileTypeColor(file.type), modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "Size: ${formatSize(file.sizeBytes)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Restore Action Button
                            IconButton(onClick = { viewModel.restoreFile(file) }) {
                                Icon(imageVector = Icons.Filled.Restore, contentDescription = "Restore File", tint = EmeraldGreen)
                            }

                            // Permanent Delete Action Button
                            IconButton(onClick = { viewModel.deleteFilePermanently(file) }) {
                                Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = "Delete Permanently", tint = SoftRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- OPTIMIZATION: DUPLICATE FINDER SCREEN ---
@Composable
fun DuplicateFinderScreen(
    viewModel: StorageViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val duplicatesList = viewModel.getDuplicateFiles()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.changeTab("HOME") }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Duplicate File Finder",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "NexStorage scanned your directories and isolated files having exactly identical filenames and byte sizes.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (duplicatesList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Awesome! No duplicate files found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(duplicatesList) { file ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(getFileTypeColor(file.type).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = getFileTypeIcon(file.type), contentDescription = null, tint = getFileTypeColor(file.type), modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "Path: folder_${file.folderId} • Size: ${formatSize(file.sizeBytes)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = {
                                    viewModel.recycleFile(file)
                                    scope.launch { snackbarHostState.showSnackbar("Duplicate file moved to Recycle Bin.") }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Remove", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- OPTIMIZATION: LARGE FILES SCREEN ---
@Composable
fun LargeFileFinderScreen(
    viewModel: StorageViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val largeFilesList = viewModel.getLargeFiles()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.changeTab("HOME") }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Large File Finder",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The following files are taking up the most space on your storage drive (files larger than 10 MB).",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (largeFilesList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No large files indexed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(largeFilesList) { file ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(getFileTypeColor(file.type).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = getFileTypeIcon(file.type), contentDescription = null, tint = getFileTypeColor(file.type), modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "${file.type} • ${formatSize(file.sizeBytes)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = {
                                    viewModel.recycleFile(file)
                                    scope.launch { snackbarHostState.showSnackbar("Large file sent to Recycle Bin.") }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
