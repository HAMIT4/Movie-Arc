package com.hamit.moviearc.Fragments.SearchFragment;

import static com.hamit.moviearc.Fragments.HomeFragment.HomeFragment.API_KEY;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.hamit.moviearc.Adapters.SearchRecycler;
import com.hamit.moviearc.Network.Clients.RetrofitClient;
import com.hamit.moviearc.Network.Data.MultiSearchResponse;
import com.hamit.moviearc.Network.Services.TmdbService;
import com.hamit.moviearc.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private SearchView searchView;
    private SearchViewModel mViewModel;
    private FrameLayout noSearch;
    private RecyclerView searchResultRecycler;
    private TmdbService mdbService;
    private SearchRecycler searchAdapter;
    ChipGroup chipGroup;
    private TextView popularTxt;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View contentView= inflater.inflate(R.layout.fragment_search, container, false);
        mdbService= RetrofitClient.getTmdbService();

        // initializing components
        chipGroup= contentView.findViewById(R.id.popularSearchesGroup);
        searchView= contentView.findViewById(R.id.Search_searchView);
        noSearch= contentView.findViewById(R.id.frameSearch);
        popularTxt= contentView.findViewById(R.id.txt);

        searchResultRecycler= contentView.findViewById(R.id.searchResultRecycler);
        searchResultRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));

        searchAdapter= new SearchRecycler(Collections.emptyList());
        searchResultRecycler.setAdapter(searchAdapter);


        // popular searches default
        List<String> popularSearches= Arrays.asList(
                "Action", "Comedy", "Sci-Fi", "Leonardo DiCaprio", "Christopher Nolan"
        );

        // dynamically add chips in case I implement searches in database
        for (String search: popularSearches){
            Chip chip=  new Chip(getContext(), null, R.style.Widget_Custom_Chip);
            chip.setText(search);

            chip.setOnClickListener(v->{
                searchView.setQuery(search, true);
            });

            chipGroup.addView(chip);
        }

        // on default show no search placeholder, and hide recycler
        noSearch.setVisibility(View.VISIBLE);
        searchResultRecycler.setVisibility(View.GONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // if search box is cleared show placeholder
                if (newText.trim().isEmpty()){
                    noSearch.setVisibility(View.VISIBLE);
                    searchResultRecycler.setVisibility(View.GONE);
                    chipGroup.setVisibility(View.VISIBLE);
                    popularTxt.setVisibility(View.VISIBLE);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()){
                    noSearch.setVisibility(View.GONE);
                    chipGroup.setVisibility(View.GONE);
                    popularTxt.setVisibility(View.GONE);

                    // set recycler view to visible
                    searchResultRecycler.setVisibility(View.VISIBLE);

                    // TODO: call Api and viewModel to fetch search results
                    fetchSearchResults(query);

                }
                return true;
            }
        });

        return contentView;
    }

    private void fetchSearchResults(String query) {
        Call<MultiSearchResponse> call= mdbService.multiSearch(API_KEY, query, 1, false);
        call.enqueue(new Callback<MultiSearchResponse>() {
            @Override
            public void onResponse(Call<MultiSearchResponse> call, Response<MultiSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    MultiSearchResponse searchResponse= response.body();
                    List<MultiSearchResponse.ResultItem> results= searchResponse.getResults();
                    // add the results to a recyclerView to display them
                    searchAdapter.updateResults(results);

                } else {
                    Toast.makeText(getContext(), "No Results found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MultiSearchResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching search results", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        // TODO: Use the ViewModel
    }

}