package com.nisaefendioglu.qrcodescanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.nisaefendioglu.qrcodescanner.ui.theme.QRCodeScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRCodeScannerTheme {
                var code by remember {
                    mutableStateOf("")
                }
                var hasReadCode by remember {
                    mutableStateOf(false)
                }
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val cameraProviderFeature = remember {
                    ProcessCameraProvider.getInstance(context)
                }
                var hasCamPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        hasCamPermission = granted
                    }
                )
                LaunchedEffect(key1 = true) {
                    launcher.launch(Manifest.permission.CAMERA)
                }
                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(Color(0xFFAED581))
                        .padding(50.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (hasCamPermission) {
                        if (hasReadCode) {
                            LoadWebUrl(code)
                        } else {
                            AndroidView(
                                factory = { context ->
                                    val previewView = PreviewView(context)
                                    val preview = Preview.Builder().build()
                                    val selector = CameraSelector.Builder()
                                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                        .build()
                                    preview.setSurfaceProvider(previewView.surfaceProvider)
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setTargetResolution(Size(
                                            previewView.width,
                                            previewView.height
                                        ))
                                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                    imageAnalysis.setAnalyzer(
                                        ContextCompat.getMainExecutor(context),
                                        QRCode { result ->
                                            code = result
                                            hasReadCode = true
                                        }
                                    )
                                    try {
                                        cameraProviderFeature.get().bindToLifecycle(
                                            lifecycleOwner,
                                            selector,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    previewView
                                },
                                modifier = Modifier.border(10.dp, color = Color(0xFF32FF7E))
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LoadWebUrl(url: String) {
        AndroidView(factory = {
            WebView(this).apply {
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        })
    }

}