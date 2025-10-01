package com.hamit.moviearc.Ui;

import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_BASE_URL;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_SIZE_W342;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.R;
import com.hamit.moviearc.Utils.GenreManager;

import java.util.List;

public class MovieDetails extends AppCompatActivity {
    private Movie movie;
    private ChipGroup genreChipGroup;

    private ImageView btnBack, backdropImage;
    private TextView movieTitle, movieRating, movieRelease, movieSynopsis;

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

        // buttons
        btnBack = findViewById(R.id.btn_exit);
        backdropImage = findViewById(R.id.backdropImage);
        movieTitle = findViewById(R.id.movieTitle);
        movieRating = findViewById(R.id.movieRating);
        movieRelease = findViewById(R.id.movieRelease);
        movieSynopsis = findViewById(R.id.movieSynopsis);
        genreChipGroup= findViewById(R.id.genreChips);



        movie = getIntent().getParcelableExtra("movie_data");

        if (movie != null) {
            displayMovieData(movie);

        } else {
            int movieId = getIntent().getIntExtra("movie_id", -1);
            if (movieId != -1) {
                // movie id to fetch data from the api if no data available
                fetchMovieDetails(movieId);
            }
        }

        btnBack.setOnClickListener(v -> {
            // close the activity
            finish();
        });
    }

    private void fetchMovieDetails(int movieId) {
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
            String backdropUrl = IMAGE_BASE_URL + IMAGE_SIZE_W342 + movie.getBackdropPath();
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


}