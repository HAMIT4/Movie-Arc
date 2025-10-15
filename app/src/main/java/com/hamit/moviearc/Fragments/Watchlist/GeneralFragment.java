package com.hamit.moviearc.Fragments.Watchlist;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hamit.moviearc.Adapters.UserMovieRecycler;
import com.hamit.moviearc.HelperClasses.FirestoreHelper;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeneralFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeneralFragment extends Fragment {

    private static final String ARG_CATEGORY= "category";
    private String category;
    private TextView emptyList;
    private RecyclerView generalRecycler;
    private FirestoreHelper firestoreHelper;
    private UserMovieRecycler movieAdapter;
    private List<Movie> movieList;


    public GeneralFragment() {
        // Required empty public constructor
    }

    public static GeneralFragment newInstance(String category) {
        GeneralFragment fragment= new GeneralFragment();
        Bundle args= new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category= getArguments().getString(ARG_CATEGORY);
        }

        firestoreHelper= new FirestoreHelper();
        movieList= new ArrayList<>();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCategoryData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View contentView= inflater.inflate(R.layout.fragment_general, container, false);

        emptyList= contentView.findViewById(R.id.textEmpty);
        generalRecycler= contentView.findViewById(R.id.generalRecyclerView);


        // setup recyclerView
        setupRecyclerView();

        return contentView;
    }

    public void setupRecyclerView(){
        generalRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        movieAdapter = new UserMovieRecycler(movieList);
        generalRecycler.setAdapter(movieAdapter);
    }

    private void loadCategoryData() {
        switch (category) {
            case "Watchlist":
                loadWatchlist();
                break;
            case "Wishlist":
                loadWishlist();
                break;
            case "Liked":
                loadLikedMovies();
                break;
            case "History":
                loadHistory();
                break;
            case "Reviews":
                loadReviews();
                break;
            default:
                showEmptyState();
                break;
        }
    }

    private void showEmptyState() {
        emptyList.setVisibility(View.VISIBLE);
        generalRecycler.setVisibility(View.GONE);

        // Set empty message based on category
        switch (category) {
            case "Watchlist":
                emptyList.setText("Your watchlist is empty");
                break;
            case "Wishlist":
                emptyList.setText("Your wishlist is empty");
                break;
            case "Liked Movies":
                emptyList.setText("You haven't liked any movies yet");
                break;
            case "History":
                emptyList.setText("No viewing history");
                break;
            case "Reviews":
                emptyList.setText("No reviews yet");
                break;
        }
    }

    private void loadReviews() {
        // nothing here yet
    }

    private void loadHistory() {
        // TODO: Implement history loading if you have history collection
        showEmptyState();
        emptyList.setText("No history available");
    }

    private void loadLikedMovies() {
        firestoreHelper.getLikedMovies(new FirestoreHelper.CollectionCallback() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                movieList.clear();
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Movie movie = document.toObject(Movie.class);
                        if (movie != null) {
                            movieList.add(movie);
                        }
                    }
                    updateUI();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to load liked movies: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void loadWishlist() {
        firestoreHelper.getWishlist(new FirestoreHelper.CollectionCallback() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                movieList.clear();
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Movie movie = document.toObject(Movie.class);
                        if (movie != null) {
                            movieList.add(movie);
                        }
                    }
                    updateUI();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to load wishlist: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void loadWatchlist(){
        firestoreHelper.getWatchlist(new FirestoreHelper.CollectionCallback() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                movieList.clear();
                if (!queryDocumentSnapshots.isEmpty()){
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                        Movie movie = document.toObject(Movie.class);
                        if(movie != null){
                            movieList.add(movie);
                        }
                    }
                    updateUI();
                } else{
                    showEmptyState();
                }
                
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to load watchlist: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        if (movieList.isEmpty()) {
            showEmptyState();
        } else {
            // Update the adapter and show recyclerView
            movieAdapter.notifyDataSetChanged();
            emptyList.setVisibility(View.GONE);
            generalRecycler.setVisibility(View.VISIBLE);
        }
        notifyParentToUpdateCounts();
    }

    private void notifyParentToUpdateCounts() {
        // Get parent fragment and call refreshCounts
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof WatchlistFragment) {
            ((WatchlistFragment) parentFragment).refreshCounts();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when fragment becomes visible
        if (category != null && movieList.isEmpty()) {
            loadCategoryData();
        }
    }
}