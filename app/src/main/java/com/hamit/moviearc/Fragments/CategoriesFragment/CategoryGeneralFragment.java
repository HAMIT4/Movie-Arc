package com.hamit.moviearc.Fragments.CategoriesFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hamit.moviearc.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryGeneralFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryGeneralFragment extends Fragment {

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView= inflater.inflate(R.layout.fragment_category_general, container, false);


        // add our data and recycler setups here
        return contentView;
    }
}