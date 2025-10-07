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
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hamit.moviearc.Fragments.Watchlist.Adapters.CategoryPageAdapter;
import com.hamit.moviearc.R;

import java.util.Arrays;
import java.util.List;

public class WatchlistFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseUser user;

    private TabLayout tablayout;
    private ViewPager2 viewPager;

    private TextView username, email;

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

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        username= contentView.findViewById(R.id.username);
        email= contentView.findViewById(R.id.email);
        // update our user info UI
        updateUserInfo(user);

        return contentView;
    }

    private void updateUserInfo(FirebaseUser user) {
        if (user != null){
            // set our text
            username.setText(user.getDisplayName());
            email.setText(user.getEmail());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
        // TODO: Use the ViewModel
    }

}