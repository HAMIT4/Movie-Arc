package com.hamit.moviearc.Ui;

import static com.hamit.moviearc.Fragments.HomeFragment.HomeFragment.API_KEY;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_BASE_URL;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_SIZE_W500;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.hamit.moviearc.Adapters.PopularRecycler;
import com.hamit.moviearc.HelperClasses.FirestoreHelper;
import com.hamit.moviearc.MainActivity;
import com.hamit.moviearc.Network.Clients.RetrofitClient;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.Network.Data.MovieResponse;
import com.hamit.moviearc.Network.Data.MultiSearchResponse;
import com.hamit.moviearc.Network.Data.Video;
import com.hamit.moviearc.Network.Data.VideoResponse;
import com.hamit.moviearc.Network.Services.TmdbService;
import com.hamit.moviearc.R;
import com.hamit.moviearc.Utils.GenreManager;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetails extends AppCompatActivity {
    private Movie movie;
    private ChipGroup genreChipGroup;
    private TmdbService mdbService;
    private PopularRecycler similarMoviesAdapter;
    private PopularRecycler recommendedMoviesAdapter;
    private RecyclerView similarMoviesRecycler;
    private RecyclerView recommendedMovies;

    private ImageView btnBack, backdropImage, heartIcon, bookmarkIcon, watchIcon;
    private TextView movieTitle, movieRating, movieRelease, movieSynopsis;
    private MultiSearchResponse.ResultItem resultItem;

    private YouTubePlayerView youTubePlayerView;
    private Video trailer;
    private LinearLayout trailerBtn, watchBtn, btnFavorite, btnWishlist, btnWatchList;

    private FirestoreHelper firestoreHelper;
    private boolean isLiked= false;
    private boolean isInWishlist= false;
    private boolean isInWatchList= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mdbService= RetrofitClient.getTmdbService();
        firestoreHelper= new FirestoreHelper();

        // initialize views
        btnBack = findViewById(R.id.btn_exit);
        backdropImage = findViewById(R.id.backdropImage);
        movieTitle = findViewById(R.id.movieTitle);
        movieRating = findViewById(R.id.movieRating);
        movieRelease = findViewById(R.id.movieRelease);
        movieSynopsis = findViewById(R.id.movieSynopsis);
        genreChipGroup= findViewById(R.id.genreChips);
        similarMoviesRecycler = findViewById(R.id.similarMoviesRecycler);
        recommendedMovies = findViewById(R.id.recommendedMoviesRecycler);
        trailerBtn= findViewById(R.id.trailer_btn);
        watchBtn= findViewById(R.id.watch_btn);
        youTubePlayerView = findViewById(R.id.youtubePlayerView);
        btnFavorite= findViewById(R.id.btn_favorite);
        btnWishlist= findViewById(R.id.btn_wishlist);
        heartIcon= findViewById(R.id.heart_icon);
        bookmarkIcon= findViewById(R.id.bookmark_icon);
        btnWatchList= findViewById(R.id.btn_watchlist);
        watchIcon= findViewById(R.id.watch_icon);

        similarMoviesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // initialize youtube player
        getLifecycle().addObserver(youTubePlayerView);



        movie = getIntent().getParcelableExtra("movie_data");

        if(movie != null){
            displayMovieData(movie);
            fetchSimilarMovies();
            fetchRecommendedMovies();
            fetchMovieTrailer();
        }else if (getIntent().hasExtra("result_item")) {
            resultItem = getIntent().getParcelableExtra("result_item");
            if (resultItem != null) {
                displayResultItemData(resultItem);
                // Convert ResultItem to Movie or use its ID for API calls
                if (resultItem.getMediaType() != null && resultItem.getMediaType().equals("movie")) {
                    fetchSimilarMoviesForResultItem();
                    fetchRecommendedMoviesForResultItem();
                    fetchMovieTrailer();
                }
            }
        }else {
            int movieId = getIntent().getIntExtra("movie_id", -1);
            if (movieId != -1) {
                fetchMovieDetails(movieId);
            } else {
                Toast.makeText(this, "No movie data available", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        btnBack.setOnClickListener(v -> {
            // close the activity
            finish();
        });

        // now, now we watch some movies
        watchBtn.setOnClickListener(v->{
            // openMoviePlayer();
            // here we try to download drama player
            openSimplePlayer();

        });

        setupFavoriteButton();
        setupWishlistButton();
        setupWatchListButton();

        // Check initial states after movie data is loaded
        if (movie != null || resultItem != null) {
            checkInitialStates();
        }

    }

    public void setupWatchListButton() {
        btnWatchList.setOnClickListener(v->{
            int movieId = getCurrentMovieId();
            Movie currentMovie = getCurrentMovie();

            if (currentMovie == null || movieId == -1) {
                Toast.makeText(this, "Movie data not available", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isInWatchList) {
                // remove from watchlist
                firestoreHelper.removeFromWatchlist(movieId, new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        isInWatchList = false;
                        updateWatchlistButton();
                        Toast.makeText(MovieDetails.this, "Removed from Watchlist", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(MovieDetails.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // add to watchlist
                firestoreHelper.addToWatchlist(currentMovie, new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        isInWatchList= true;
                        updateWatchlistButton();
                        Toast.makeText(MovieDetails.this, "Added to Watchlist", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(MovieDetails.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });
    }

    public void updateWatchlistButton(){
        if(isInWatchList){
            btnWatchList.setBackgroundResource(R.drawable.red_bg);
            watchIcon.setImageResource(R.drawable.watchlist_true_com);
        }else {
            btnWatchList.setBackgroundResource(R.drawable.light_dark_bg);
            watchIcon.setImageResource(R.drawable.watchlist_default_com);
        }
    }

    private void checkInitialStates() {
        int movieId = getCurrentMovieId();
        if (movieId == -1) return;
        // Check if movie is liked
        firestoreHelper.isMovieLiked(movieId, isLiked -> {
            this.isLiked = isLiked;
            updateFavoriteButton();
        });

        // Check if movie is in wishlist
        firestoreHelper.isMovieInWishlist(movieId, inWishlist -> {
            this.isInWishlist = inWishlist;
            updateWishlistButton();
        });
        // check if movie is in watchlist
        firestoreHelper.isMovieInWatchlist(movieId, isInWatchList ->{
            this.isInWatchList = isInWatchList;
            updateWatchlistButton();
        });

    }

    private void setupWishlistButton() {
        btnWishlist.setOnClickListener(v -> {
            int movieId = getCurrentMovieId();
            Movie currentMovie = getCurrentMovie();

            if (currentMovie == null || movieId == -1) {
                Toast.makeText(this, "Movie data not available", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isInWishlist) {
                // Remove from wishlist
                firestoreHelper.removeFromWishlist(movieId, new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        isInWishlist = false;
                        updateWishlistButton();
                        Toast.makeText(MovieDetails.this, "Removed from watchlist", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(MovieDetails.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Add to wishlist
                firestoreHelper.addToWishlist(currentMovie, new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        isInWishlist = true;
                        updateWishlistButton();
                        Toast.makeText(MovieDetails.this, "Added to watchlist", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(MovieDetails.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void setupFavoriteButton() {
        btnFavorite.setOnClickListener(v -> {
            int movieId = movie != null ? movie.getId() : (resultItem != null ? resultItem.getId() : -1);
            Movie currentMovie = getCurrentMovie();

            if (currentMovie == null || movieId == -1) {
                Toast.makeText(this, "Movie data not available", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLiked) {
                // Remove from favorites
                firestoreHelper.removeFromLikedMovies(movieId, new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        isLiked = false;
                        updateFavoriteButton();
                        Toast.makeText(MovieDetails.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(MovieDetails.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Add to favorites
                firestoreHelper.addToLikedMovies(currentMovie, new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        isLiked = true;
                        updateFavoriteButton();
                        Toast.makeText(MovieDetails.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(MovieDetails.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void updateFavoriteButton() {
        if (isLiked) {
            heartIcon.setImageResource(R.drawable.heart_red_com);
            btnFavorite.setBackgroundResource(R.drawable.red_bg);
        } else {
            heartIcon.setImageResource(R.drawable.heart_com);
            btnFavorite.setBackgroundResource(R.drawable.light_dark_bg);
        }
    }
    private void updateWishlistButton() {
        if (isInWishlist) {
            bookmarkIcon.setImageResource(R.drawable.bookmark_blue_com);
            btnWishlist.setBackgroundResource(R.drawable.red_bg);
        } else {
            bookmarkIcon.setImageResource(R.drawable.bookmark_com);
            btnWishlist.setBackgroundResource(R.drawable.light_dark_bg);
        }
    }

    private void fetchMovieTrailer() {
        int movieId= movie !=null ? movie.getId() : (resultItem != null ? resultItem.getId() : -1);
        if(movieId == -1) return;

        // make api call for trailer video
        Call<VideoResponse> call = mdbService.getMovieVideos(movieId, API_KEY);
        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    trailer= response.body().getFirstTrailer();

                    if (trailer != null && "YouTube".equalsIgnoreCase(trailer.getSite())){
                        setupTrailerPlayer(trailer.getKey());
                    } else {
                        trailerBtn.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                trailerBtn.setVisibility(View.GONE);
            }
        });

    }

    // setup youtube to play Trailer
    private void setupTrailerPlayer(String youtubeVideoId){
        trailerBtn.setVisibility(View.VISIBLE);

        trailerBtn.setOnClickListener(v -> {
            youTubePlayerView.setVisibility(View.VISIBLE);
            trailerBtn.setVisibility(View.GONE);

            youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer -> {
                youTubePlayer.loadVideo(youtubeVideoId, 0);
                youTubePlayer.play();
            });

            //  load an play the video
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {

                @Override
                public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                    super.onStateChange(youTubePlayer, state);
                    // When video ends, show the trailer button again
                    if (state == PlayerConstants.PlayerState.ENDED) {
                        runOnUiThread(() -> {
                            youTubePlayerView.setVisibility(View.GONE);
                            trailerBtn.setVisibility(View.VISIBLE);
                            if (watchBtn != null) {
                                watchBtn.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    if (state == PlayerConstants.PlayerState.UNKNOWN) {
                        runOnUiThread(() -> {
                            Toast.makeText(MovieDetails.this, "Playback error. Please try again.", Toast.LENGTH_SHORT).show();
                            youTubePlayerView.setVisibility(View.GONE);
                            trailerBtn.setVisibility(View.VISIBLE);
                        });
                    }
                }

            });

        });
    }

    private void fetchRecommendedMovies() {
        Call<MovieResponse> call = mdbService.getMovieRecommendations(movie.getId(), API_KEY, 1);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Movie> movies= response.body().getResults();
                    // feed to our recycler
                    recommendedMoviesAdapter = new PopularRecycler(movies);
                    recommendedMovies.setAdapter(recommendedMoviesAdapter);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MovieDetails.this, "Failed to get Recommended Movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSimilarMovies() {
        Call<MovieResponse> call = mdbService.getSimilarMovies(movie.getId(), API_KEY, 1);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Movie> movies= response.body().getResults();
                    // feed to our recycler
                    similarMoviesAdapter = new PopularRecycler(movies);
                    similarMoviesRecycler.setAdapter(similarMoviesAdapter);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                // toast to detect error
                Toast.makeText(MovieDetails.this, "Failed to get SimilarMovies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMovieDetails(int movieId) {
        Call<Movie> call = mdbService.getMovieDetails(movieId, API_KEY, "videos,credits,similar,recommendations");
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful()){
                    List<Movie> movies= Collections.singletonList(response.body());
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(MovieDetails.this, "Failed to get Data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMovieData(Movie movie){
        movieTitle.setText(movie.getTitle());
        movieSynopsis.setText(movie.getOverview());
        double rating = movie.getVoteAverage();
        movieRating.setText(String.format("%.1f", rating));
        String releaseYear = movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4 ?
                movie.getReleaseDate().substring(0, 4) : "N/A";
        movieRelease.setText(releaseYear);

        // Load backdrop image
        if (movie.getBackdropPath() != null && !movie.getBackdropPath().isEmpty()) {
            String backdropUrl = IMAGE_BASE_URL + IMAGE_SIZE_W500 + movie.getBackdropPath();
            Glide.with(this)
                    .load(backdropUrl)
                    .placeholder(R.drawable.loading_landscape)
                    .error(R.drawable.loading_error)
                    .into(backdropImage);
        }
        setupGenreChips(movie.getGenreIds());

    }

    private void setupGenreChips(List<Integer> genreIds) {
        genreChipGroup.removeAllViews(); // Clear existing chips

        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }

        for (Integer genreId : genreIds) {
            String genreName = GenreManager.getGenreName(genreId);

            // Create chip
            Chip chip = new Chip(this);
            chip.setText(genreName);
            chip.setChipBackgroundColorResource(R.color.chip_text);
            chip.setTextColor(getColor(R.color.white));
            chip.setChipStrokeWidth(2f);
            chip.setChipStrokeColorResource(R.color.chip_bg);

            genreChipGroup.addView(chip);
        }
    }

    // additions top test result view data
    private void displayResultItemData(MultiSearchResponse.ResultItem resultItem) {
        // Use helper methods from ResultItem class
        movieTitle.setText(resultItem.getDisplayTitle());
        movieSynopsis.setText(resultItem.getOverview());

        double rating = resultItem.getVoteAverage();
        movieRating.setText(String.format("%.1f", rating));

        String releaseYear = resultItem.getDisplayDate() != null && resultItem.getDisplayDate().length() >= 4 ?
                resultItem.getDisplayDate().substring(0, 4) : "N/A";
        movieRelease.setText(releaseYear);

        // Load backdrop image
        if (resultItem.getBackdropPath() != null && !resultItem.getBackdropPath().isEmpty()) {
            String backdropUrl = IMAGE_BASE_URL + IMAGE_SIZE_W500 + resultItem.getBackdropPath();
            Glide.with(this)
                    .load(backdropUrl)
                    .placeholder(R.drawable.loading_landscape)
                    .error(R.drawable.loading_error)
                    .into(backdropImage);
        }

        // Setup genre chips
        setupGenreChips(resultItem.getGenreIds());
    }

    private void fetchSimilarMoviesForResultItem() {
        if (resultItem == null || !"movie".equals(resultItem.getMediaType())) return;

        Call<MovieResponse> call = mdbService.getSimilarMovies(resultItem.getId(), API_KEY, 1);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Movie> movies = response.body().getResults();
                    similarMoviesAdapter = new PopularRecycler(movies);
                    similarMoviesRecycler.setAdapter(similarMoviesAdapter);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MovieDetails.this, "Failed to get Similar Movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecommendedMoviesForResultItem() {
        if (resultItem == null || !"movie".equals(resultItem.getMediaType())) return;

        Call<MovieResponse> call = mdbService.getMovieRecommendations(resultItem.getId(), API_KEY, 1);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Movie> movies = response.body().getResults();
                    recommendedMoviesAdapter = new PopularRecycler(movies);
                    recommendedMovies.setAdapter(recommendedMoviesAdapter);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MovieDetails.this, "Failed to get Recommended Movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMoviePlayer(){
        int movieId= movie !=null ? movie.getId() : (resultItem != null ? resultItem.getId() : -1);

        String movieTitle= movie !=null ? movie.getTitle() : (resultItem != null ? resultItem.getDisplayTitle() : "Movie");

        if (movieId == -1){
            Toast.makeText(this, "Movie not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // send data to movie player
        Intent intent= new Intent(this, PlayerActivity.class);
        intent.putExtra("movieTitle", movieTitle);
        intent.putExtra("tmdbId", movieId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
        }
    }

    // helper methods
    // Helper method to get current movie ID
    private int getCurrentMovieId() {
        if (movie != null) {
            return movie.getId();
        } else if (resultItem != null) {
            return resultItem.getId();
        }
        return -1;
    }

    // Helper method to get current Movie object
    private Movie getCurrentMovie() {
        if (movie != null) {
            return movie;
        } else if (resultItem != null && "movie".equals(resultItem.getMediaType())) {
            // Convert ResultItem to Movie object
            return convertResultItemToMovie(resultItem);
        }
        return null;
    }
    // Convert ResultItem to Movie object for Firestore
    private Movie convertResultItemToMovie(MultiSearchResponse.ResultItem resultItem) {
        Movie movie = new Movie();
        movie.setId(resultItem.getId());
        movie.setTitle(resultItem.getTitle() != null ? resultItem.getTitle() : resultItem.getName());
        movie.setOriginalTitle(resultItem.getOriginalTitle() != null ? resultItem.getOriginalTitle() : resultItem.getOriginalName());
        movie.setOverview(resultItem.getOverview());
        movie.setPosterPath(resultItem.getPosterPath());
        movie.setBackdropPath(resultItem.getBackdropPath());
        movie.setReleaseDate(resultItem.getReleaseDate() != null ? resultItem.getReleaseDate() : resultItem.getFirstAirDate());
        movie.setVoteAverage(resultItem.getVoteAverage());
        movie.setVoteCount(resultItem.getVoteCount());
        movie.setPopularity(resultItem.getPopularity());
        movie.setMediaType(resultItem.getMediaType());
        movie.setGenreIds(resultItem.getGenreIds());
        // Set default values for required fields
        movie.setAdult(false);
        movie.setVideo(false);
        movie.setOriginalLanguage(resultItem.getOriginalLanguage() != null ? resultItem.getOriginalLanguage() : "en");
        return movie;
    }









    // this code might make or break

    private void openSimplePlayer() {
        String packageName = "com.drama.simpleplayer";
        String downloadUrl = "https://dw.uptodown.net/dwn/UtcNUeFrmEi9M75Qcyo9gCVS_dFqnp7YmttCcolfzCMu7QNGY-QNAbIXwNzuhCN2APMuY6HnfYqxxNHI95pJO-rtsCVbZeaC2gejPTzBYSDQ-AhKfsI-LOquekkyRPmp/3wsjtC8vl5n_Ka7ZSSDcJVfC7Iv2elNKiNxMR3bAHXiQ6fm1ghDGyP2YdErX8RnG-6IdBIHSYm-nyaZUH04XwSGdTc58khBpPD9O8mXFkrju4CrVMgMlLZ55-oimbVfA/8SNupDZxPjVPmMBTKeb_HnT8T43YByHrW22V6V65xmljhTo2ZQErhvbfTcD2NUHDHosPqJqisCOcse2-gjenTw==/drama-player-1-0-5.apk";

        if (isAppInstalled(packageName)) {
            // App is installed, open it
            openApp(packageName);
        } else {
            // App not installed, download and install
            downloadAndInstallDramaPlayer(downloadUrl);
        }
    }

    // Check if app is installed
    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Open the app if installed
    private void openApp(String packageName) {
        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(this, "Could not open Drama Player", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening Drama Player", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Download and install Drama Player
    private void downloadAndInstallDramaPlayer(String downloadUrl) {
        // Check for install permission (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                // Request install permission
                requestInstallPermission();
                return;
            }
        }

        // Show confirmation dialog
        showDownloadConfirmationDialog(downloadUrl);
    }

    // Show download confirmation dialog
    private void showDownloadConfirmationDialog(String downloadUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Install Drama Player");
        builder.setMessage("Drama Player is required to watch movies. Do you want to download and install it now?");

        builder.setPositiveButton("Download", (dialog, which) -> {
            startDownload(downloadUrl);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    // Start the download process
    private void startDownload(String downloadUrl) {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading Drama Player");
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.show();

        // Use DownloadManager for reliable download
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        // Create download request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));

        // Set download details
        request.setTitle("Drama Player");
        request.setDescription("Downloading Drama Player app");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Set destination
        String fileName = "DramaPlayer-" + System.currentTimeMillis() + ".apk";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // Enqueue download
        long downloadId = downloadManager.enqueue(request);

        // Monitor download progress
        monitorDownloadProgress(downloadId, downloadManager, progressDialog);
    }

    // Monitor download progress
    private void monitorDownloadProgress(long downloadId, DownloadManager downloadManager, ProgressDialog progressDialog) {
        new Thread(() -> {
            boolean downloading = true;

            while (downloading) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);

                try (Cursor cursor = downloadManager.query(query)) {
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                        switch (status) {
                            case DownloadManager.STATUS_SUCCESSFUL:
                                downloading = false;
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    installDownloadedApk(downloadId);
                                });
                                break;

                            case DownloadManager.STATUS_FAILED:
                                downloading = false;
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_SHORT).show();
                                });
                                break;

                            case DownloadManager.STATUS_RUNNING:
                                // Update progress
                                @SuppressLint("Range") long bytesDownloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                @SuppressLint("Range") long bytesTotal = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                                if (bytesTotal > 0) {
                                    int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                                    runOnUiThread(() -> progressDialog.setProgress(progress));
                                }
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Install the downloaded APK
    private void installDownloadedApk(long downloadId) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri apkUri = downloadManager.getUriForDownloadedFile(downloadId);

        if (apkUri != null) {
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(installIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Error installing app. Please enable 'Install from unknown sources'.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Downloaded file not found", Toast.LENGTH_SHORT).show();
        }
    }

    // Request install permission for Android 8.0+
    private void requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Install Permission Required");
            builder.setMessage("This app needs permission to install other apps. Please grant the permission to continue.");

            builder.setPositiveButton("Grant Permission", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, INSTALL_PERMISSION_REQUEST_CODE);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });

            builder.show();
        }
    }

    // Handle permission result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INSTALL_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getPackageManager().canRequestPackageInstalls()) {
                    // Permission granted, restart download process
                    String downloadUrl = "https://dw.uptodown.net/dwn/UtcNUeFrmEi9M75Qcyo9gCVS_dFqnp7YmttCcolfzCMu7QNGY-QNAbIXwNzuhCN2APMuY6HnfYqxxNHI95pJO-rtsCVbZeaC2gejPTzBYSDQ-AhKfsI-LOquekkyRPmp/3wsjtC8vl5n_Ka7ZSSDcJVfC7Iv2elNKiNxMR3bAHXiQ6fm1ghDGyP2YdErX8RnG-6IdBIHSYm-nyaZUH04XwSGdTc58khBpPD9O8mXFkrju4CrVMgMlLZ55-oimbVfA/8SNupDZxPjVPmMBTKeb_HnT8T43YByHrW22V6V65xmljhTo2ZQErhvbfTcD2NUHDHosPqJqisCOcse2-gjenTw==/drama-player-1-0-5.apk";
                    showDownloadConfirmationDialog(downloadUrl);
                } else {
                    Toast.makeText(this, "Permission denied. Cannot install Drama Player.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Define the permission request code
    private static final int INSTALL_PERMISSION_REQUEST_CODE = 1001;

}