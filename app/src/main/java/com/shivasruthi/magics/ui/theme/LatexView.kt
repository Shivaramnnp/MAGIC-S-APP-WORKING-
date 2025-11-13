package com.shivasruthi.magics.ui.theme

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private fun getLocalHtmlTemplate(content: String): String {
    // --- THE DEFINITIVE FIX: Remove the incorrect double-escaping of backslashes ---
    // The string from the JSON parser is already correctly formatted as '\frac'.
    // We only need to escape characters that would break the JavaScript string literal itself.
    val escapedContent = content
        .replace("'", "\\'")   // Escape single quotes
        .replace("\n", "<br>") // Convert newlines to HTML line breaks
        .replace("\r", "")     // Remove carriage returns

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <script type="text/x-mathjax-config">
            MathJax.Hub.Config({
                showMathMenu: false,
                tex2jax: {
                    inlineMath: [['$','$'], ['\\(','\\)']],
                    displayMath: [['$$','$$'], ['\\[','\\]']],
                    processEscapes: true
                },
                TeX: { extensions: ["AMSmath.js", "AMSsymbols.js"] }
            });
        </script>
        <script type="text/javascript" async
            src="file:///android_asset/mathjax/MathJax.js?config=TeX-AMS-MML_HTMLorMML">
        </script>
        <style>
            body {
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                font-size: 1.1em;
                line-height: 1.6;
                background-color: transparent !important;
                color: #000000; /* Default to black text for light mode */
                margin: 0;
                padding: 8px;
            }
            @media (prefers-color-scheme: dark) {
                body {
                    color: #FFFFFF; /* White text in dark mode */
                }
            }
        </style>
    </head>
    <body>
        <div id="content-div">$escapedContent</div>
    </body>
    </html>
    """.trimIndent()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LatexView(
    text: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                setBackgroundColor(0x00000000)
            }
        },
        update = { webView ->
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webView.evaluateJavascript("MathJax.Hub.Queue(['Typeset', MathJax.Hub, 'content-div']);", null)
                }
            }
            val htmlContent = getLocalHtmlTemplate(text)
            webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}