package com.hamit.moviearc.HelperClasses;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MoviePlayerHelper {
    private static final String TAG = "MoviePlayerHelper";

    public interface VideoLinkCallback {
        void onVideoLinkFound(String videoUrl);
        void onAllSourcesFailed();
    }

    private final Context context;
    private final VideoLinkCallback callback;
    private final int tmdbId;
    private final String movieTitle;
    private final Handler handler = new Handler();
    private int currentSourceIndex = 0;
    private boolean sourceFound = false;

    // Updated sources that provide direct video streams or APIs
    private static final VideoSource[] VIDEO_SOURCES = {
            // VidSrc API endpoint
            new VideoSource("https://vidsrc.xyz/embed/movie/" + "TMDB_ID", SourceType.API),
            // 2embed with different structure
            new VideoSource("https://www.2embed.cc/embed/" + "TMDB_ID", SourceType.IFRAME),
            // Alternative sources
            new VideoSource("https://vidsrc.to/embed/movie/" + "TMDB_ID", SourceType.IFRAME),
            new VideoSource("https://multiembed.mov/?video_id=" + "TMDB_ID&tmdb=1", SourceType.IFRAME)
    };

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    enum SourceType {
        API,        // Direct API that returns JSON
        IFRAME,     // Embed page that needs parsing
        DIRECT      // Direct video URL
    }

    static class VideoSource {
        String url;
        SourceType type;

        VideoSource(String url, SourceType type) {
            this.url = url;
            this.type = type;
        }
    }

    public MoviePlayerHelper(Context context, int tmdbId, String movieTitle, VideoLinkCallback callback) {
        this.context = context;
        this.tmdbId = tmdbId;
        this.movieTitle = movieTitle;
        this.callback = callback;

        tryNextSource();
    }

    private void tryNextSource() {
        if (sourceFound) return;

        if (currentSourceIndex >= VIDEO_SOURCES.length) {
            handler.post(() -> callback.onAllSourcesFailed());
            return;
        }

        VideoSource source = VIDEO_SOURCES[currentSourceIndex];
        String url = source.url.replace("TMDB_ID", String.valueOf(tmdbId));

        Log.d(TAG, "Trying source " + (currentSourceIndex + 1) + ": " + url);

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "https://www.google.com/")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Source failed: " + url + " - " + e.getMessage());
                nextSourceDelayed();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        Log.d(TAG, "HTTP error: " + response.code());
                        nextSourceDelayed();
                        return;
                    }

                    String body = response.body() != null ? response.body().string() : "";
                    String videoLink = null;

                    switch (source.type) {
                        case API:
                            videoLink = extractFromAPI(body);
                            break;
                        case IFRAME:
                            videoLink = extractFromHTML(body);
                            break;
                        case DIRECT:
                            videoLink = url;
                            break;
                    }

                    if (videoLink != null && isValidVideoUrl(videoLink)) {
                        sourceFound = true;
                        String finalLink = videoLink;
                        Log.d(TAG, "✓ Video found: " + finalLink);
                        handler.post(() -> callback.onVideoLinkFound(finalLink));
                    } else {
                        Log.d(TAG, "✗ No valid video link found");
                        nextSourceDelayed();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response: " + e.getMessage());
                    nextSourceDelayed();
                }
            }
        });
    }

    private void nextSourceDelayed() {
        if (!sourceFound) {
            currentSourceIndex++;
            handler.postDelayed(this::tryNextSource, 800);
        }
    }

    /**
     * Extract video URL from API JSON response
     */
    private String extractFromAPI(String json) {
        try {
            JSONObject obj = new JSONObject(json);

            // Try different JSON structures
            if (obj.has("stream")) {
                return obj.getString("stream");
            }
            if (obj.has("url")) {
                return obj.getString("url");
            }
            if (obj.has("sources")) {
                JSONArray sources = obj.getJSONArray("sources");
                if (sources.length() > 0) {
                    return sources.getJSONObject(0).getString("file");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON parse error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Extract video URL from HTML/JavaScript
     */
    private String extractFromHTML(String html) {
        if (html == null || html.isEmpty()) return null;

        // Pattern 1: Direct .mp4/.m3u8 URLs
        String[] patterns = {
                "https?://[^\"'\\s<>]+\\.m3u8[^\"'\\s<>]*",
                "https?://[^\"'\\s<>]+\\.mp4[^\"'\\s<>]*",
                "\"file\"\\s*:\\s*\"([^\"]+)\"",
                "'file'\\s*:\\s*'([^']+)'",
                "source:\\s*\"([^\"]+)\"",
                "src:\\s*\"([^\"]+\\.(?:m3u8|mp4))\""
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(html);
            if (m.find()) {
                String url = m.group(m.groupCount() > 0 ? 1 : 0);
                // Clean up escaped characters
                url = url.replace("\\", "").replace("\\/", "/");
                if (isValidVideoUrl(url)) {
                    return url;
                }
            }
        }

        return null;
    }

    /**
     * Validate if URL is a proper video URL
     */
    private boolean isValidVideoUrl(String url) {
        if (url == null || url.isEmpty()) return false;

        // Must be HTTP(S)
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }

        // Should end with video extension or be a streaming URL
        return url.contains(".m3u8") ||
                url.contains(".mp4") ||
                url.contains("/stream") ||
                url.contains("googlevideo.com") ||
                url.contains("master.m3u8");
    }
}