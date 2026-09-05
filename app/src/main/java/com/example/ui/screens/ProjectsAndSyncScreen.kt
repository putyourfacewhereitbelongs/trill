package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppTab
import com.example.ui.theme.*
import com.example.util.ProjectZipUtil
import com.example.viewmodel.TrillAiViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class ProjectSearchCategory(val label: String) {
    ALL("All"),
    CHATS("Chats"),
    CODE("Code Projects"),
    VECTORS("Learned Vectors")
}

@Composable
fun ProjectsAndSyncScreen(
    viewModel: TrillAiViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: (AppTab) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val projects by viewModel.codeProjects.collectAsState()
    val activeProjectId by viewModel.activeProjectId.collectAsState()
    val patterns by viewModel.learnedPatterns.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProjectSearchCategory.ALL) }
    var showSyncCopiedToast by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    // Filter sessions based on search query
    val filteredSessions = remember(sessions, searchQuery, selectedCategory) {
        if (selectedCategory == ProjectSearchCategory.CODE || selectedCategory == ProjectSearchCategory.VECTORS) {
            emptyList()
        } else if (searchQuery.isBlank()) {
            sessions
        } else {
            sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Filter code projects based on search query
    val filteredProjects = remember(projects, searchQuery, selectedCategory) {
        if (selectedCategory == ProjectSearchCategory.CHATS || selectedCategory == ProjectSearchCategory.VECTORS) {
            emptyList()
        } else if (searchQuery.isBlank()) {
            projects
        } else {
            projects.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.defaultLanguage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Filter learned patterns
    val filteredPatterns = remember(patterns, searchQuery, selectedCategory) {
        if (selectedCategory == ProjectSearchCategory.CHATS || selectedCategory == ProjectSearchCategory.CODE) {
            emptyList()
        } else if (searchQuery.isBlank()) {
            patterns
        } else {
            patterns.filter {
                it.patternKey.contains(searchQuery, ignoreCase = true) ||
                        it.patternCategory.contains(searchQuery, ignoreCase = true) ||
                        it.value.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val totalMatchingCount = filteredSessions.size + filteredProjects.size + filteredPatterns.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(MinimalPurplePrimary, MinimalContainer))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MinimalContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MinimalPurplePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Workspace Organization & Search",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = "SEARCH LOCAL CHATS & CODE",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MinimalPurplePrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Local Search Input Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_search_bar"),
                        placeholder = {
                            Text(
                                text = "Search saved chats, code projects, vectors...",
                                fontSize = 13.sp,
                                color = MinimalTextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MinimalPurplePrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = MinimalTextSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalPurplePrimary,
                            unfocusedBorderColor = MinimalSurfaceBorder,
                            focusedContainerColor = MinimalSurface,
                            unfocusedContainerColor = MinimalSurface,
                            focusedTextColor = MinimalTextPrimary,
                            unfocusedTextColor = MinimalTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ProjectSearchCategory.values()) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
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
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Found $totalMatchingCount matching item(s) for \"$searchQuery\"",
                            fontSize = 12.sp,
                            color = MinimalTextSecondary
                        )
                    }
                }
            }
        }

        // Empty Search Results State
        if (searchQuery.isNotBlank() && totalMatchingCount == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MinimalTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No items match \"$searchQuery\"",
                            style = MaterialTheme.typography.titleSmall,
                            color = MinimalTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search terms or category filter.",
                            fontSize = 12.sp,
                            color = MinimalTextMuted
                        )
                    }
                }
            }
        }

        // Section: Filtered Saved Chat Sessions
        if (filteredSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MinimalPurplePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saved Chat Sessions (${filteredSessions.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MinimalTextPrimary
                        )
                    }
                }
            }

            items(filteredSessions, key = { "chat_${it.id}" }) { session ->
                val isActive = session.id == activeSessionId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.switchSession(session.id)
                            onNavigateToTab(AppTab.CHAT)
                        }
                        .testTag("session_item_${session.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MinimalSurfaceElevated else MinimalSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isActive) MinimalPurplePrimary else MinimalSurfaceBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) MinimalPurplePrimary else MinimalContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = if (isActive) MinimalPurpleOnPrimary else MinimalPurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MinimalTextPrimary
                                    )
                                    if (isActive) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MinimalPurplePrimary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MinimalPurpleOnPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormat.format(Date(session.createdAt)),
                                    fontSize = 11.sp,
                                    color = MinimalTextMuted
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    viewModel.switchSession(session.id)
                                    onNavigateToTab(AppTab.CHAT)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Open Chat",
                                    tint = MinimalPurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (sessions.size > 1) {
                                IconButton(
                                    onClick = { viewModel.deleteSession(session.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Chat",
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

        // Section: Filtered Saved Code Projects
        if (filteredProjects.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MinimalCodeCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saved Code Projects (${filteredProjects.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MinimalTextPrimary
                        )
                    }
                }
            }

            items(filteredProjects, key = { "proj_${it.id}" }) { project ->
                val isActive = project.id == activeProjectId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.switchProject(project.id)
                            onNavigateToTab(AppTab.CODE)
                        }
                        .testTag("project_item_${project.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MinimalSurfaceElevated else MinimalSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isActive) MinimalCodeCyan else MinimalSurfaceBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MinimalContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = MinimalCodeCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = project.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MinimalTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MinimalContainer
                                    ) {
                                        Text(
                                            text = project.defaultLanguage.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MinimalCodeCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (project.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = project.description,
                                        fontSize = 11.sp,
                                        color = MinimalTextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    viewModel.switchProject(project.id)
                                    viewModel.exportProjectAsZip(context)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export ZIP",
                                    tint = MinimalTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.switchProject(project.id)
                                    onNavigateToTab(AppTab.CODE)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Open Project",
                                    tint = MinimalCodeCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Filtered Learned ML Patterns
        if (filteredPatterns.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = MinimalPurplePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Local Machine Learning Vectors", fontWeight = FontWeight.SemiBold, color = MinimalTextPrimary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Trill AI refines storytelling and code syntax adaptations based on your input patterns without sending personal telemetry outside your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        filteredPatterns.take(4).forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MinimalPurplePrimary))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(p.patternKey, fontSize = 12.sp, color = MinimalTextPrimary)
                                }
                                Text(
                                    text = "x${p.usageCount} uses",
                                    fontSize = 11.sp,
                                    color = MinimalPurplePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cross-Platform Sync Engine
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = MinimalPurplePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cross-Platform Sync & Export", fontWeight = FontWeight.SemiBold, color = MinimalTextPrimary)
                        }

                        Button(
                            onClick = {
                                val syncObj = JSONObject().apply {
                                    put("appName", "Trill AI")
                                    put("platform", "Trill AI")
                                    put("timestamp", System.currentTimeMillis())
                                    put("sessionsCount", sessions.size)
                                    put("projectsCount", projects.size)
                                    put("inferenceServer", "https://trill-ai.putyourfacewhereitbelongs.workers.dev/")
                                }
                                clipboardManager.setText(AnnotatedString(syncObj.toString(2)))
                                showSyncCopiedToast = true
                                ProjectZipUtil.shareText(
                                    context,
                                    "Trill AI Cross-Platform Sync Package",
                                    syncObj.toString(2)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MinimalPurplePrimary,
                                contentColor = MinimalPurpleOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_sync_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Package", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Seamlessly synchronize your active workspaces across Android, iOS, Windows, Mac, Linux, and Web browsers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary
                    )
                }
            }
        }

        // Browser & PWA Hub (Web App info & offline installation)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MinimalPurplePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browser & PWA Access (Trill AI)", fontWeight = FontWeight.SemiBold, color = MinimalTextPrimary)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MinimalContainer
                        ) {
                            Text(
                                text = "Installable",
                                fontSize = 11.sp,
                                color = MinimalPurplePrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Trill AI supports universal PWA installation on desktop and mobile web browsers with pre-cached offline assets and low-latency Cloudflare inference.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MinimalSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Inference Endpoint:", fontSize = 10.sp, color = MinimalTextMuted)
                                Text("https://trill-ai.putyourfacewhereitbelongs.workers.dev/", fontSize = 11.sp, color = MinimalPurplePrimary, fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MinimalSuccessGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
