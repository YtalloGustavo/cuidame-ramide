package com.example.medapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.medapp.scanner.BarcodeAnalyzer
import com.example.medapp.ui.theme.EpilogueFontFamily
import com.example.medapp.ui.theme.GreenMid
import com.example.medapp.ui.theme.GreenPrimary

@Composable
fun BarcodeScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var hasResult by remember { mutableStateOf(false) }
    val currentOnBarcodeScanned by rememberUpdatedState(onBarcodeScanned)
    val stableAnalyzer = remember {
        BarcodeAnalyzer(onBarcodeDetected = { code ->
            if (!hasResult) {
                hasResult = true
                currentOnBarcodeScanned(code)
            }
        })
    }

    BackHandler(enabled = true, onBack = onBack)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!hasPermission) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Permissão de câmera necessária",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = EpilogueFontFamily
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Conceda o acesso à câmera para escanear o código de barras do medicamento.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontFamily = EpilogueFontFamily,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Conceder permissão")
                }
            }
            // Back button
            CloseButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart))
        } else {
            CameraPreview(
                lifecycleOwner = lifecycleOwner,
                analyzer = stableAnalyzer
            )

            CloseButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart))

            // Center scan guide
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .size(width = 260.dp, height = 140.dp)
                        .border(2.dp, GreenMid, RoundedCornerShape(12.dp))
                )
            }

            Text(
                text = "Aponte a câmera para o código de barras do produto",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = EpilogueFontFamily,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            )
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp, top = 56.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("✕", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
private fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    analyzer: BarcodeAnalyzer
) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    LaunchedEffect(analyzer) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderRef = cameraProvider
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer) }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("BarcodeScanner", "Failed to bind camera", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProviderRef?.unbindAll()
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}