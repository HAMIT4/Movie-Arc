package com.hamit.moviearc.IntroUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hamit.moviearc.IntroUI.IntroFragments.ActorFragment;
import com.hamit.moviearc.IntroUI.IntroFragments.GenreFragment;
import com.hamit.moviearc.IntroUI.IntroFragments.WelcomeFragment;
import com.hamit.moviearc.R;

import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private Button btnContinue, btnBack, btnSkip;
    private FrameLayout frameContiner;
    private ProgressBar progressBar;
    private TextView stepCount;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnContinue = findViewById(R.id.btn_Continue);
        btnBack = findViewById(R.id.btn_back);
        btnSkip= findViewById(R.id.btn_skip);
        frameContiner= findViewById(R.id.frameContainer);
        stepCount= findViewById(R.id.step_tv);
        progressBar= findViewById(R.id.progressBar);

        // setup default fragment
        switchFragments(new WelcomeFragment());
        btnBack.setVisibility(View.GONE);
        stepCount.setText("step 1 of 3");
        progressBar.setProgress(33);

        btnContinue.setOnClickListener(v -> {
            // we check our progress adjust and switch fragments
            if (activeFragment instanceof WelcomeFragment){
                btnBack.setVisibility(View.VISIBLE);
                switchFragments(new GenreFragment());
                stepCount.setText("step 2 of 3");
                progressBar.setProgress(66);
            } else if (activeFragment instanceof  GenreFragment) {
                btnBack.setVisibility(View.VISIBLE);
                switchFragments(new ActorFragment());
                stepCount.setText("step 3 of 3");
                progressBar.setProgress(100);
            } else if (activeFragment instanceof  ActorFragment) {
                // switch to authentication screen
                Intent intent= new Intent(this, AuthenticationActivity.class);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(v->{
            if (activeFragment instanceof GenreFragment){
                switchFragments(new WelcomeFragment());
                stepCount.setText("step 1 of 3");
                progressBar.setProgress(33);
                btnBack.setVisibility(View.GONE);
            } else if (activeFragment instanceof ActorFragment) {
                switchFragments(new GenreFragment());
                stepCount.setText("step 2 of 3");
                progressBar.setProgress(66);
            }
        });

        // to skip the intro screen go straight to signup
        btnSkip.setOnClickListener(v->{
            Intent intent= new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
        });

    }

    private void switchFragments(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Hide all existing fragments first
        List<Fragment> fragments = fm.getFragments();
        for (Fragment frag : fragments) {
            if (frag != null) {
                ft.hide(frag);
            }
        }

        // Add the new fragment if not already added
        if (!fragment.isAdded()) {
            ft.add(R.id.frameContainer, fragment);
        }

        // Show the new fragment
        ft.show(fragment);
        activeFragment = fragment;

        ft.commit();
    }
}