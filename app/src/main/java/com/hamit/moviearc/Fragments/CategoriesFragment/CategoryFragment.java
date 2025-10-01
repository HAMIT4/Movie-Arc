package com.hamit.moviearc.Fragments.CategoriesFragment;

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
import com.hamit.moviearc.Fragments.CategoriesFragment.Adapters.CategoryListAdapter;
import com.hamit.moviearc.R;
import com.hamit.moviearc.Utils.GenreManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CategoryFragment extends Fragment {
    private TabLayout tabLayoutCategory;
    private ViewPager2 categoryViewpager;

    private CategoryViewModel mViewModel;

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View contentView= inflater.inflate(R.layout.fragment_category, container, false);

        tabLayoutCategory= contentView.findViewById(R.id.category_tabLayout);
        categoryViewpager= contentView.findViewById(R.id.category_viewPager);

        List<String> categories= GenreManager.getAllGenreNames();

        //set adapter
        CategoryListAdapter adapter= new CategoryListAdapter(requireActivity(), categories);
        categoryViewpager.setAdapter(adapter);

        // setup our tabs
        new TabLayoutMediator(tabLayoutCategory, categoryViewpager,
                (tab, position) -> tab.setText(categories.get(position))
        ).attach();

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        // TODO: Use the ViewModel
    }

}