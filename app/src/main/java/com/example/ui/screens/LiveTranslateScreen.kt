package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.util.ProjectZipUtil
import com.example.viewmodel.TrillAiViewModel

val SUPPORTED_LANGUAGES = listOf(
    "English", "Spanish", "French", "German", "Japanese",
    "Chinese", "Russian", "Italian", "Portuguese", "Arabic",
    "Hindi", "Korean", "Dutch", "Turkish", "Polish"
)

@Composable
fun LiveTranslateScreen(
    viewModel: TrillAiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isListening by viewModel.isLiveTranslating.collectAsState()
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val liveSourceText by viewModel.liveSourceTranscript.collectAsState()
    val liveTargetText by viewModel.liveTargetTranscript.collectAsState()
    val recentTranslations by viewModel.recentTranslations.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.toggleLiveTranslation(context)
        }
    }

    // Audio Visualizer Wave Animation
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 38f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 46f,
        animationSpec = infiniteRepeatable(tween(350, delayMillis = 100, easing = LinearEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val waveHeight3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 42f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 50, easing = LinearEasing), RepeatMode.Reverse),
        label = "w3"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Surface(color = DarkSurface, tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live Transcription & Translation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Continuous Background Intelligence",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary
                        )
                    }

                    // Background Service Active Badge
                    if (isListening) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldTertiary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTertiary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldTertiary))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Active 24/7", fontSize = 11.sp, color = EmeraldTertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Language Selectors (Source ➔ Target)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FROM", fontSize = 10.sp, color = TextMutedDark, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SUPPORTED_LANGUAGES.forEach { lang ->
                                FilterChip(
                                    selected = lang == sourceLang,
                                    onClick = { viewModel.setSourceLang(lang) },
                                    label = { Text(lang, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = CyanPrimary
                                    )
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val temp = sourceLang
                            viewModel.setSourceLang(targetLang)
                            viewModel.setTargetLang(temp)
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Languages",
                            tint = CyanPrimary
                        )
                    }

                    // Target
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TO", fontSize = 10.sp, color = TextMutedDark, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SUPPORTED_LANGUAGES.forEach { lang ->
                                FilterChip(
                                    selected = lang == targetLang,
                                    onClick = { viewModel.setTargetLang(lang) },
                                    label = { Text(lang, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VioletSecondary.copy(alpha = 0.2f),
                                        selectedLabelColor = VioletSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Audio Stream Dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.4f), VioletSecondary.copy(alpha = 0.4f))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Waveform or Idle Indicator
                if (isListening) {
                    Row(
                        modifier = Modifier.height(50.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(waveHeight1, waveHeight2, waveHeight3, waveHeight2, waveHeight1, waveHeight3, waveHeight2).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(h.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.verticalGradient(listOf(CyanPrimary, VioletSecondary))
                                    )
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.MicNone,
                        contentDescription = "Mic idle",
                        tint = TextMutedDark,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isListening) "Continuous Live Stream Active" else "Ready for Uncapped Live Speech Translation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isListening) CyanPrimary else Color.White
                )

                Text(
                    text = if (isListening) "Translates continuously in foreground & background without limits" else "Tap start to activate 24/7 microphone audio decoding",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start / Stop Master Button
                Button(
                    onClick = {
                        if (!hasAudioPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            viewModel.toggleLiveTranslation(context)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) Color(0xFFD32F2F) else CyanPrimary,
                        contentColor = if (isListening) Color.White else Color(0xFF00363D)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .testTag("toggle_live_translate_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isListening) "Stop Continuous Translation" else "Start Live Translation",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Dual Transcript Box
        if (liveSourceText.isNotEmpty() || liveTargetText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE TRANSCRIPT ($sourceLang):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Text(
                        text = liveSourceText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = DarkSurfaceBorder)

                    Text(
                        text = "REAL-TIME TRANSLATION ($targetLang):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldTertiary
                    )
                    Text(
                        text = liveTargetText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recent Translation Records
        Text(
            text = "Translation History & Memory",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentTranslations, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${log.sourceLang} ➔ ${log.targetLang}",
                                fontSize = 11.sp,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("${log.originalText}\n${log.translatedText}"))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMutedDark, modifier = Modifier.size(14.dp))
                                }
                                IconButton(
                                    onClick = {
                                        ProjectZipUtil.shareText(
                                            context,
                                            "Translation (${log.sourceLang} to ${log.targetLang})",
                                            "${log.originalText}\n\n➔ ${log.translatedText}"
                                        )
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = TextMutedDark, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Text(text = log.originalText, style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = log.translatedText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color.White)
                    }
                }
            }
        }
    }
}
