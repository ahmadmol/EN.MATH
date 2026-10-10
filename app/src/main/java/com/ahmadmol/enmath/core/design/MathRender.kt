package com.ahmadmol.enmath.core.design

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

/** Offline KaTeX renderer. No JS bridge, no network, and no user-supplied HTML. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathRender(value: String, style: TextStyle = MaterialTheme.typography.titleLarge) {
    if (LocalInspectionMode.current) {
        Text(value, style = style)
        return
    }
    val color = MaterialTheme.colorScheme.onSurface.toArgb()
    val cssColor = "#%06x".format(color and 0xffffff)
    val latex = remember(value) { mathTex(value) }
    val literal = JSONObject.quote(latex).replace("<", "\\u003c")
    val fallback = JSONObject.quote(value).replace("<", "\\u003c")
    var rendered by remember(value, cssColor) { mutableStateOf(false) }
    val html =
        """<!doctype html><html lang="en" dir="ltr"><head><meta name="viewport" content="width=device-width, initial-scale=1"><link rel="stylesheet" href="katex.min.css"><style>html,body{margin:0;padding:0;background:transparent;color:$cssColor;}body{font-size:${style.fontSize.value.toInt().coerceAtLeast(16)}px;}#math{padding:10px 0;overflow-x:auto;white-space:nowrap;} .katex{color:$cssColor;}</style></head><body><div id="math"></div><script src="katex.min.js"></script><script>try{katex.render($literal,document.getElementById('math'),{throwOnError:true,trust:false,strict:'ignore',output:'htmlAndMathml',maxExpand:100});}catch(e){document.getElementById('math').textContent=$fallback;}</script></body></html>"""
    Box(
        Modifier.fillMaxWidth().height(Space.hero + Space.lg).testTag("math-render").semantics {
            stateDescription = if (rendered) "math-ready" else "math-loading"
        }
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    settings.javaScriptEnabled = true
                    settings.blockNetworkLoads = true
                    settings.allowContentAccess = false
                    settings.allowFileAccess = true
                    settings.builtInZoomControls = false
                    webViewClient =
                        object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, url: String) = true

                            override fun onPageFinished(view: WebView, url: String) {
                                view.evaluateJavascript(
                                    "document.getElementById('math').textContent.length > 0"
                                ) {
                                    rendered = it == "true"
                                }
                            }
                        }
                    isVerticalScrollBarEnabled = false
                }
            },
            update = { view ->
                if (view.tag != html) {
                    view.tag = html
                    view.loadDataWithBaseURL(
                        "file:///android_asset/katex/",
                        html,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                }
            },
            onRelease = { it.destroy() },
            modifier =
                Modifier.fillMaxWidth().height(Space.hero + Space.lg).semantics {
                    text = AnnotatedString(value)
                },
        )
        if (!rendered) Text(value, style = style, modifier = Modifier.padding(vertical = Space.sm))
    }
}

fun mathTex(value: String): String {
    if (value.startsWith("\\")) return value
    return value
        .replace("\\", "\\backslash ")
        .replace("{", "\\{")
        .replace("}", "\\}")
        .replace("%", "\\%")
        .replace("#", "\\#")
        .replace("&", "\\&")
        .replace("ⁿ⁻¹", "^{n-1}")
        .replace("ⁿ⁺¹", "^{n+1}")
        .replace("²", "^{2}")
        .replace("³", "^{3}")
        .replace("ⁿ", "^{n}")
        .replace("⁻¹", "^{-1}")
        .replace("¹", "^{1}")
        .replace("ₙ", "_{n}")
        .replace("₁", "_{1}")
        .replace("₂", "_{2}")
        .replace("₀", "_{0}")
        .replace("∫", "\\int ")
        .replace("∑", "\\sum ")
        .replace("π", "\\pi ")
        .replace("∞", "\\infty ")
        .replace("→", "\\to ")
        .replace("×", "\\times ")
        .replace("÷", "\\div ")
        .replace("−", "-")
        .replace("≠", "\\ne ")
        .replace("∩", "\\cap ")
        .replace("Ω", "\\Omega ")
        .replace("ln(", "\\ln(")
        .replace("sin(", "\\sin(")
        .replace("cos(", "\\cos(")
        .replace("tan(", "\\tan(")
        .replace("d/dx", "\\frac{d}{dx}")
        .replace(" at ", "\\text{ at } ")
        .replace("det ", "\\det ")
        .replace("even on fair die", "\\text{even on fair die}")
        .replace("head on fair coin", "\\text{head on fair coin}")
}
