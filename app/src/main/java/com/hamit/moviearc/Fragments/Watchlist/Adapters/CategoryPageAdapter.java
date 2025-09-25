package com.hamit.moviearc.Fragments.Watchlist.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hamit.moviearc.Fragments.Watchlist.GeneralFragment;

import java.util.List;

public class CategoryPageAdapter extends FragmentStateAdapter {

    // this is completed
    private final List<String> categories;

    public CategoryPageAdapter(@NonNull FragmentActivity fragmentActivity, List<String> categories) {
        super(fragmentActivity);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        String category= categories.get(position);

        // return an new instance of general fragment
        return GeneralFragment.newInstance(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
