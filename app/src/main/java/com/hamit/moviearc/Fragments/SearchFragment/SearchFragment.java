package com.hamit.moviearc.Fragments.SearchFragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.hamit.moviearc.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment {


    private SearchViewModel mViewModel;
    private FrameLayout noSearch;
    private RecyclerView searchResultRecycler;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View contentView= inflater.inflate(R.layout.fragment_search, container, false);

        // initializing components
        ChipGroup chipGroup= contentView.findViewById(R.id.popularSearchesGroup);
        SearchView searchView= contentView.findViewById(R.id.Search_searchView);
        noSearch= contentView.findViewById(R.id.frameSearch);
        searchResultRecycler= contentView.findViewById(R.id.searchResultRecycler);

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
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()){
                    noSearch.setVisibility(View.GONE);
                    // set recycler view to visible
                    searchResultRecycler.setVisibility(View.VISIBLE);

                    // TODO: call Api and viewModel to fetch search results

                }
                return true;
            }
        });

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        // TODO: Use the ViewModel
    }

}