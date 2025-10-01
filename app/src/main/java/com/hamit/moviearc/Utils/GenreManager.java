package com.hamit.moviearc.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hamit.moviearc.Network.Data.GenreResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenreManager {
    private static final String PREFS_NAME = "genre_prefs";
    private static final String GENRES_KEY = "movie_genres";
    private static final long CACHE_DURATION = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds
    private static final String LAST_UPDATE_KEY = "last_genre_update";

    private static Map<Integer, String> genreMap = new HashMap<>();
    private static boolean isInitialized = false;

    // Initialize with default genres as fallback
    static {
        initializeDefaultGenres();
    }

    private static void initializeDefaultGenres() {
        genreMap.put(28, "Action");
        genreMap.put(12, "Adventure");
        genreMap.put(16, "Animation");
        genreMap.put(35, "Comedy");
        genreMap.put(80, "Crime");
        genreMap.put(99, "Documentary");
        genreMap.put(18, "Drama");
        genreMap.put(10751, "Family");
        genreMap.put(14, "Fantasy");
        genreMap.put(36, "History");
        genreMap.put(27, "Horror");
        genreMap.put(10402, "Music");
        genreMap.put(9648, "Mystery");
        genreMap.put(10749, "Romance");
        genreMap.put(878, "Science Fiction");
        genreMap.put(10770, "TV Movie");
        genreMap.put(53, "Thriller");
        genreMap.put(10752, "War");
        genreMap.put(37, "Western");
    }

    public static void updateGenres(GenreResponse genreResponse) {
        if (genreResponse != null && genreResponse.getGenres() != null) {
            List<GenreResponse.Genre> genres = genreResponse.getGenres();
            genreMap.clear();
            for (GenreResponse.Genre genre : genres) {
                genreMap.put(genre.getId(), genre.getName());
            }
            isInitialized = true;
            Log.d("GenreManager", "Updated genres with " + genreMap.size() + " items");
        }
    }

    public static String getGenreName(int genreId) {
        String genreName = genreMap.get(genreId);
        if (genreName == null) {
            Log.w("GenreManager", "Unknown genre ID: " + genreId);
            return "Unknown";
        }
        return genreName;
    }

    public static String getGenreNames(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return "Unknown";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(genreIds.size(), 2); i++) {
            String genreName = getGenreName(genreIds.get(i));
            if (!"Unknown".equals(genreName)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(genreName);
            }
        }

        return sb.length() > 0 ? sb.toString() : "Unknown";
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    // Cache management methods
    public static void saveGenresToCache(Context context, GenreResponse genreResponse) {
        if (genreResponse == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String genresJson = gson.toJson(genreResponse);

        editor.putString(GENRES_KEY, genresJson);
        editor.putLong(LAST_UPDATE_KEY, System.currentTimeMillis());
        editor.apply();

        updateGenres(genreResponse);
        Log.d("GenreManager", "Genres saved to cache");
    }

    public static boolean loadGenresFromCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        long lastUpdate = prefs.getLong(LAST_UPDATE_KEY, 0);
        if (System.currentTimeMillis() - lastUpdate > CACHE_DURATION) {
            Log.d("GenreManager", "Cache expired");
            return false; // Cache expired
        }

        String genresJson = prefs.getString(GENRES_KEY, null);
        if (genresJson == null) {
            return false;
        }

        try {
            Gson gson = new Gson();
            GenreResponse genreResponse = gson.fromJson(genresJson, GenreResponse.class);

            updateGenres(genreResponse);
            Log.d("GenreManager", "Genres loaded from cache: " + genreMap.size());
            return true;
        } catch (Exception e) {
            Log.e("GenreManager", "Error loading genres from cache", e);
            return false;
        }
    }

    public static int getGenreId(String genreName) {
        for (Map.Entry<Integer, String> entry : genreMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(genreName)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static List<String> getAllGenreNames() {
        return new ArrayList<>(genreMap.values());
    }

    // Get all genres as map for debugging or other uses
    public static Map<Integer, String> getGenreMap() {
        return new HashMap<>(genreMap);
    }
}