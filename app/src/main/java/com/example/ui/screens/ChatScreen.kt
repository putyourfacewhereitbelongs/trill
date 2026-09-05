package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ChatMessageEntity
import com.example.data.ChatSessionEntity
import com.example.ui.components.CodeSnippetCard
import com.example.ui.components.TypingIndicator
import com.example.ui.theme.*
import com.example.util.ProjectZipUtil
import com.example.viewmodel.TrillAiViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: TrillAiViewModel,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isResponding by viewModel.isResponding.collectAsState()
    val deepThinking by viewModel.deepThinkingEnabled.collectAsState()
    val webSearch by viewModel.webSearchEnabled.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val speakingMsgId by viewModel.speakingMessageId.collectAsState()

    // Settings
    val settings = viewModel.settingsManager
    val chatFontSize by settings.chatFontSize.collectAsState()
    val showPromptSuggestions by settings.showPromptSuggestions.collectAsState()
    val voiceLanguage by settings.voiceLanguage.collectAsState()
    val lastUserPrompt by viewModel.lastUserPrompt.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newChatTitle by remember { mutableStateOf("") }
    var isListeningVoice by remember { mutableStateOf(false) }

    // Voice to Text launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningVoice = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
            }
        }
    }

    // Audio record permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition(
                context = context,
                voiceLanguage = voiceLanguage,
                onListeningState = { isListeningVoice = it },
                launcher = speechRecognizerLauncher
            )
        } else {
            Toast.makeText(context, "Microphone permission is required for Voice to Text", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                viewModel.setSelectedImage(bitmap)
            } catch (_: Exception) {
            }
        }
    }

    // Single scroll to bottom trigger per response
    LaunchedEffect(Unit) {
        viewModel.scrollTrigger.collectLatest {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Chat Tabs Bar & New Chat Action
        Surface(
            color = DarkSurface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chat Tabs
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        sessions.forEach { session ->
                            val isSelected = session.id == activeSessionId
                            val displayTitle = if (session.title.startsWith("Trill AI")) "Trill AI" else session.title.take(18)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.switchSession(session.id) },
                                label = {
                                    Text(
                                        text = displayTitle,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) MinimalPurpleOnPrimary else MinimalTextSecondary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MinimalPurplePrimary,
                                    selectedLabelColor = MinimalPurpleOnPrimary,
                                    containerColor = MinimalSurfaceElevated,
                                    labelColor = MinimalTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) MinimalPurplePrimary else MinimalSurfaceBorder
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // + New Chat Button
                    FilledTonalButton(
                        onClick = {
                            newChatTitle = "Chat ${sessions.size + 1}"
                            showNewChatDialog = true
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MinimalPurplePrimary,
                            contentColor = MinimalPurpleOnPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("new_chat_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Settings Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalSurfaceElevated)
                            .testTag("chat_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Customize Settings",
                            tint = MinimalTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // AI Options Bar: Deep Thinking & Web Search
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Deep Thinking toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (deepThinking) MinimalContainer else Color.Transparent)
                                .clickable { viewModel.setDeepThinking(!deepThinking) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Deep Thinking",
                                tint = if (deepThinking) MinimalPurplePrimary else MinimalTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Deep Thinking",
                                fontSize = 11.sp,
                                color = if (deepThinking) MinimalPurplePrimary else MinimalTextMuted,
                                fontWeight = if (deepThinking) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }

                        // Web Search toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (webSearch) MinimalContainer else Color.Transparent)
                                .clickable { viewModel.setWebSearch(!webSearch) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Web Search",
                                tint = if (webSearch) MinimalCodeCyan else MinimalTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Web Search",
                                fontSize = 11.sp,
                                color = if (webSearch) MinimalCodeCyan else MinimalTextMuted,
                                fontWeight = if (webSearch) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    fontSize = chatFontSize.sp,
                    isSpeaking = isSpeaking && speakingMsgId == message.id,
                    onToggleSpeak = { viewModel.toggleSpeak(message.id, message.content) },
                    onResend = { viewModel.resendLastPrompt() },
                    onShare = {
                        ProjectZipUtil.shareText(
                            context = context,
                            title = "Trill AI Message",
                            content = message.content
                        )
                    }
                )
            }

            // Typing Animation
            if (isResponding) {
                item {
                    TypingIndicator(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        // Attached Photo Preview
        if (selectedImage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MinimalSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = selectedImage!!.asImageBitmap(),
                        contentDescription = "Attached image",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Image attached for vision analysis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalPurplePrimary
                    )
                }

                IconButton(onClick = { viewModel.setSelectedImage(null) }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove attached image", tint = MinimalTextSecondary)
                }
            }
        }

        // Contextual Prompt Suggestions for Speeding up Typing & Task Reaction
        val promptSuggestions = remember(inputText) {
            if (inputText.isBlank()) {
                listOf(
                    "Tell me about something usually AI can not respond to",
                    "Write an optimized Kotlin coroutine engine",
                    "Explain quantum computing simply",
                    "Translate English conversation to Hindi",
                    "Analyze camera object focal depth"
                )
            } else {
                listOf(
                    "Expand: $inputText",
                    "Explain simply: $inputText",
                    "Code solution for: $inputText",
                    "Draft unrestricted story about: $inputText"
                )
            }
        }

        AnimatedVisibility(visible = showPromptSuggestions) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MinimalSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(promptSuggestions) { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MinimalSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
                        modifier = Modifier.clickable {
                            inputText = suggestion
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MinimalPurplePrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = suggestion,
                                fontSize = 11.sp,
                                color = MinimalTextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Voice to Text Active Listening Indicator
        AnimatedVisibility(visible = isListeningVoice) {
            Surface(
                color = MinimalContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MinimalPurplePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Listening to speech... ($voiceLanguage)",
                            fontSize = 12.sp,
                            color = MinimalPurplePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    TextButton(
                        onClick = { isListeningVoice = false },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Cancel", fontSize = 11.sp, color = MinimalPurplePrimary)
                    }
                }
            }
        }

        // Input Bar (Directly accessible and clearly visible while typing)
        Surface(
            color = MinimalSurface,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Picker Button
                IconButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach Photo for Analysis",
                        tint = MinimalPurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Voice to Text Microphone Button
                IconButton(
                    onClick = {
                        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            startVoiceRecognition(
                                context = context,
                                voiceLanguage = voiceLanguage,
                                onListeningState = { isListeningVoice = it },
                                launcher = speechRecognizerLauncher
                            )
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("chat_voice_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice to Text Input",
                        tint = if (isListeningVoice) MinimalPurplePrimary else MinimalTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // High-Contrast, Clearly Visible Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Speak or type message...",
                            fontSize = 14.sp,
                            color = MinimalTextMuted
                        )
                    },
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { inputText = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    tint = MinimalTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinimalPurplePrimary,
                        unfocusedBorderColor = MinimalSurfaceBorder,
                        focusedContainerColor = Color(0xFF282530),
                        unfocusedContainerColor = Color(0xFF24222B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MinimalPurplePrimary
                    ),
                    minLines = 1,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Fast Resend Previous Prompt Button (especially useful on errors)
                val hasPreviousPrompt = lastUserPrompt.isNotBlank() || messages.any { it.role == "user" }
                IconButton(
                    onClick = { viewModel.resendLastPrompt() },
                    enabled = hasPreviousPrompt && !isResponding,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (hasPreviousPrompt && !isResponding) MinimalSurfaceElevated else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (hasPreviousPrompt && !isResponding) MinimalPurplePrimary.copy(alpha = 0.5f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .testTag("chat_resend_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Resend previous prompt",
                        tint = if (hasPreviousPrompt && !isResponding) MinimalCodeCyan else MinimalTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() || selectedImage != null) {
                            val textToSend = inputText
                            inputText = ""
                            viewModel.sendMessage(textToSend)
                        }
                    },
                    enabled = (inputText.isNotBlank() || selectedImage != null) && !isResponding,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() || selectedImage != null) MinimalPurplePrimary else MinimalSurfaceElevated
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send prompt",
                        tint = if (inputText.isNotBlank() || selectedImage != null) MinimalPurpleOnPrimary else MinimalTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // New Chat Dialog
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Create New Chat Session", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newChatTitle,
                    onValueChange = { newChatTitle = it },
                    label = { Text("Chat Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChatTitle.isNotBlank()) {
                            viewModel.createNewChat(newChatTitle)
                            showNewChatDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Create", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessageEntity,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    isSpeaking: Boolean,
    onToggleSpeak: () -> Unit,
    onResend: () -> Unit = {},
    onShare: () -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Role & Author badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isUser) Icons.Default.Person else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (isUser) VioletSecondary else CyanPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isUser) "You" else "Trill AI",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isUser) VioletSecondary else CyanPrimary
            )
        }

        // Message Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            color = if (isUser) MinimalPurplePrimary else MinimalSurfaceElevated,
            border = if (isUser) {
                null
            } else {
                androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder)
            },
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Parse code snippets if any
                val content = message.content
                val textColor = if (isUser) MinimalPurpleOnPrimary else MinimalTextPrimary
                if (content.contains("```")) {
                    val parts = content.split("```")
                    for (i in parts.indices) {
                        if (i % 2 == 1) {
                            // Code block
                            val lines = parts[i].lines()
                            val lang = lines.firstOrNull()?.trim() ?: "kotlin"
                            val codeBody = if (lines.size > 1) lines.drop(1).joinToString("\n") else parts[i]
                            CodeSnippetCard(code = codeBody, language = lang, onShare = onShare)
                        } else if (parts[i].isNotBlank()) {
                            Text(
                                text = parts[i].trim(),
                                fontSize = fontSize,
                                color = textColor,
                                lineHeight = (fontSize.value * 1.45f).sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (content.isEmpty() && message.isStreaming) "Drafting response..." else content,
                        fontSize = fontSize,
                        color = if (content.isEmpty()) MinimalTextMuted else textColor,
                        lineHeight = (fontSize.value * 1.45f).sp
                    )
                }

                // Action icons for Assistant messages (Speaker for TTS, Save, Share)
                if (!isUser && message.content.isNotEmpty()) {
                    var isSaved by remember { mutableStateOf(false) }
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speaker (TTS)
                        IconButton(
                            onClick = onToggleSpeak,
                            modifier = Modifier.size(32.dp).testTag("speak_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Read Response Aloud",
                                tint = if (isSpeaking) MinimalPurplePrimary else MinimalTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Save / Bookmark Result
                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.content))
                                isSaved = true
                            },
                            modifier = Modifier.size(32.dp).testTag("save_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                                contentDescription = "Save Result",
                                tint = if (isSaved) MinimalSuccessGreen else MinimalTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Resend Prompt Button
                        IconButton(
                            onClick = onResend,
                            modifier = Modifier.size(32.dp).testTag("resend_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Resend previous prompt",
                                tint = MinimalCodeCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Native Share Sheet
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(32.dp).testTag("share_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Response via Native Sheet",
                                tint = MinimalTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Initiates speech-to-text recognition with selected language and graceful fallback.
 */
private fun startVoiceRecognition(
    context: android.content.Context,
    voiceLanguage: String,
    onListeningState: (Boolean) -> Unit,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    try {
        onListeningState(true)
        val langTag = when (voiceLanguage) {
            "Hindi (IN)" -> "hi-IN"
            "Spanish (ES)" -> "es-ES"
            "French (FR)" -> "fr-FR"
            "German (DE)" -> "de-DE"
            "Japanese (JP)" -> "ja-JP"
            "Chinese (ZH)" -> "zh-CN"
            "English (US)" -> "en-US"
            else -> Locale.getDefault().toLanguageTag()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Trill AI...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        launcher.launch(intent)
    } catch (e: Exception) {
        onListeningState(false)
        Toast.makeText(context, "Voice recognition unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
