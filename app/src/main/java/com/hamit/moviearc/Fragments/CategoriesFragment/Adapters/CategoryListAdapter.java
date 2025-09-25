package com.hamit.moviearc.Fragments.CategoriesFragment.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hamit.moviearc.Fragments.CategoriesFragment.CategoryGeneralFragment;

import java.util.List;

public class CategoryListAdapter extends FragmentStateAdapter {

    private final List<String> categories;

    public CategoryListAdapter(@NonNull FragmentActivity fragmentActivity, List<String> categories) {
        super(fragmentActivity);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String category= categories.get(position);

        //return a new instance of general fragment
        return CategoryGeneralFragment.newInstance(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
