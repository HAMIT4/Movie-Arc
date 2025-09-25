package com.hamit.moviearc.Fragments.Watchlist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hamit.moviearc.R;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View contentView= inflater.inflate(R.layout.fragment_general, container, false);

        emptyList= contentView.findViewById(R.id.textEmpty);
        generalRecycler= contentView.findViewById(R.id.generalRecyclerView);


        // remember to setup the recycler view to show details depending on tab selected
        // Load data to display and setup recycler

        loadCategoryData();

        return contentView;
    }

    private void loadCategoryData() {
        List<String> data = Collections.emptyList();
        // here we will switch data depending on the tap

        // TODO: the data will be loaded from saved user data, either from the database or a shared storage class!!


        if (data.isEmpty()) {
            // show our hidden empty text and hide recycler view
            emptyList.setVisibility(View.VISIBLE);
            generalRecycler.setVisibility(View.GONE);
        } else{
            // hide our hidden empty text and show recycler view
            emptyList.setVisibility(View.GONE);
            generalRecycler.setVisibility(View.VISIBLE);
        }
    }
}