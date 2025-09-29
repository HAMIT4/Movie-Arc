package com.hamit.moviearc.IntroUI.IntroAdapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamit.moviearc.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenreRecycler extends RecyclerView.Adapter<GenreRecycler.MyViewHolder> {

    private OnGenreSelectedListener listener;
    private List<String> selectedGenres = new ArrayList<>();
    List<String> genreList= Arrays.asList("Action", "Adventure", "Animation", "Comedy", "Crime",
            "Documentary", "Drama", "Family", "Fantasy", "History", "Horror", "Music", "Mystery",
            "Romance", "Science Fiction", "TV Movie", "Thriller", "War", "Western");

    public interface OnGenreSelectedListener{
        void onGenreCountChanged(int count);
    }

    public GenreRecycler(OnGenreSelectedListener listener){
        this.listener= listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder holder= new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.genre_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String genre = genreList.get(position);
        holder.genreTitle.setText(genre);

        // Reset background if re-used
        if (selectedGenres.contains(genre)) {
            holder.genrePicker.setBackgroundResource(R.drawable.red_bg);
        } else {
            holder.genrePicker.setBackgroundResource(R.drawable.outline_bg);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedGenres.contains(genre)) {
                selectedGenres.remove(genre);
                holder.genrePicker.setBackgroundResource(R.drawable.outline_bg);
            } else {
                selectedGenres.add(genre);
                holder.genrePicker.setBackgroundResource(R.drawable.red_bg);
            }

            // notify fragment
            if (listener != null) {
                listener.onGenreCountChanged(selectedGenres.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreList == null? 0: genreList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView genreTitle;
        private RelativeLayout genrePicker;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            genreTitle= itemView.findViewById(R.id.genreTitle);
            genrePicker= itemView.findViewById(R.id.Genre);
        }
    }
}
