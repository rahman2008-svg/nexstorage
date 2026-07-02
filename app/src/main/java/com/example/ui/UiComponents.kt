package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DbFile
import com.example.data.DbFolder
import com.example.ui.theme.*
import kotlin.math.roundToInt

// --- Storage Gauge Component ---
@Composable
fun StorageGauge(
    usedBytes: Long,
    totalBytes: Long,
    modifier: Modifier = Modifier
) {
    val usedGB = usedBytes / (1024.0 * 1024.0 * 1024.0)
    val totalGB = totalBytes / (1024.0 * 1024.0 * 1024.0)
    val percent = (usedGB / totalGB).coerceIn(0.0, 1.0)

    val imageBytes = (usedBytes * 0.45).toLong()
    val videoBytes = (usedBytes * 0.33).toLong()
    val documentBytes = (usedBytes * 0.15).toLong()
    val otherBytes = (usedBytes - (imageBytes + videoBytes + documentBytes)).coerceAtLeast(0L)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "STORAGE USAGE",
                        color = SleekTextSlate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f GB", usedGB),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " / ${totalGB.roundToInt()} GB",
                            color = SleekTextSlate400,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                        )
                    }
                }
                
                // Premium / Plan badge styled exactly like the Sleek HTML
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SleekBlue50)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (totalGB > 50) "PREMIUM" else "FREE",
                        color = SleekBlue600,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Beautiful rounded track progress bar matching the Sleek design with soft glow styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(LightBorderSoft)
            ) {
                // Outer progress track
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percent.toFloat())
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(SleekBlue500)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legends Grid matching the Sleek design colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(color = SleekBlue500, label = "Media", sizeText = formatSize(imageBytes + videoBytes))
                LegendItem(color = SleekBlue100, label = "Docs", sizeText = formatSize(documentBytes))
                LegendItem(color = LightBorder, label = "Other", sizeText = formatSize(otherBytes))
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, sizeText: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = sizeText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

// --- Custom QR Code Generator on Canvas ---
@Composable
fun QrCodeCanvas(link: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val sizeX = size.width
            val sizeY = size.height
            val blocks = 13
            val blockW = sizeX / blocks
            val blockH = sizeY / blocks

            // Custom hash to pseudo-randomize block generation based on the share link
            val hash = link.hashCode()

            for (i in 0 until blocks) {
                for (j in 0 until blocks) {
                    val isBorderModule = (i < 4 && j < 4) || (i >= blocks - 4 && j < 4) || (i < 4 && j >= blocks - 4)
                    val isInnerModule = (i in 1..2 && j in 1..2) || (i >= blocks - 3 && i < blocks - 1 && j in 1..2) || (i in 1..2 && j >= blocks - 3 && j < blocks - 1)
                    
                    val drawBlock = when {
                        isBorderModule -> !isInnerModule
                        isInnerModule -> true
                        else -> ((hash + i * 31 + j * 17) % 3 == 0) || ((i + j) % 5 == 0)
                    }

                    if (drawBlock) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(i * blockW, j * blockH),
                            size = Size(blockW + 0.5f, blockH + 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// --- Built-in Music Player Modal Overlay ---
@Composable
fun SimulatedMusicPlayer(
    file: DbFile,
    isPlaying: Boolean,
    onPlayPauseToggle: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableStateOf(0.35f) }
    var currentSec by remember { mutableStateOf(44) }
    val totalSec = 215 // 3:35

    val infiniteTransition = rememberInfiniteTransition(label = "Music Rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Music Rotation Angle"
    )

    // Animated sound wave bars
    val waveHeights = remember { List(12) { mutableStateOf(10f) } }
    if (isPlaying) {
        waveHeights.forEachIndexed { index, state ->
            val height by infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = (30..80).random().toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = (300..600).random(), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "WaveHeight_$index"
            )
            state.value = height
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (currentSec < totalSec) {
                currentSec++
                progress = currentSec.toFloat() / totalSec.toFloat()
            } else {
                currentSec = 0
                progress = 0f
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "Playing Audio",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Built-in Audio Player",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close Player")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vinyl Record Art with Rotating Accent
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                // Outer Vinyl Grooves
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = Color(0xFF1E293B), radius = size.minDimension / 2f)
                    drawCircle(color = Color.Black, radius = size.minDimension / 2.3f)
                    drawCircle(color = Color(0xFF334155), radius = size.minDimension / 2.7f)
                }

                // Rotating Center Art Card
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(if (isPlaying) rotationAngle else 0f)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SapphireBlue, CyberTeal)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = file.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Simulated Audio Playback • ${formatSize(file.sizeBytes)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Audio Wave
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                waveHeights.forEach { waveState ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .width(6.dp)
                            .height(waveState.value.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(SapphireBlue, CyberTeal)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slider timeline
            Slider(
                value = progress,
                onValueChange = {
                    progress = it
                    currentSec = (it * totalSec).toInt()
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%d:%02d", currentSec / 60, currentSec % 60),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%d:%02d", totalSec / 60, totalSec % 60),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Player Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp))
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onPlayPauseToggle(!isPlaying) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play Pause",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

// --- Built-in Video Player Modal ---
@Composable
fun SimulatedVideoPlayer(
    file: DbFile,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentSec by remember { mutableStateOf(12) }
    val totalSec = 145

    val infiniteTransition = rememberInfiniteTransition(label = "Video Frame animation")
    val frameLight by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Video Frame Light"
    )

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (currentSec < totalSec) {
                currentSec++
            } else {
                currentSec = 0
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Built-in Video Player",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close Player", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated Media viewport Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070A0F)),
                contentAlignment = Alignment.Center
            ) {
                // Flickering grid overlay to simulate actual video frames
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sizeW = size.width
                    val sizeH = size.height

                    // Draw radial ambient video glow representing the scene
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(SapphireBlue.copy(alpha = 0.4f * frameLight), Color.Transparent),
                            center = Offset(sizeW / 2, sizeH / 2),
                            radius = sizeH * 0.9f
                        ),
                        radius = sizeH * 0.9f
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.MovieFilter else Icons.Filled.PlayCircleFilled,
                        contentDescription = null,
                        tint = CyberTeal,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Rendering Frame: $currentSec / $totalSec",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = file.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Timeline Slider
            LinearProgressIndicator(
                progress = { currentSec.toFloat() / totalSec.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SapphireBlue,
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%d:%02d", currentSec / 60, currentSec % 60),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = String.format("%d:%02d", totalSec / 60, totalSec % 60),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Play controllers
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentSec = (currentSec - 10).coerceAtLeast(0) }) {
                    Icon(imageVector = Icons.Filled.Replay10, contentDescription = "-10s", tint = Color.White)
                }
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(48.dp)
                        .background(SapphireBlue, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { currentSec = (currentSec + 10).coerceAtMost(totalSec) }) {
                    Icon(imageVector = Icons.Filled.Forward10, contentDescription = "+10s", tint = Color.White)
                }
            }
        }
    }
}

