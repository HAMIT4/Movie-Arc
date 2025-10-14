package com.hamit.moviearc.Ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hamit.moviearc.R;

public class PlayerActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private String movieTitle;
    private int tmdbId;
    private TextView errorText;
    private LinearLayout toolbar;
    private  TextView title;

    // Video sources
    private final String[] VIDEO_SOURCES = {
            "https://vidsrc.me/embed/",  // Primary - most reliable
            "https://2embed.org/embed/",
            "https://moviesapi.club/movie/",
            "https://autoembed.co/movie/",
            "https://vidlink.pro/movie/"
    };

    private int currentSourceIndex = 0;
    private boolean isPageLoading = false;

    private boolean sourceFound = false;

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        // forwarded data by intent
        movieTitle= getIntent().getStringExtra("movieTitle");
        tmdbId= getIntent().getIntExtra("tmdbId", -1);

        if (tmdbId == -1){
            Toast.makeText(this, "Movie Id not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // functions
        initializeViews();
        setupWebView();
        loadMovieWithFallback();

        // consume touch event till source is confirmed
        webView.setOnTouchListener((v, event) ->{
            if (!sourceFound){
                Log.d("PlayerActivity", "Blocking touch event before source is confirmed.");
                return true;
            }
            return false;
        });

    }

    private void initializeViews() {
        webView= findViewById(R.id.webView);
        progressBar= findViewById(R.id.progressBar);
        btnBack= findViewById(R.id.btn_back);
        errorText= findViewById(R.id.errorText);
        title= findViewById(R.id.toolbar_title);

        btnBack.setOnClickListener(v-> {finish();});
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings= webView.getSettings();

        // lets enable JavaScript
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        //just a check to debug
        WebView.setWebContentsDebuggingEnabled(true);


        // optimize performance
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // adjust layout
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient(){

            // let's try blocking ads an the popups

            // block redirection
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // legitimate video source domains (including redirect targets)
                String[] legitimateDomains = {
                        "vidsrc.me", "vidsrc.net", "vidsrc.to", "vidsrc.icu",  // vidsrc family
                        "2embed.org", "ww7.2embed.org", "ww5.2embed.org",      // 2embed family
                        "moviesapi.club", "moviesapi.cc",                      // moviesapi family
                        "autoembed.co", "autoembed.cc",                        // autoembed family
                        "vidlink.pro", "vidlink.io",                           // vidlink family
                        "multiembed.mov", "embed.sbs",                         // other common sources
                        "youtube.com", "youtu.be",                             // YouTube embeds
                        "vimeo.com"                                            // Vimeo embeds
                };

                boolean isLegitimateRedirect = false;
                for(String domain : legitimateDomains){
                    if (url.contains(domain)) {
                        isLegitimateRedirect = true;
                        Log.d("AdBlocker", "Allowing legitimate redirect to: " + url);
                        break;
                    }
                }

                // Block everything except legitimate video sources
                if (!isLegitimateRedirect) {
                    Log.w("AdBlocker", "Blocked ad/tracker redirect: " + url);
                    Log.d("AdBlocker", "Blocked ad redirect ");
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isPageLoading = true;
                progressBar.setVisibility(View.VISIBLE);
                errorText.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isPageLoading = false;
                progressBar.setVisibility(View.GONE);


                if (!sourceFound) {
                    // Retry mechanism for failed resources
                    webView.postDelayed(() -> checkPageSuccess(url), 3000);
                } else {
                    // Source already found, just remove timeout
                    webView.removeCallbacks(PlayerActivity.this::checkSourceTimeout);
                    Log.d("PlayerActivity", "Source already verified, skipping check");
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                progressBar.setVisibility(View.GONE);
                errorText.setVisibility(View.VISIBLE);
                // Only try next source if we haven't found one yet
                if (!sourceFound) {
                    errorText.setVisibility(View.VISIBLE);
                    // Silently handle chunk loading errors
                    if (!failingUrl.contains("_next/static/chunks")) {
                        Log.d("PlayerActivity", "WebView error: " + description);
                    }
                    tryNextSource();
                }
            }
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                // Ignore chunk loading errors and missing resources
                String url = request.getUrl().toString();
                if (url.contains("_next/static/chunks") || url.contains("cdn.jwplayer.com")) {
                    Log.d("PlayerActivity", "Ignoring missing resource: " + url);
                }
            }

        });


        webView.setWebChromeClient(new WebChromeClient(){

            // block new windows/ pop-ups
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                Log.w("AdBlocker", "Blocked new window/popup attempt.");
                return true; // true so as to block creation of new window.
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress < 100){
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    private void loadMovieWithFallback(){

        if (currentSourceIndex >= VIDEO_SOURCES.length) {
            allSourcesFailed();
            return;
        }

        String sourceUrl = VIDEO_SOURCES[currentSourceIndex] + tmdbId;
        Log.d("PlayerActivity", "Trying source " + (currentSourceIndex + 1) + ": " + sourceUrl);

        // Update UI to show current source
        if (movieTitle != null) {
            setTitle(movieTitle + " - Source " + (currentSourceIndex + 1));
            title.setText(movieTitle);
        }

        webView.loadUrl(sourceUrl);

        // Set timeout for this source (8 seconds)
        if (!sourceFound) {
            webView.postDelayed(this::checkSourceTimeout, 8000);
        }
    }
    private void checkSourceTimeout() {
        if (!sourceFound && (isPageLoading || progressBar.getVisibility() == View.VISIBLE)) {
            // This source timed out, try next one
            Log.d("PlayerActivity", "Source " + (currentSourceIndex + 1) + " timeout, trying next");
            tryNextSource();
        }
    }
    private void allSourcesFailed() {
        progressBar.setVisibility(View.GONE);
        errorText.setVisibility(View.VISIBLE);
        errorText.setText("All video sources failed.\nThe movie may not be available for streaming.");

        Toast.makeText(this,
                "All video sources failed. The movie may not be available for streaming.",
                Toast.LENGTH_LONG).show();

        // Auto-close after 5 seconds
        webView.postDelayed(this::finish, 5000);
    }

    private void tryNextSource() {
        if (!sourceFound) {
            currentSourceIndex++;
            webView.removeCallbacks(this::checkSourceTimeout);
            loadMovieWithFallback();
        }
    }

    private void checkPageSuccess(String url) {
        // Remove timeout since page loaded
        webView.removeCallbacks(this::checkSourceTimeout);

        if (sourceFound) {
            return;
        }

        // Check if video player is actually present
        webView.evaluateJavascript(
                "(function() { " +
                        "   var videos = document.getElementsByTagName('video'); " +
                        "   var iframes = document.getElementsByTagName('iframe'); " +
                        "   var hasPlayer = videos.length > 0 || iframes.length > 0; " +
                        "   var hasContent = document.body.innerText.length > 500; " +
                        "   var hasError = document.body.innerText.includes('404') || " +
                        "                  document.body.innerText.includes('Not Found') || " +
                        "                  document.body.innerText.includes('unavailable'); " +
                        "   return hasPlayer || (hasContent && !hasError); " +
                        "})();",
                value -> {
                    // If we already found a source during the async operation, return
                    if (sourceFound) {
                        return;
                    }
                    if ("false".equals(value)) {
                        // No video player detected, try next source
                        Log.d("PlayerActivity", "No video player detected, trying next source");
                        tryNextSource();
                    } else {// SUCCESS! Source found
                        sourceFound = true;
                        Log.d("PlayerActivity", "Video source " + (currentSourceIndex + 1) + " loaded successfully - STOPPING SEARCH");
                    }
                    // Update UI to show final source
                    if (movieTitle != null) {
                        title.setText(movieTitle);
                    }
                }
        );
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        webView.pauseTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        webView.resumeTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }

}