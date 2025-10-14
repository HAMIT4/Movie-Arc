package com.hamit.moviearc.Adapters;

import static androidx.core.app.NotificationCompat.getColor;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_BASE_URL;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_SIZE_W342;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_SIZE_W500;
import static java.security.AccessController.getContext;

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
import com.hamit.moviearc.Utils.GenreManager;

import java.util.List;

public class UserMovieRecycler extends RecyclerView.Adapter<UserMovieRecycler.MyViewHolder> {

    List<Movie> movieList;

    public UserMovieRecycler(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public UserMovieRecycler.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder holder= new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wiishlist_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserMovieRecycler.MyViewHolder holder, int position) {
        Movie movie= movieList.get(position);
        holder.movieTitle.setText(movie.getTitle());
        String releaseDate = movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 10 ?
                movie.getReleaseDate().substring(8, 10) + "-" + movie.getReleaseDate().substring(5, 7) + "-" + movie.getReleaseDate().substring(0, 4) : "N/A";
        holder.releaseDate.setText(releaseDate);
        double rating = movie.getVoteAverage();
        holder.Rating.setText(String.format("%.1f", rating));

        String fullBackdropUrl= IMAGE_BASE_URL + IMAGE_SIZE_W342 + movie.getBackdropPath();

        Glide.with(holder.itemView.getContext())
                .load(fullBackdropUrl)
                .placeholder(R.drawable.loading_landscape)
                .error(R.drawable.loading_error)
                .into(holder.movieImage);

        // lets try to match this genre
        List<Integer> genreIds = movie.getGenreIds();
        holder.genreChips.removeAllViews();
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
            chip.setChipCornerRadiusResource(R.dimen.chip_corner_radius);

            holder.genreChips.addView(chip);
        }

    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0 : movieList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView movieImage;
        private TextView movieTitle, releaseDate, Rating;
        private ChipGroup genreChips;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            movieImage= itemView.findViewById(R.id.posterImg);
            movieTitle= itemView.findViewById(R.id.movieTitle);
            releaseDate= itemView.findViewById(R.id.releaseDate);
            Rating= itemView.findViewById(R.id.movieRating);
            genreChips= itemView.findViewById(R.id.genreChips);
        }
    }
}
