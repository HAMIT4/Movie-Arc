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
import com.hamit.moviearc.HelperClasses.FirestoreHelper;
import com.hamit.moviearc.R;

import java.util.Arrays;
import java.util.List;

public class WatchlistFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseUser user;

    private TabLayout tablayout;
    private ViewPager2 viewPager;

    private TextView username, email, watchlistCount, wishlistCount, likedCount,
            userLikedCount, userWishlistCount, userWatchlistCount;

    private WatchlistViewModel mViewModel;

    private FirestoreHelper firestoreHelper;

    public static WatchlistFragment newInstance() {
        return new WatchlistFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView= inflater.inflate(R.layout.fragment_watchlist, container, false);

        firestoreHelper= new FirestoreHelper();

        tablayout= contentView.findViewById(R.id.tabLayout_watchList);
        viewPager= contentView.findViewById(R.id.viewpager_watchList);
        watchlistCount= contentView.findViewById(R.id.Watchlist_count);
        wishlistCount= contentView.findViewById(R.id.Wishlist_count);
        likedCount= contentView.findViewById(R.id.liked_count);
        userLikedCount= contentView.findViewById(R.id.like_count);
        userWishlistCount= contentView.findViewById(R.id.wishlist_count);
        userWatchlistCount= contentView.findViewById(R.id.watch_count);

        List<String> categories= Arrays.asList("Watchlist","Wishlist", "Liked", "Reviews");

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
        updateCounts();

        return contentView;
    }

    private void updateCounts() {
        // Update watchlist count
        firestoreHelper.getWatchlistCount(new FirestoreHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        watchlistCount.setText(String.valueOf(count));
                        userWatchlistCount.setText(String.valueOf(count));
                        updateTabTitle(0, "Watchlist", count);
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                // Handle error
            }
        });

        // Update wishlist count
        firestoreHelper.getWishlistCount(new FirestoreHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        wishlistCount.setText(String.valueOf(count));
                        userWishlistCount.setText(String.valueOf(count));
                        updateTabTitle(1, "Wishlist", count);
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                // Handle error
            }
        });

        // Update liked movies count
        firestoreHelper.getLikedMoviesCount(new FirestoreHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        likedCount.setText(String.valueOf(count));
                        userLikedCount.setText(String.valueOf(count));
                        updateTabTitle(2, "Liked", count);
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                // Handle error
            }
        });

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

    public void refreshCounts() {
        updateCounts();
    }

    private void updateTabTitle(int position, String baseTitle, int count) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TabLayout.Tab tab = tablayout.getTabAt(position);
                    if (tab != null) {
                        tab.setText(baseTitle + " (" + count + ")");
                    }
                }
            });
        }
    }
}