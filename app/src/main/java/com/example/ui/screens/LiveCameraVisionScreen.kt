package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.viewmodel.TrillAiViewModel
import java.util.concurrent.Executors

@Composable
fun LiveCameraVisionScreen(
    viewModel: TrillAiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val detectedObjects by viewModel.detectedObjects.collectAsState()
    val insights by viewModel.cameraAnalysisInsights.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingCamera.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraLensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (hasCameraPermission) {
            // CameraX Live Preview View
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        val executor = Executors.newSingleThreadExecutor()
                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            // Continuous analyzer
                            imageProxy.close()
                        }

                        try {
                            cameraProvider.unbindAll()
                            val selector = CameraSelector.Builder()
                                .requireLensFacing(cameraLensFacing)
                                .build()
                            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
                        } catch (_: Exception) {
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Cyber Vision HUD Overlay & Bounding Boxes
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Center Target Reticle
                val reticleSize = w * 0.55f
                val left = (w - reticleSize) / 2
                val top = (h - reticleSize) / 2

                drawRect(
                    color = CyanPrimary,
                    topLeft = Offset(left, top),
                    size = Size(reticleSize, reticleSize),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Corner brackets
                val bracketLen = 30.dp.toPx()
                drawLine(CyanPrimary, Offset(left, top), Offset(left + bracketLen, top), 5f)
                drawLine(CyanPrimary, Offset(left, top), Offset(left, top + bracketLen), 5f)

                drawLine(CyanPrimary, Offset(left + reticleSize, top), Offset(left + reticleSize - bracketLen, top), 5f)
                drawLine(CyanPrimary, Offset(left + reticleSize, top), Offset(left + reticleSize, top + bracketLen), 5f)

                drawLine(CyanPrimary, Offset(left, top + reticleSize), Offset(left + bracketLen, top + reticleSize), 5f)
                drawLine(CyanPrimary, Offset(left, top + reticleSize), Offset(left, top + reticleSize - bracketLen), 5f)

                drawLine(CyanPrimary, Offset(left + reticleSize, top + reticleSize), Offset(left + reticleSize - bracketLen, top + reticleSize), 5f)
                drawLine(CyanPrimary, Offset(left + reticleSize, top + reticleSize), Offset(left + reticleSize, top + reticleSize - bracketLen), 5f)
            }
        } else {
            // Permission Required Fallback
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant camera access to use Trill AI's real-time optical object identifier and neural frame recognition.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Grant Camera Access", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Control Header
        Surface(
            color = Color(0xCC090D15),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Object Identifier",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Trill AI Vision Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanPrimary
                    )
                }

                IconButton(
                    onClick = {
                        cameraLensFacing = if (cameraLensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera",
                        tint = CyanPrimary
                    )
                }
            }
        }

        // Bottom Telemetry & Object Detection Drawer
        Surface(
            color = MinimalSurfaceElevated.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REAL-TIME OBJECT TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalPurplePrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share Analysis Button
                        if (insights.isNotEmpty() || detectedObjects.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val summary = "Trill AI Live Vision Detection:\n" +
                                            "Objects: ${detectedObjects.joinToString(", ")}\n\n" +
                                            insights
                                    com.example.util.ProjectZipUtil.shareText(
                                        context = context,
                                        title = "Trill AI Vision Analysis",
                                        content = summary
                                    )
                                },
                                modifier = Modifier.size(32.dp).testTag("share_vision_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Vision Analysis",
                                    tint = MinimalTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Capture & Deep Analyze Button
                        Button(
                            onClick = {
                                val dummyBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
                                viewModel.processCameraFrameAnalysis(dummyBitmap)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MinimalPurplePrimary,
                                contentColor = MinimalPurpleOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("analyze_frame_button")
                        ) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Frame", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAnalyzing) "Scanning..." else "Analyze Scene",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detected Tag Pills
                val tags = if (detectedObjects.isNotEmpty()) {
                    detectedObjects
                } else {
                    listOf("Active Focus Lock", "RGB Optical Grid", "Zero Censorship Filter", "High-Throughput Frame")
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MinimalSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MinimalSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MinimalSuccessGreen))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tag, fontSize = 11.sp, color = MinimalTextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                if (insights.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = insights,
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary
                    )
                }
            }
        }
    }
}
