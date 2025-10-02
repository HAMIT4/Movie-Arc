package com.hamit.moviearc.Adapters;

import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_BASE_URL;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_SIZE_W500;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.R;
import com.hamit.moviearc.Ui.MovieDetails;
import com.hamit.moviearc.Utils.GenreManager;

import java.util.List;

public class ImageSlider extends RecyclerView.Adapter<ImageSlider.ImageViewHolder> {

    private List<Movie> movieList;

    public ImageSlider(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageViewHolder holder= new ImageViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.slider_item, parent, false));
        return holder;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Movie movie= movieList.get(position);

        // set all the movie details
        holder.movieTitle.setText(movie.getTitle());
        double rating = movie.getVoteAverage();
        holder.movieRating.setText(String.format("%.1f", rating));
        String releaseYear = movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4 ?
                movie.getReleaseDate().substring(0, 4) : "N/A";
        holder.movieRelease.setText(releaseYear);

        holder.movieDuration.setText(movie.getMediaType());

        String fullBackdropUrl= IMAGE_BASE_URL + IMAGE_SIZE_W500 + movie.getBackdropPath();

        Glide.with(holder.itemView.getContext())
                .load(fullBackdropUrl)
                .placeholder(R.drawable.loading_landscape)
                .error(R.drawable.loading_error)
                .into(holder.backdropImage);

        // open the movie Details activity when a movie is selected
        holder.itemView.setOnClickListener(v->{
            Intent intent = new Intent(holder.itemView.getContext(), MovieDetails.class);
            intent.putExtra("movie_data", movie);
            holder.itemView.getContext().startActivity(intent);
        });


        // lets try to match this genre
        List<Integer> genreIds = movie.getGenreIds();
        holder.genreChip.removeAllViews(); // Clear existing chips
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }

        for (Integer genreId : genreIds) {
            String genreName = GenreManager.getGenreName(genreId);

            // Create chip
            Chip chip = new Chip(holder.itemView.getContext());
            chip.setText(genreName);
            chip.setChipBackgroundColorResource(R.color.trp);
            chip.setClickable(false);
            chip.setTextColor(R.color.white);
            chip.setChipCornerRadiusResource(R.dimen.chip_corner_radius);

            holder.genreChip.addView(chip);
        }

    }


    @Override
    public int getItemCount() {
        return movieList == null ? 0: movieList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView backdropImage;
        private ChipGroup genreChip;
        private TextView movieTitle, movieRating, movieRelease, movieDuration, genreId1, genreId2;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            backdropImage= itemView.findViewById(R.id.backdropImage);
            movieTitle= itemView.findViewById(R.id.movieTitle);
            movieRating= itemView.findViewById(R.id.movieRating);
            movieRelease= itemView.findViewById(R.id.movieRelease);
            movieDuration= itemView.findViewById(R.id.movieDuration);
            genreChip= itemView.findViewById(R.id.genreChip);
        }
    }
}
