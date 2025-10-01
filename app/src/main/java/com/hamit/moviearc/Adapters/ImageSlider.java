package com.hamit.moviearc.Adapters;

import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_BASE_URL;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_SIZE_W500;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.R;
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

        // lets try to match this genre
        List<Integer> genreIds = movie.getGenreIds();
        if (genreIds != null && !genreIds.isEmpty()) {
            // First genre will always be available
            holder.genreId1.setText(GenreManager.getGenreName(genreIds.get(0)));

            // Check if other genres are available
            if (genreIds.size() > 1) {
                holder.genreId2.setText(GenreManager.getGenreName(genreIds.get(1)));
                holder.genreId2.setVisibility(View.VISIBLE);
            } else {
                holder.genreId2.setVisibility(View.GONE);
            }
        } else {
            holder.genreId1.setText("Unknown");
            holder.genreId2.setVisibility(View.GONE);
        }



    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0: movieList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView backdropImage;
        private TextView movieTitle, movieRating, movieRelease, movieDuration, genreId1, genreId2;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            backdropImage= itemView.findViewById(R.id.backdropImage);
            movieTitle= itemView.findViewById(R.id.movieTitle);
            movieRating= itemView.findViewById(R.id.movieRating);
            movieRelease= itemView.findViewById(R.id.movieRelease);
            movieDuration= itemView.findViewById(R.id.movieDuration);
            genreId1= itemView.findViewById(R.id.genre_id1);
            genreId2= itemView.findViewById(R.id.genre_id2);
        }
    }
}
