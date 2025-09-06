package com.example.adityalearn;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webviews);
        setupWebView();
        webView.loadUrl("https://info.aec.edu.in/ACET/Default.aspx?ReturnUrl=%2facet%2f");
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Inject JavaScript to show only the login section
                injectLoginFilter();
            }
        });
    }

    private void injectLoginFilter() {
        String javascript = "(function() {" +
                "   var loginPanel = document.getElementById('pnlLogin');" + // Target the login panel
                "   if (loginPanel) {" +
                "       document.body.innerHTML = '';" + // Clear entire page
                "       document.body.appendChild(loginPanel);" + // Keep only login panel
                "       loginPanel.style.width = '100%';" +
                "       loginPanel.style.margin = '0';" +
                "       loginPanel.style.padding = '20px';" +
                "   }" +
                "})();";

        webView.evaluateJavascript(javascript, null);

        // Re-inject after short delay to ensure elements are loaded
        webView.postDelayed(() -> webView.evaluateJavascript(javascript, null), 500);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}