package com.hamit.moviearc.Fragments.CategoriesFragment;

import static com.hamit.moviearc.Fragments.HomeFragment.HomeFragment.API_KEY;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.hamit.moviearc.Adapters.MoviesRecycler;
import com.hamit.moviearc.Network.Clients.RetrofitClient;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.Network.Data.MovieResponse;
import com.hamit.moviearc.Network.Services.TmdbService;
import com.hamit.moviearc.R;
import com.hamit.moviearc.Utils.GenreManager;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryGeneralFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryGeneralFragment extends Fragment {

    private TmdbService mdbService;
    private RecyclerView categoryRecycler;
    private FrameLayout progressOverlay;
    private MoviesRecycler moviesAdapter;

    private static final String ARG_CATEGORY= "category";
    private String category;

    public CategoryGeneralFragment() {
        // Required empty public constructor
    }

    public static CategoryGeneralFragment newInstance(String category) {
        CategoryGeneralFragment fragment = new CategoryGeneralFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
        }
        mdbService = RetrofitClient.getTmdbService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView= inflater.inflate(R.layout.fragment_category_general, container, false);

        categoryRecycler= contentView.findViewById(R.id.category_recycler);
        progressOverlay= contentView.findViewById(R.id.loading_overlay);
        categoryRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));

        moviesAdapter= new MoviesRecycler(Collections.emptyList());
        categoryRecycler.setAdapter(moviesAdapter);

        // fetch movies for the category
        if (category != null) {
            fetchMoviesByCategory(category);
        }

        // add our data and recycler setups here
        return contentView;
    }

    private void fetchMoviesByCategory(String categoryName){
        // show loading
        progressOverlay.setVisibility(View.VISIBLE);

        // use genre id since it's required bu APi
        int genreId= GenreManager.getGenreId(categoryName);

        if (genreId == -1) {
            Toast.makeText(getContext(), "Genre not found: " + categoryName, Toast.LENGTH_SHORT).show();
            progressOverlay.setVisibility(View.GONE);
            return;
        }

        // make API call
        Call<MovieResponse> call= mdbService.discoverMoviesByGenre(API_KEY,
                String.valueOf(genreId),
                1,
                "popularity.desc",
                null,
                "US");
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    List<Movie> movies= response.body().getResults();
                    progressOverlay.setVisibility(View.GONE);

                    if (movies != null && !movies.isEmpty()){
                        // setup our recycler view adapter
                        moviesAdapter.updateMovies(movies);
                    }else {
                        Toast.makeText(getContext(), "No movies found for " + categoryName, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                progressOverlay.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (category != null) {
            fetchMoviesByCategory(category);
        }
    }
}