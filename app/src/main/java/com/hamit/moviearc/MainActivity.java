package com.hamit.moviearc;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hamit.moviearc.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        title= findViewById(R.id.titleTextView);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // setup binding
        BottomNavigationView navView= findViewById(R.id.bottomNavigationView);
        AppBarConfiguration appBarConfiguration= new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_categories,R.id.nav_watchlist, R.id.nav_search, R.id.nav_profile
        ).build();
        NavController navController= Navigation.findNavController(this, R.id.fragmentContainerView2);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        // setup a title change on navigation change
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            updateTitle(destination.getId());
        });
    }

    private void updateTitle(int id) {
        if (id == R.id.nav_home){
            title.setText("Movie Arc");
        } else if (id ==R.id.nav_search) {
            title.setText("Discover");
        } else if (id == R.id.nav_categories) {
            title.setText("Categories");
        } else if (id == R.id.nav_profile) {
            title.setText("Profile");
        } else {
            title.setText("Watchlist");
        }
    }


}