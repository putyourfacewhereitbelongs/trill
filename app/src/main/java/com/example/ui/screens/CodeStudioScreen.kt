package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.CodeFileEntity
import com.example.data.CodeProjectEntity
import com.example.ui.theme.*
import com.example.viewmodel.TrillAiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeStudioScreen(
    viewModel: TrillAiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.codeProjects.collectAsState()
    val activeProjectId by viewModel.activeProjectId.collectAsState()
    val projectFiles by viewModel.activeProjectFiles.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var newProjName by remember { mutableStateOf("") }
    var newProjDesc by remember { mutableStateOf("") }
    var newProjLang by remember { mutableStateOf("html") }

    var isLivePreviewMode by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf("") }

    LaunchedEffect(selectedFile) {
        editedContent = selectedFile?.content ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Studio Top Bar
        Surface(
            color = DarkSurface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Code Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Trill AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Toggle Real-Time Preview
                        FilledTonalButton(
                            onClick = { isLivePreviewMode = !isLivePreviewMode },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isLivePreviewMode) EmeraldTertiary else DarkSurfaceElevated,
                                contentColor = if (isLivePreviewMode) Color(0xFF003919) else EmeraldTertiary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("toggle_preview_button")
                        ) {
                            Icon(
                                imageVector = if (isLivePreviewMode) Icons.Default.Code else Icons.Default.PlayArrow,
                                contentDescription = "Live Preview",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLivePreviewMode) "Editor" else "Live Preview",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Export Zip Button
                        Button(
                            onClick = { viewModel.exportProjectAsZip(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VioletSecondary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("export_zip_button")
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = "Export ZIP", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ZIP Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Projects Selector Horizontal Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    projects.forEach { proj ->
                        val isSelected = proj.id == activeProjectId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.switchProject(proj.id) },
                            label = { Text(proj.name, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isSelected) CyanPrimary else TextSecondaryDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary,
                                containerColor = DarkSurfaceElevated,
                                labelColor = TextSecondaryDark
                            )
                        )
                    }

                    // + New Project button
                    IconButton(
                        onClick = { showNewProjectDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "New Project", tint = CyanPrimary)
                    }
                }
            }
        }

        // Project Files Tab Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D121D))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            projectFiles.forEach { file ->
                val isSelected = file.id == selectedFile?.id
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) DarkSurfaceElevated else Color.Transparent,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f)) else null,
                    modifier = Modifier
                        .clickable { viewModel.selectFile(file) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (file.language.lowercase()) {
                                "html" -> Icons.Default.Language
                                "python" -> Icons.Default.Terminal
                                "kotlin" -> Icons.Default.Android
                                else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = if (isSelected) CyanPrimary else TextMutedDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.filename,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else TextSecondaryDark,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Main Editor or Real-Time Preview Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isLivePreviewMode) {
                // Real-Time Live Sandbox Renderer
                Column(modifier = Modifier.fillMaxSize().background(Color(0xFF090D15))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Live Execution Sandbox (HTML5 / Web / Polyglot)", fontSize = 11.sp, color = EmeraldTertiary, fontWeight = FontWeight.Bold)
                    }

                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                val codeToRun = editedContent
                                val finalHtml = if (codeToRun.contains("<!DOCTYPE") || codeToRun.contains("<html")) {
                                    codeToRun
                                } else {
                                    "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>body{font-family:monospace;padding:20px;background:#0d1117;color:#58a6ff;white-space:pre-wrap;}</style></head><body><h2>⚡ Trill AI Terminal Output</h2><hr><pre>$codeToRun</pre></body></html>"
                                }
                                loadDataWithBaseURL(null, finalHtml, "text/html", "UTF-8", null)
                            }
                        },
                        update = { webView ->
                            val codeToRun = editedContent
                            val finalHtml = if (codeToRun.contains("<!DOCTYPE") || codeToRun.contains("<html")) {
                                codeToRun
                            } else {
                                "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>body{font-family:monospace;padding:20px;background:#0d1117;color:#58a6ff;white-space:pre-wrap;}</style></head><body><h2>⚡ Trill AI Terminal Output</h2><hr><pre>$codeToRun</pre></body></html>"
                            }
                            webView.loadDataWithBaseURL(null, finalHtml, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // High Performance Code Editor
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = editedContent,
                        onValueChange = {
                            editedContent = it
                            selectedFile?.let { f ->
                                viewModel.updateFileContent(f.id, it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_editor_field"),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFF0F6FC),
                            lineHeight = 18.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CodeEditorBg,
                            unfocusedContainerColor = CodeEditorBg,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Code Project", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newProjName,
                        onValueChange = { newProjName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProjDesc,
                        onValueChange = { newProjDesc = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("html", "kotlin", "python", "javascript").forEach { lang ->
                            FilterChip(
                                selected = newProjLang == lang,
                                onClick = { newProjLang = lang },
                                label = { Text(lang.uppercase()) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjName.isNotBlank()) {
                            viewModel.createNewProject(newProjName, newProjDesc, newProjLang)
                            showNewProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Create", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