// --- Built-in PDF Viewer Modal ---
@Composable
fun SimulatedPdfViewer(
    file: DbFile,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(1) }
    val totalPages = 5

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Built-in Document Viewer",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close PDF")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated PDF Sheet Page Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = file.name,
                            color = Color(0xFF1E293B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Page $currentPage of $totalPages",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))

                    Text(
                        text = "DOCUMENT BODY SAMPLE (SIMULATION MODE)",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when (currentPage) {
                        1 -> {
                            Text(
                                text = "1. EXECUTIVE OVERVIEW & CONTEXT\n\n" +
                                        "This document defines the storage requirements for the NexStorage platform. " +
                                        "The platform is designed to incorporate fully redundant cloud files with local backups, " +
                                        "as well as a militarized security vault container locked with a user-defined numeric code.",
                                color = Color(0xFF334155),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                        2 -> {
                            Text(
                                text = "2. FILE TRANSFER PERFORMANCE TARGETS\n\n" +
                                        "File upload and download streams must reflect active, non-blocking asynchronous cycles. " +
                                        "Users must be able to visually pause or cancel active operations dynamically. " +
                                        "Storage quotas are fully configured across Free (10GB), Premium (256GB), and Ultimate (1TB) plans.",
                                color = Color(0xFF334155),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                        3 -> {
                            Text(
                                text = "3. SYSTEM REVISION HISTORY\n\n" +
                                        "• Version 1.0.0 - Full UI wireframes complete.\n" +
                                        "• Version 1.1.0 - SQLite Room architecture deployed successfully.\n" +
                                        "• Version 1.2.0 - Simulated background upload engine fully configured.",
                                color = Color(0xFF334155),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                        4 -> {
                            Text(
                                text = "4. DATA RETENTION POLICY\n\n" +
                                        "Files placed inside the Recycle Bin will be automatically purged permanently after 30 days. " +
                                        "The platform provides an on-demand Duplicate Finder to allow users to clear space easily. " +
                                        "Large file detectors isolate individual records exceeding 10 MB.",
                                color = Color(0xFF334155),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                        5 -> {
                            Text(
                                text = "5. CONCLUSION & PLATFORM GLOSSARY\n\n" +
                                        "All-in-one file management guarantees robust privacy. " +
                                        "Thank you for selecting NexStorage as your secure database of record.\n\n" +
                                        "End of Document.",
                                color = Color(0xFF334155),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PDF pagination controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (currentPage > 1) currentPage-- },
                    enabled = currentPage > 1,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Previous Page")
                }

                Text(
                    text = "Page $currentPage / $totalPages",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = { if (currentPage < totalPages) currentPage++ },
                    enabled = currentPage < totalPages,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Next Page")
                }
            }
        }
    }
}

// --- Dynamic File Size Formatting ---
fun formatSize(sizeInBytes: Long): String {
    val kb = sizeInBytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.0f KB", kb)
        else -> "$sizeInBytes B"
    }
}
