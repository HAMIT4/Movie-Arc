package com.hamit.moviearc.Fragments.HomeFragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hamit.moviearc.Adapters.PopularRecycler;
import com.hamit.moviearc.Network.Clients.RetrofitClient;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.Network.Data.MovieResponse;
import com.hamit.moviearc.Network.Services.TmdbService;
import com.hamit.moviearc.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    public String API_KEY= "Key";
    TmdbService mdbService;

    private RecyclerView forYouRecyclerView;
    private PopularRecycler forYouAdapter;

    private HomeViewModel mViewModel;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView= inflater.inflate(R.layout.fragment_home, container, false);

        mdbService= RetrofitClient.getTmdbService();

        forYouRecyclerView = contentView.findViewById(R.id.forYouRecycler);
        forYouRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        fetchPopularMovies();



        return contentView;
    }

    private void fetchPopularMovies() {
        Call<MovieResponse> call = mdbService.getPopularMovies(API_KEY, 1,"en-US");
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful()){
                    List<Movie> movies= response.body().getResults();
                    // feed to our recycler
                    forYouAdapter = new PopularRecycler(movies);
                    forYouRecyclerView.setAdapter(forYouAdapter);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                // toast to detect error
                Toast.makeText(getContext(), "Failed to get Data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }

}