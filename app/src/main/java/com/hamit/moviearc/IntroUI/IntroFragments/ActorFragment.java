package com.hamit.moviearc.IntroUI.IntroFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hamit.moviearc.IntroUI.IntroAdapters.ActorRecycler;
import com.hamit.moviearc.R;


public class ActorFragment extends Fragment implements ActorRecycler.OnActorSelectedListener {

    private TextView actorCount;
    private RecyclerView actorsRecyclerView;
    private ActorRecycler actorAdapter;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public ActorFragment() {
        // Required empty public constructor
    }

    public static ActorFragment newInstance(String param1, String param2) {
        ActorFragment fragment = new ActorFragment();
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
        View contentView = inflater.inflate(R.layout.fragment_actor, container, false);

        actorCount = contentView.findViewById(R.id.actorCount);

        // setup RecyclerView
        actorsRecyclerView = contentView.findViewById(R.id.actorRecycler);
        actorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        actorAdapter= new ActorRecycler(this::onActorCountChanged);
        actorsRecyclerView.setAdapter(actorAdapter);

        return contentView;
    }

    @Override
    public void onActorCountChanged(int count) {
        actorCount.setText("Selected " + count + " actors");
    }
}

