package com.hamit.moviearc.IntroUI.IntroFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hamit.moviearc.IntroUI.IntroAdapters.GenreRecycler;
import com.hamit.moviearc.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenreFragment extends Fragment implements GenreRecycler.OnGenreSelectedListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView genreCount;

    private RecyclerView genreRecycler;
    private GenreRecycler genreAdapter;


    private String mParam1;
    private String mParam2;

    public GenreFragment() {
        // Required empty public constructor
    }

    public static GenreFragment newInstance(String param1, String param2) {
        GenreFragment fragment = new GenreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView= inflater.inflate(R.layout.fragment_genre, container, false);

        genreCount= contentView.findViewById(R.id.genreCount);

        // setup recyclerView
        genreRecycler= contentView.findViewById(R.id.genreRecycler);
        genreRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        genreAdapter= new GenreRecycler(this::onGenreCountChanged);
        genreRecycler.setAdapter(genreAdapter);

        return contentView;
    }

    @Override
    public void onGenreCountChanged(int count) {
        genreCount.setText("Selected " + count + " genres");
    }
}