package com.hamit.moviearc.HelperClasses;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.hamit.moviearc.Network.Data.Movie;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Add movie to liked movies
    public void addToLikedMovies(Movie movie, FirestoreCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Convert Movie object to Map for Firestore
            Map<String, Object> movieData = convertMovieToMap(movie);
            movieData.put("timestamp", FieldValue.serverTimestamp());

            db.collection("users")
                    .document(user.getUid())
                    .collection("liked_movies")
                    .document(String.valueOf(movie.getId())) // Using movie ID as document ID
                    .set(movieData)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Remove movie from liked movies
    public void removeFromLikedMovies(int movieId, FirestoreCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("liked_movies")
                    .document(String.valueOf(movieId))
                    .delete()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Add movie to wishlist
    public void addToWishlist(Movie movie, FirestoreCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Map<String, Object> movieData = convertMovieToMap(movie);
            movieData.put("timestamp", FieldValue.serverTimestamp());

            db.collection("users")
                    .document(user.getUid())
                    .collection("wishlist")
                    .document(String.valueOf(movie.getId()))
                    .set(movieData)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Remove movie from wishlist
    public void removeFromWishlist(int movieId, FirestoreCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("wishlist")
                    .document(String.valueOf(movieId))
                    .delete()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // ===== WATCHLIST SECTION =====

    // Add movie to watchlist
    public void addToWatchlist(Movie movie, FirestoreCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Map<String, Object> movieData = convertMovieToMap(movie);
            movieData.put("timestamp", FieldValue.serverTimestamp());

            db.collection("users")
                    .document(user.getUid())
                    .collection("watchlist")
                    .document(String.valueOf(movie.getId()))
                    .set(movieData)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Remove movie from watchlist
    public void removeFromWatchlist(int movieId, FirestoreCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("watchlist")
                    .document(String.valueOf(movieId))
                    .delete()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Check if movie is liked
    public void isMovieLiked(int movieId, LikedCheckCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("liked_movies")
                    .document(String.valueOf(movieId))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        callback.onResult(documentSnapshot.exists());
                    })
                    .addOnFailureListener(e -> callback.onResult(false));
        } else {
            callback.onResult(false);
        }
    }

    // Check if movie is in wishlist
    public void isMovieInWishlist(int movieId, LikedCheckCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("wishlist")
                    .document(String.valueOf(movieId))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        callback.onResult(documentSnapshot.exists());
                    })
                    .addOnFailureListener(e -> callback.onResult(false));
        } else {
            callback.onResult(false);
        }
    }

    // Check if movie is in watchlist
    public void isMovieInWatchlist(int movieId, LikedCheckCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("watchlist")
                    .document(String.valueOf(movieId))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        callback.onResult(documentSnapshot.exists());
                    })
                    .addOnFailureListener(e -> callback.onResult(false));
        } else {
            callback.onResult(false);
        }
    }

    // ===== RETRIEVAL METHODS =====

    // Get all liked movies
    public void getLikedMovies(CollectionCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("liked_movies")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        callback.onSuccess(queryDocumentSnapshots);
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Get all wishlist movies
    public void getWishlist(CollectionCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("wishlist")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        callback.onSuccess(queryDocumentSnapshots);
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Get all watchlist movies
    public void getWatchlist(CollectionCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("watchlist")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        callback.onSuccess(queryDocumentSnapshots);
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    public void getLikedMoviesCount(CountCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("liked_movies")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        callback.onSuccess(queryDocumentSnapshots.size());
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Get wishlist count
    public void getWishlistCount(CountCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("wishlist")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        callback.onSuccess(queryDocumentSnapshots.size());
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Get watchlist count
    public void getWatchlistCount(CountCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("watchlist")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        callback.onSuccess(queryDocumentSnapshots.size());
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            callback.onFailure("User not authenticated");
        }
    }

    // Convert Movie object to Map for Firestore
    private Map<String, Object> convertMovieToMap(Movie movie) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", movie.getId());
        map.put("title", movie.getTitle());
        map.put("originalTitle", movie.getOriginalTitle());
        map.put("overview", movie.getOverview());
        map.put("posterPath", movie.getPosterPath());
        map.put("backdropPath", movie.getBackdropPath());
        map.put("releaseDate", movie.getReleaseDate());
        map.put("voteAverage", movie.getVoteAverage());
        map.put("voteCount", movie.getVoteCount());
        map.put("popularity", movie.getPopularity());
        map.put("adult", movie.isAdult());
        map.put("video", movie.isVideo());
        map.put("mediaType", movie.getMediaType());
        map.put("originalLanguage", movie.getOriginalLanguage());
        map.put("genreIds", movie.getGenreIds());
        return map;
    }

    // Callback interfaces
    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface LikedCheckCallback {
        void onResult(boolean exists);
    }

    // New callback for collection retrieval
    public interface CollectionCallback {
        void onSuccess(QuerySnapshot queryDocumentSnapshots);
        void onFailure(String error);
    }

    public interface CountCallback {
        void onSuccess(int count);
        void onFailure(String error);
    }
}