package com.lightwhite.paper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lightwhite.paper.ui.theme.PaperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold {
                        AndroidWebView(
                            url = "http://paperclub.top",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun AndroidWebView(url: String, modifier: Modifier = Modifier) {
        var webView by remember { mutableStateOf<WebView?>(null) }
        val canGoBack = remember { mutableStateOf(false) }
        var showRefreshButton by remember { mutableStateOf(true) }
        var function: (() -> Unit)? = null

        BackHandler(enabled = canGoBack.value) {
            webView?.goBack()
        }


        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun doUpdateVisitedHistory(
                                view: WebView?,
                                url: String?,
                                isReload: Boolean
                            ) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                canGoBack.value = view?.canGoBack() ?: false
                            }
                        }

                        function = {
                            val currentWebView = this
                            Handler(Looper.getMainLooper()).post {
                                val scrollY = currentWebView.scrollY
                                showRefreshButton = scrollY <= 0
                            }
                        }
                        viewTreeObserver.addOnScrollChangedListener(function)
                        webView = this

                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                    }
                },
                modifier = modifier,
                update = { it.loadUrl(url) }
            )

            DisposableEffect(Unit) {
                onDispose {
                    webView?.apply {
                        loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
                        clearHistory()
                        viewTreeObserver.removeOnScrollChangedListener(function)
                        destroy()
                    }
                }
            }

            AnimatedVisibility(
                visible = showRefreshButton,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { 100 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
            ) {
                FloatingActionButton(
                    onClick = { webView?.reload() },
                    shape = CircleShape,
                    containerColor = Color(60, 239, 255, 255),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .size(64.dp)
                        .animateEnterExit(
                            enter = scaleIn(animationSpec = SpringSpec(stiffness = 500f)),
                            exit = scaleOut(animationSpec = SpringSpec(stiffness = 500f))
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }

        }
    }
}
