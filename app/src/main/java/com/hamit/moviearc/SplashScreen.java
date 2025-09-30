package com.hamit.moviearc;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hamit.moviearc.IntroUI.IntroActivity;

public class SplashScreen extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    private View dot1, dot2, dot3;
    private Handler handler= new Handler();
    private RelativeLayout logoContiner;
    private static final Long CYCLE_DURATION= 3000L;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth= FirebaseAuth.getInstance();

        // initialize the dots
        dot1= findViewById(R.id.dot1);
        dot2= findViewById(R.id.dot2);
        dot3= findViewById(R.id.dot3);
        logoContiner= findViewById(R.id.relativeLayout6);

        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        logoContiner.startAnimation(scaleUp);

        startFullAnimationCycle();

        new Handler().postDelayed(() -> {

            Intent intent;
            if (currentUser != null){
                // skip to the Main Activity since the user is already logged in
                intent = new Intent(SplashScreen.this, MainActivity.class);
                Toast.makeText(this, "Welcome Back", Toast.LENGTH_SHORT).show();
            } else{
                intent = new Intent(SplashScreen.this, IntroActivity.class);
                Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
            }
            startActivity(intent);
            finish();
        }, 7000);

    }

    private void startFullAnimationCycle(){
        // stop any animation before stating a new cycle
        dot1.animate().cancel();
        dot2.animate().cancel();
        dot3.animate().cancel();

        // reset to starting position
        dot1.setTranslationY(0f);
        dot2.setTranslationY(0f);
        dot3.setTranslationY(0f);

        // animate each dot with a slight stagger
        animateDot(dot1, 0);
        animateDot(dot2, 150);
        animateDot(dot3, 250);

        handler.postDelayed(animationLoop, CYCLE_DURATION);

    }
    private Runnable animationLoop = new Runnable() {
        @Override
        public void run() {
            startFullAnimationCycle();
        }
    };

    private void animateDot(View view, long startDelay){
        Animator animator= AnimatorInflater.loadAnimator(this, R.animator.dot_bounce);
        animator.setTarget(view);
        animator.setStartDelay(startDelay);
        animator.start();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(animationLoop);
        super.onDestroy();
    }
}