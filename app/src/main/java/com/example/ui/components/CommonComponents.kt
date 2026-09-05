package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Animated Splash Screen with Loading Bar, Pulsing "by Brian Cross", and Continue button
 */
@Composable
fun AnimatedSplashScreen(
    onContinueClicked: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoaded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(25)
            progress += 0.02f
        }
        progress = 1f
        isLoaded = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Minimalist Logo Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MinimalSurfaceElevated)
                    .border(1.5.dp, MinimalPurplePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Trill AI Logo",
                    tint = MinimalPurplePrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Minimalist Title
            Text(
                text = "Trill AI",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MinimalTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pulsing "BY BRIAN CROSS"
            Text(
                text = "BY BRIAN CROSS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                ),
                color = MinimalPurplePrimary.copy(alpha = glowAlpha),
                modifier = Modifier
                    .scale(pulseScale)
                    .testTag("author_tag")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Unrestricted Creative Intelligence & High-Speed Engine",
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Loading Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MinimalPurplePrimary,
                    trackColor = MinimalContainer,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isLoaded) "Ready • 100%" else "Initializing environment... ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalTextMuted
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button (enabled when loaded)
            AnimatedVisibility(
                visible = isLoaded,
                enter = fadeIn(tween(400)) + scaleIn(tween(400))
            ) {
                Button(
                    onClick = onContinueClicked,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                        .testTag("splash_continue_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MinimalPurplePrimary,
                        contentColor = MinimalPurpleOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Continue into Trill AI"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exit Confirmation Dialog on Back Navigation
 */
@Composable
fun ExitConfirmationDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Exit Trill AI?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to quit? Your active stories, code sessions, and translations are safely saved in local storage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("confirm_exit_button")
            ) {
                Text("Exit App")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_exit_button")
            ) {
                Text("Stay in App")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Active Typing Indicator Animation
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    text: String = "Trill AI is generating..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d3"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalSurfaceElevated)
            .border(1.dp, MinimalSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MinimalPurplePrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MinimalPurplePrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MinimalPurplePrimary.copy(alpha = dot1)))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MinimalPurplePrimary.copy(alpha = dot2)))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MinimalPurplePrimary.copy(alpha = dot3)))
        }
    }
}

/**
 * Real-time Code Snippet Card with live execution/preview runner
 */
@Composable
fun CodeSnippetCard(
    code: String,
    language: String = "kotlin",
    onShare: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    var showLivePreviewModal by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MinimalSurfaceBorder, MinimalSurfaceBorder)))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MinimalSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MinimalPurplePrimary.copy(alpha = 0.6f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = language.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MinimalPurplePrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Live Preview button if HTML / JS / Web
                    if (language.equals("html", true) || language.equals("javascript", true) || language.equals("js", true) || code.contains("<!DOCTYPE") || code.contains("<html")) {
                        FilledTonalButton(
                            onClick = { showLivePreviewModal = true },
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MinimalContainer,
                                contentColor = MinimalPurplePrimary
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Live Preview", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Copy Button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                            copied = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy Code",
                            tint = if (copied) MinimalSuccessGreen else MinimalTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Code",
                            tint = MinimalTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    ),
                    color = MinimalTextPrimary
                )
            }
        }
    }

    // Live HTML / JS Preview Dialog
    if (showLivePreviewModal) {
        Dialog(onDismissRequest = { showLivePreviewModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Preview, contentDescription = null, tint = CyanPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Live Code Preview", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        IconButton(onClick = { showLivePreviewModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                val htmlData = if (code.contains("<!DOCTYPE") || code.contains("<html")) {
                                    code
                                } else {
                                    "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>body{font-family:sans-serif;padding:16px;background:#0d1117;color:#c9d1d9;}</style></head><body><script>$code</script><div id='app'><h3>Execution Output</h3><p>Script mounted in live sandbox.</p></div></body></html>"
                                }
                                loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}
