package com.hamit.moviearc.Adapters;

import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_BASE_URL;
import static com.hamit.moviearc.Network.Services.TmdbService.IMAGE_SIZE_W342;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hamit.moviearc.Network.Data.MultiSearchResponse;
import com.hamit.moviearc.R;
import com.hamit.moviearc.Ui.MovieDetails;

import java.util.List;

public class SearchRecycler extends RecyclerView.Adapter<SearchRecycler.MyViewHolder>{

    private List<MultiSearchResponse.ResultItem> searchResponse;

    public SearchRecycler(List<MultiSearchResponse.ResultItem> searchResponse) {
        this.searchResponse = searchResponse;
    }

    public void updateResults(List<MultiSearchResponse.ResultItem> searchedMovies) {
        this.searchResponse = searchedMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder holder= new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.potrait_movie_item2,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        MultiSearchResponse.ResultItem movie= searchResponse.get(position);

        holder.movieTitle.setText(movie.getTitle());
        double rating = movie.getVoteAverage();
        holder.movieRating.setText(String.format("%.1f", rating));
        String releaseYear = movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4 ?
                movie.getReleaseDate().substring(0, 4) : "N/A";
        holder.movieRelease.setText(releaseYear);

        String fullPosterUrl= IMAGE_BASE_URL + IMAGE_SIZE_W342 + movie.getPosterPath();
        Glide.with(holder.itemView.getContext())
                .load(fullPosterUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.movieImage);

        // test details activity for movie details from search
        holder.itemView.setOnClickListener(v -> {
            MultiSearchResponse.ResultItem resultItem = searchResponse.get(position);
            Intent intent= new Intent(holder.itemView.getContext(), MovieDetails.class);
            // try to pass results for movies only
            if ("movie".equals(resultItem.getMediaType())) {
                intent.putExtra("result_item", resultItem);
            } else {
                Toast.makeText(holder.itemView.getContext(),
                        "This feature is available for movies only",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            holder.itemView.getContext().startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return searchResponse == null ? 0 : searchResponse.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView movieImage;
        private TextView movieTitle, movieRating, movieRelease;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            movieImage = itemView.findViewById(R.id.posterImage);
            movieTitle = itemView.findViewById(R.id.movieTitle);
            movieRating = itemView.findViewById(R.id.movieRating);
            movieRelease = itemView.findViewById(R.id.movieRelease);
        }
    }
}


