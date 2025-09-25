package com.hamit.moviearc.Fragments.Watchlist;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hamit.moviearc.Fragments.Watchlist.Adapters.CategoryPageAdapter;
import com.hamit.moviearc.R;

import java.util.Arrays;
import java.util.List;

public class WatchlistFragment extends Fragment {

    private TabLayout tablayout;
    private ViewPager2 viewPager;

    private WatchlistViewModel mViewModel;

    public static WatchlistFragment newInstance() {
        return new WatchlistFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView= inflater.inflate(R.layout.fragment_watchlist, container, false);

        tablayout= contentView.findViewById(R.id.tabLayout_watchList);
        viewPager= contentView.findViewById(R.id.viewpager_watchList);
        List<String> categories= Arrays.asList("Watchlist","Wishlist", "History", "Reviews");

        // set the category adapter
        CategoryPageAdapter adapter= new CategoryPageAdapter(requireActivity(), categories);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tablayout, viewPager,
                (tab, position) -> tab.setText(categories.get(position))
        ).attach();

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
        // TODO: Use the ViewModel
    }

}