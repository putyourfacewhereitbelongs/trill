package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettingsManager
import com.example.ui.theme.*
import com.example.viewmodel.TrillAiViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TrillAiViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val settings = viewModel.settingsManager

    // Settings States
    val serverUrl by settings.serverUrl.collectAsState()
    val modelName by settings.modelName.collectAsState()
    val systemPrompt by settings.systemPrompt.collectAsState()
    val temperature by settings.temperature.collectAsState()
    val topP by settings.topP.collectAsState()
    val maxTokens by settings.maxTokens.collectAsState()
    val streamEnabled by settings.streamEnabled.collectAsState()

    val chatFontSize by settings.chatFontSize.collectAsState()
    val autoScroll by settings.autoScroll.collectAsState()
    val showPromptSuggestions by settings.showPromptSuggestions.collectAsState()
    val hapticFeedback by settings.hapticFeedback.collectAsState()

    val voiceLanguage by settings.voiceLanguage.collectAsState()
    val ttsSpeed by settings.ttsSpeed.collectAsState()
    val ttsPitch by settings.ttsPitch.collectAsState()
    val ttsAutoPlay by settings.ttsAutoPlay.collectAsState()

    val codeFontSize by settings.codeFontSize.collectAsState()
    val codeLineNumbers by settings.codeLineNumbers.collectAsState()

    // DB stats
    val sessions by viewModel.sessions.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val codeProjects by viewModel.codeProjects.collectAsState()
    val learnedPatterns by viewModel.learnedPatterns.collectAsState()

    // Local editable fields
    var localServerUrl by remember(serverUrl) { mutableStateOf(serverUrl) }
    var localSystemPrompt by remember(systemPrompt) { mutableStateOf(systemPrompt) }

    // Test connection state
    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionTestResult by remember { mutableStateOf<String?>(null) }
    var connectionSuccess by remember { mutableStateOf(false) }

    // Dialog states
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetDefaultsDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All Chat History?", color = MinimalTextPrimary) },
            text = {
                Text(
                    "This will delete all saved sessions and chat messages. This cannot be undone.",
                    color = MinimalTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllChatHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "All chat history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = MinimalTextSecondary)
                }
            },
            containerColor = MinimalSurfaceElevated
        )
    }

    if (showResetDefaultsDialog) {
        AlertDialog(
            onDismissRequest = { showResetDefaultsDialog = false },
            title = { Text("Reset All Settings?", color = MinimalTextPrimary) },
            text = {
                Text(
                    "This will restore all server, model, voice, font, and UI settings back to factory defaults.",
                    color = MinimalTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllSettings()
                        localServerUrl = AppSettingsManager.DEFAULT_SERVER_URL
                        localSystemPrompt = AppSettingsManager.DEFAULT_SYSTEM_PROMPT
                        showResetDefaultsDialog = false
                        Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalPurplePrimary)
                ) {
                    Text("Reset Defaults", color = MinimalPurpleOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDefaultsDialog = false }) {
                    Text("Cancel", color = MinimalTextSecondary)
                }
            },
            containerColor = MinimalSurfaceElevated
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings & Customization",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextPrimary
                        )
                        Text(
                            text = "Trill AI • Full Control",
                            fontSize = 11.sp,
                            color = MinimalPurplePrimary
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MinimalTextPrimary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showResetDefaultsDialog = true },
                        modifier = Modifier.testTag("reset_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset to Defaults",
                            tint = MinimalTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MinimalSurface,
                    titleContentColor = MinimalTextPrimary
                )
            )
        },
        containerColor = MinimalBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Section 1: AI Inference Server & Model
            item {
                SettingsSectionCard(
                    title = "AI Inference & Model",
                    icon = Icons.Default.Dns,
                    description = "Configure remote server endpoints, latency, and LLM parameters"
                ) {
                    // Server URL Input
                    Text(
                        text = "Server URL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = localServerUrl,
                        onValueChange = {
                            localServerUrl = it
                            settings.setServerUrl(it)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_server_url_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalPurplePrimary,
                            unfocusedBorderColor = MinimalSurfaceBorder,
                            focusedContainerColor = MinimalSurface,
                            unfocusedContainerColor = MinimalSurface,
                            focusedTextColor = MinimalTextPrimary,
                            unfocusedTextColor = MinimalTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ping Test & Reset Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                isTestingConnection = true
                                connectionTestResult = null
                                viewModel.testServerConnection(localServerUrl) { success, msg ->
                                    isTestingConnection = false
                                    connectionSuccess = success
                                    connectionTestResult = msg
                                }
                            },
                            enabled = !isTestingConnection,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MinimalContainer,
                                contentColor = MinimalOnContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("test_server_button")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MinimalPurplePrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Server Ping", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                localServerUrl = AppSettingsManager.DEFAULT_SERVER_URL
                                settings.setServerUrl(AppSettingsManager.DEFAULT_SERVER_URL)
                                Toast.makeText(context, "URL reset Server", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder)
                        ) {
                            Text("Reset URL", fontSize = 12.sp)
                        }
                    }

                    // Connection Result Badge
                    AnimatedVisibility(visible = connectionTestResult != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (connectionSuccess) MinimalSuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (connectionSuccess) MinimalSuccessGreen else MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (connectionSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (connectionSuccess) MinimalSuccessGreen else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = connectionTestResult ?: "",
                                    fontSize = 12.sp,
                                    color = MinimalTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Model Selection
                    Text(
                        text = "Active AI Model",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val modelOptions = listOf(
                        "default" to "Trill AI Turbo (Fast)",
                        "trill-deep-think" to "Deep Think (Reasoning)",
                        "deepseek-r1" to "DeepSeek R1",
                        "gemini-2.5-flash" to "Gemini 2.5 Flash",
                        "llama-3.3-70b" to "Llama 3.3 70B",
                        "qwen-2.5-coder" to "Qwen 2.5 Coder"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(modelOptions) { (key, label) ->
                            val isSelected = modelName == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { settings.setModelName(key) },
                                label = { Text(label, fontSize = 11.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MinimalPurplePrimary,
                                    selectedLabelColor = MinimalPurpleOnPrimary,
                                    containerColor = MinimalSurface,
                                    labelColor = MinimalTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) MinimalPurplePrimary else MinimalSurfaceBorder
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Temperature Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Temperature (Creativity)", fontSize = 12.sp, color = MinimalTextSecondary)
                        Text(
                            String.format(java.util.Locale.US, "%.2f", temperature) +
                                    if (temperature < 0.4f) " (Precise)" else if (temperature > 1.0f) " (Unrestricted)" else " (Balanced)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalPurplePrimary
                        )
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { settings.setTemperature(it) },
                        valueRange = 0.0f..1.5f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = MinimalPurplePrimary,
                            activeTrackColor = MinimalPurplePrimary,
                            inactiveTrackColor = MinimalContainer
                        )
                    )

                    // Top-P Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Top-P (Nucleus Sampling)", fontSize = 12.sp, color = MinimalTextSecondary)
                        Text(
                            String.format(java.util.Locale.US, "%.2f", topP),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalPurplePrimary
                        )
                    }
                    Slider(
                        value = topP,
                        onValueChange = { settings.setTopP(it) },
                        valueRange = 0.1f..1.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MinimalPurplePrimary,
                            activeTrackColor = MinimalPurplePrimary,
                            inactiveTrackColor = MinimalContainer
                        )
                    )

                    // Max Tokens Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Max Output Tokens", fontSize = 12.sp, color = MinimalTextSecondary)
                        Text("$maxTokens tokens", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalPurplePrimary)
                    }
                    Slider(
                        value = maxTokens.toFloat(),
                        onValueChange = { settings.setMaxTokens(it.roundToInt()) },
                        valueRange = 512f..8192f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = MinimalPurplePrimary,
                            activeTrackColor = MinimalPurplePrimary,
                            inactiveTrackColor = MinimalContainer
                        )
                    )

                    // Streaming Toggle
                    SettingsSwitchRow(
                        title = "Real-Time Streaming (SSE)",
                        subtitle = "Streams tokens live as generated instead of waiting for full response",
                        checked = streamEnabled,
                        onCheckedChange = { settings.setStreamEnabled(it) }
                    )
                }
            }

            // Section 2: System Persona & Instructions
            item {
                SettingsSectionCard(
                    title = "System Persona & Instructions",
                    icon = Icons.Default.Psychology,
                    description = "Define Trill AI's autonomous persona, tone, and guidance"
                ) {
                    Text(
                        text = "Persona Presets",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val presets = listOf(
                        "Default" to AppSettingsManager.DEFAULT_SYSTEM_PROMPT,
                        "Code Architect" to "You are Trill AI, an expert polyglot software engineer. Provide clean, secure, production-grade architecture without boilerplate.",
                        "Academic Expert" to "You are Trill AI, an objective academic scholar. Answer questions with rigorous factual honesty, intellectual depth, and zero bias or speculative distortion.",
                        "Concise & Direct" to "You are Trill AI. Answer questions directly with bulleted clarity, extreme precision, and zero conversational filler."
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { (title, promptText) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MinimalSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
                                modifier = Modifier.clickable {
                                    localSystemPrompt = promptText
                                    settings.setSystemPrompt(promptText)
                                    Toast.makeText(context, "Loaded \"$title\" preset", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    color = MinimalTextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = localSystemPrompt,
                        onValueChange = {
                            localSystemPrompt = it
                            settings.setSystemPrompt(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp)
                            .testTag("settings_system_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalPurplePrimary,
                            unfocusedBorderColor = MinimalSurfaceBorder,
                            focusedContainerColor = MinimalSurface,
                            unfocusedContainerColor = MinimalSurface,
                            focusedTextColor = MinimalTextPrimary,
                            unfocusedTextColor = MinimalTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            localSystemPrompt = AppSettingsManager.DEFAULT_SYSTEM_PROMPT
                            settings.setSystemPrompt(AppSettingsManager.DEFAULT_SYSTEM_PROMPT)
                            Toast.makeText(context, "Reset to default prompt", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder)
                    ) {
                        Text("Reset to Default Prompt", fontSize = 12.sp)
                    }
                }
            }

            // Section 3: Voice-to-Text & Speech Synthesis
            item {
                SettingsSectionCard(
                    title = "Voice-to-Text & Speech",
                    icon = Icons.Default.Mic,
                    description = "Configure speech recognition language, TTS pitch, and vocal speed"
                ) {
                    // Voice-to-Text Language Selection
                    Text(
                        text = "Voice-to-Text Speech Recognition Language",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val languages = listOf("Default", "English (US)", "Hindi (IN)", "Spanish (ES)", "French (FR)", "German (DE)", "Japanese (JP)", "Chinese (ZH)")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(languages) { lang ->
                            val isSelected = voiceLanguage == lang
                            FilterChip(
                                selected = isSelected,
                                onClick = { settings.setVoiceLanguage(lang) },
                                label = { Text(lang, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MinimalPurplePrimary,
                                    selectedLabelColor = MinimalPurpleOnPrimary,
                                    containerColor = MinimalSurface,
                                    labelColor = MinimalTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) MinimalPurplePrimary else MinimalSurfaceBorder
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TTS Speech Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TTS Voice Speed", fontSize = 12.sp, color = MinimalTextSecondary)
                        Text(String.format(java.util.Locale.US, "%.1fx", ttsSpeed), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalPurplePrimary)
                    }
                    Slider(
                        value = ttsSpeed,
                        onValueChange = { settings.setTtsSpeed(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = MinimalPurplePrimary,
                            activeTrackColor = MinimalPurplePrimary,
                            inactiveTrackColor = MinimalContainer
                        )
                    )

                    // TTS Voice Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TTS Voice Pitch", fontSize = 12.sp, color = MinimalTextSecondary)
                        Text(String.format(java.util.Locale.US, "%.1fx", ttsPitch), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalPurplePrimary)
                    }
                    Slider(
                        value = ttsPitch,
                        onValueChange = { settings.setTtsPitch(it) },
                        valueRange = 0.5f..1.5f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = MinimalPurplePrimary,
                            activeTrackColor = MinimalPurplePrimary,
                            inactiveTrackColor = MinimalContainer
                        )
                    )

                    // Test Voice button
                    Button(
                        onClick = {
                            viewModel.testTtsVoice("Hello, this is Trill AI voice synthesis.")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalContainer,
                            contentColor = MinimalOnContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Voice Speech Preview", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Auto-Play Toggle
                    SettingsSwitchRow(
                        title = "Auto-Play Responses with Speech",
                        subtitle = "Automatically reads assistant answers aloud when generated",
                        checked = ttsAutoPlay,
                        onCheckedChange = { settings.setTtsAutoPlay(it) }
                    )
                }
            }

            // Section 4: Chat Display & Typography
            item {
                SettingsSectionCard(
                    title = "Chat Display & Layout",
                    icon = Icons.Default.Tune,
                    description = "Adjust font sizing, suggestions visibility, and touch behavior"
                ) {
                    // Chat Font Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chat Message Font Size", fontSize = 12.sp, color = MinimalTextSecondary)
                        Text("${chatFontSize}sp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalPurplePrimary)
                    }
                    Slider(
                        value = chatFontSize.toFloat(),
                        onValueChange = { settings.setChatFontSize(it.roundToInt()) },
                        valueRange = 12f..20f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = MinimalPurplePrimary,
                            activeTrackColor = MinimalPurplePrimary,
                            inactiveTrackColor = MinimalContainer
                        )
                    )

                    // Font Size Live Preview
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MinimalSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Sample: The quick brown fox jumps over the lazy dog.",
                            fontSize = chatFontSize.sp,
                            color = MinimalTextPrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsSwitchRow(
                        title = "Show Prompt Suggestions Row",
                        subtitle = "Contextual quick chips above input bar for rapid inspiration",
                        checked = showPromptSuggestions,
                        onCheckedChange = { settings.setShowPromptSuggestions(it) }
                    )

                    SettingsSwitchRow(
                        title = "Auto-Scroll to Latest Response",
                        subtitle = "Automatically scrolls message history to the bottom as AI responds",
                        checked = autoScroll,
                        onCheckedChange = { settings.setAutoScroll(it) }
                    )

                    SettingsSwitchRow(
                        title = "Haptic Vibration Feedback",
                        subtitle = "Provide tactile feedback on buttons, voice start, and sends",
                        checked = hapticFeedback,
                        onCheckedChange = { settings.setHapticFeedback(it) }
                    )
                }
            }

            // Section 5: Code Studio Customization
            item {
                SettingsSectionCard(
                    title = "Code Studio Workspace",
                    icon = Icons.Default.Code,
                    description = "Syntax editor preferences, line numbering, and typography"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Code Editor Font Size", fontSize = 12.sp, color = MinimalTextSecondary)
                        Text("${codeFontSize}sp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalPurplePrimary)
                    }
                    Slider(
                        value = codeFontSize.toFloat(),
                        onValueChange = { settings.setCodeFontSize(it.roundToInt()) },
                        valueRange = 11f..18f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = MinimalPurplePrimary,
                            activeTrackColor = MinimalPurplePrimary,
                            inactiveTrackColor = MinimalContainer
                        )
                    )

                    SettingsSwitchRow(
                        title = "Show Code Line Numbers",
                        subtitle = "Displays gutter line numbering in the live code editor",
                        checked = codeLineNumbers,
                        onCheckedChange = { settings.setCodeLineNumbers(it) }
                    )
                }
            }

            // Section 6: Data & Storage Management
            item {
                SettingsSectionCard(
                    title = "Data & Storage Management",
                    icon = Icons.Default.Storage,
                    description = "Local Room database statistics and storage controls"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DataStatBadge(label = "Sessions", count = sessions.size)
                        DataStatBadge(label = "Messages", count = messages.size)
                        DataStatBadge(label = "Projects", count = codeProjects.size)
                        DataStatBadge(label = "Learned Vectors", count = learnedPatterns.size)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showClearHistoryDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("clear_history_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Chat Messages", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showResetDefaultsDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reset_all_defaults_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset All Customizations to Factory Defaults", fontSize = 12.sp)
                    }
                }
            }

            // Section 7: About & Creator
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MinimalSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MinimalContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MinimalPurplePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Trill AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Unrestricted Intelligence Architecture\nVoice-to-Text • Multimodal Vision • Polyglot Translation",
                            fontSize = 11.sp,
                            color = MinimalTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MinimalSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MinimalContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MinimalPurplePrimary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalTextPrimary
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = MinimalTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MinimalSurfaceBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MinimalTextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = MinimalTextMuted, lineHeight = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MinimalPurpleOnPrimary,
                checkedTrackColor = MinimalPurplePrimary,
                uncheckedThumbColor = MinimalTextSecondary,
                uncheckedTrackColor = MinimalContainer
            )
        )
    }
}

@Composable
private fun DataStatBadge(label: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MinimalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MinimalPurplePrimary)
            Text(text = label, fontSize = 10.sp, color = MinimalTextMuted)
        }
    }
}
