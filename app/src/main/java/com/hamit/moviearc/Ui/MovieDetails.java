package com.hamit.moviearc.Ui;

import static com.hamit.moviearc.Fragments.HomeFragment.HomeFragment.API_KEY;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_BASE_URL;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_SIZE_W500;

import android.os.Bundle;
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

    private ImageView btnBack, backdropImage;
    private TextView movieTitle, movieRating, movieRelease, movieSynopsis;
    private MultiSearchResponse.ResultItem resultItem;

    private YouTubePlayerView youTubePlayerView;
    private Video trailer;
    private LinearLayout trailerBtn, watchBtn;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
        }
    }


}