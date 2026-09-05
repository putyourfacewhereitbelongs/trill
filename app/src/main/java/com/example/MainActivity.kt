package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedSplashScreen
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.TrillAiViewModel

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    CHAT("Chat", Icons.Default.ChatBubble, "tab_chat"),
    CODE("Code", Icons.Default.Code, "tab_code"),
    TRANSLATE("Translate", Icons.Default.GTranslate, "tab_translate"),
    VISION("Vision", Icons.Default.CameraAlt, "tab_vision"),
    PROJECTS("Projects", Icons.Default.FolderSpecial, "tab_projects"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: TrillAiViewModel by viewModels()

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                var showSplashScreen by remember { mutableStateOf(true) }
                var selectedTab by remember { mutableStateOf(AppTab.CHAT) }
                var showExitDialog by remember { mutableStateOf(false) }

                // Exit Confirmation on Back
                BackHandler {
                    if (showSplashScreen) {
                        finish()
                    } else {
                        showExitDialog = true
                    }
                }

                if (showExitDialog) {
                    ExitConfirmationDialog(
                        onConfirmExit = {
                            showExitDialog = false
                            finish()
                        },
                        onDismiss = { showExitDialog = false }
                    )
                }

                if (showSplashScreen) {
                    AnimatedSplashScreen(
                        onContinueClicked = { showSplashScreen = false }
                    )
                } else {
                    val isImeVisible = WindowInsets.isImeVisible

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground),
                        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                        bottomBar = {
                            AnimatedVisibility(
                                visible = !isImeVisible,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                            ) {
                                NavigationBar(
                                    containerColor = MinimalSurface,
                                    contentColor = MinimalTextPrimary,
                                    tonalElevation = 3.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("bottom_navigation_bar")
                                ) {
                                    AppTab.values().forEach { tab ->
                                        val isSelected = selectedTab == tab
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { selectedTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title,
                                                    tint = if (isSelected) MinimalPurplePrimary else MinimalTextSecondary
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (isSelected) MinimalPurplePrimary else MinimalTextSecondary
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = MinimalContainer,
                                                selectedIconColor = MinimalPurplePrimary,
                                                unselectedIconColor = MinimalTextSecondary,
                                                selectedTextColor = MinimalPurplePrimary,
                                                unselectedTextColor = MinimalTextSecondary
                                            ),
                                            modifier = Modifier.testTag(tab.tag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = innerPadding.calculateTopPadding(),
                                    bottom = if (isImeVisible) 0.dp else innerPadding.calculateBottomPadding()
                                )
                                .consumeWindowInsets(innerPadding)
                                .imePadding()
                        ) {
                            when (selectedTab) {
                                AppTab.CHAT -> ChatScreen(
                                    viewModel = viewModel,
                                    onOpenSettings = { selectedTab = AppTab.SETTINGS }
                                )
                                AppTab.CODE -> CodeStudioScreen(viewModel = viewModel)
                                AppTab.TRANSLATE -> LiveTranslateScreen(viewModel = viewModel)
                                AppTab.VISION -> LiveCameraVisionScreen(viewModel = viewModel)
                                AppTab.PROJECTS -> ProjectsAndSyncScreen(
                                    viewModel = viewModel,
                                    onNavigateToTab = { tab -> selectedTab = tab }
                                )
                                AppTab.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { selectedTab = AppTab.CHAT }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
