package com.hamit.moviearc.Fragments.HomeFragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hamit.moviearc.Adapters.ImageSlider;
import com.hamit.moviearc.Adapters.PopularRecycler;
import com.hamit.moviearc.BuildConfig;
import com.hamit.moviearc.Network.Clients.RetrofitClient;
import com.hamit.moviearc.Network.Data.GenreResponse;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.Network.Data.MovieResponse;
import com.hamit.moviearc.Network.Services.TmdbService;
import com.hamit.moviearc.R;
import com.hamit.moviearc.Utils.GenreManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    public static final String API_KEY = BuildConfig.TMDB_API_KEY;
    TmdbService mdbService;

    private RecyclerView forYouRecyclerView;
    private PopularRecycler forYouAdapter;
    private PopularRecycler topRatedAdapter;
    private RecyclerView topRatedRecyclerView;

    private ViewPager2 imageSlider;
    private ImageSlider imageSliderAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private static final long SLIDER_DELAY = 5000;

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

        topRatedRecyclerView = contentView.findViewById(R.id.topRatedRecycler);
        topRatedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        imageSlider = contentView.findViewById(R.id.imageSlider);
        // setup image slider
        setupImageSlider();

        // First, try to load genres from cache, if not available, fetch from API
        if (!GenreManager.loadGenresFromCache(requireContext())) {
            fetchMovieGenres();
        } else {
            Log.d("HomeFragment", "Genres loaded from cache");
        }

        fetchPopularMovies();
        fetchTopRatedMovies();
        fetchBackdropMovies();



        return contentView;
    }

    private void fetchMovieGenres() {
        Call<GenreResponse> call = mdbService.getMovieGenres(API_KEY);
        call.enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenreResponse> call, @NonNull Response<GenreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenreResponse genreResponse = response.body();
                    GenreManager.saveGenresToCache(requireContext(), genreResponse);
                    Log.d("HomeFragment", "Successfully fetched " +
                            (genreResponse.getGenres() != null ? genreResponse.getGenres().size() : 0) + " genres");
                } else {
                    Log.e("HomeFragment", "Failed to fetch genres: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenreResponse> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Error fetching genres", t);
                // We'll use the default genres as fallback
            }
        });
    }

    private void setupImageSlider() {
        // smooth transition
        imageSlider.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                // let's try a fade transformation
                page.setAlpha(1 - Math.abs(position) * 0.3f);
            }
        });
        // Set off-screen page limit
        imageSlider.setOffscreenPageLimit(2);
    }

    private void startAutoSlider(){
        if (sliderRunnable != null){
            sliderHandler.removeCallbacks(sliderRunnable);
        }

        sliderRunnable= new Runnable() {
            @Override
            public void run() {
                if (imageSliderAdapter != null && imageSliderAdapter.getItemCount() > 0){
                    int currentItem= imageSlider.getCurrentItem();
                    int totalItems= imageSliderAdapter.getItemCount();

                    if(currentItem < totalItems - 1){
                        imageSlider.setCurrentItem(currentItem + 1, true);
                    } else{
                        // smooth scroll back to first item
                        imageSlider.setCurrentItem(0, true);
                    }
                }
                sliderHandler.postDelayed(this, SLIDER_DELAY);
            }
        };

        // start auto slider
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);

        // reset timer when user interacts with slider
        imageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    // User is dragging, remove callbacks temporarily
                    sliderHandler.removeCallbacks(sliderRunnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // User stopped interacting, restart auto-slide
                    sliderHandler.removeCallbacks(sliderRunnable);
                    sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // remove any pending callbacks and restart the timer
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                // will figure out what to do with this later
            }
        });
    }

    private void stopAutoSlider(){
        if (sliderHandler != null && sliderRunnable != null){
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    private void fetchBackdropMovies() {
        Call<MovieResponse> call = mdbService.getTrendingMovies("day", API_KEY, 1);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful()){
                    List<Movie> movies= response.body().getResults();
                    // feed to our recycler
                    imageSliderAdapter = new ImageSlider(movies);
                    imageSlider.setAdapter(imageSliderAdapter);
                    // start auto slider after data is success
                    startAutoSlider();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, Throwable t) {
                // toast to detect error
                Toast.makeText(getContext(), "Failed to get Data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPopularMovies() {
        Call<MovieResponse> call = mdbService.getPopularMovies(API_KEY, 1,"en-US");
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful()){
                    List<Movie> movies= response.body().getResults();
                    // feed to our recycler
                    forYouAdapter = new PopularRecycler(movies);
                    forYouRecyclerView.setAdapter(forYouAdapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, Throwable t) {
                // toast to detect error
                Toast.makeText(getContext(), "Failed to get Data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTopRatedMovies() {
        Call<MovieResponse> call = mdbService.getTopRatedMovies(API_KEY, 1,"en-US");
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful()){
                    List<Movie> movies= response.body().getResults();
                    // feed to our recycler
                    topRatedAdapter = new PopularRecycler(movies);
                    topRatedRecyclerView.setAdapter(topRatedAdapter);
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

    @Override
    public void onResume() {
        super.onResume();
        // Restart auto-slider when fragment becomes visible
        if (imageSliderAdapter != null && imageSliderAdapter.getItemCount() > 0) {
            startAutoSlider();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop auto-slider when fragment is not visible to save resources
        stopAutoSlider();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up to prevent memory leaks
        stopAutoSlider();
        if (imageSlider != null) {
            imageSlider.unregisterOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {});
        }
    }

}