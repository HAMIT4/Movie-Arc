package com.hamit.moviearc.IntroUI;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hamit.moviearc.R;

public class AuthenticationActivity extends AppCompatActivity {

    private RelativeLayout Fullname, ConfirmPassword;
    private LinearLayout rememberSection;
    private Button login_or_signUp;
    private Button signInSection, signUpSection, continueGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authentication);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // layouts that need to be hidden depending on the type of authentication
        Fullname = findViewById(R.id.fullname_section);
        ConfirmPassword = findViewById(R.id.confirmPassword);
        rememberSection= findViewById(R.id.rememberSection);

        // actual buttons that have a function
        login_or_signUp = findViewById(R.id.btn_loginOrSignup);
        signInSection= findViewById(R.id.btn_signInSection);
        signUpSection= findViewById(R.id.btn_signUpSection);
        continueGoogle= findViewById(R.id.continue_google);


        // initial state will be sign in
        updateUi(true);

        signInSection.setOnClickListener(v-> updateUi(true));
        signUpSection.setOnClickListener(v-> updateUi(false));

    }

    private void updateUi(boolean isSignInMode) {
        // colors
        int red = Color.parseColor("#BAFF0000");
        int grey = Color.parseColor("#CD9E9E9E");

        // check mode
        if(isSignInMode){
            // sign in Mode

            // hide un necessary views
            Fullname.setVisibility(View.GONE);
            ConfirmPassword.setVisibility(View.GONE);
            // show the remember button
            rememberSection.setVisibility(View.VISIBLE);
            signInSection.setBackgroundTintList(ColorStateList.valueOf(red));
            signUpSection.setBackgroundTintList(ColorStateList.valueOf(grey));

            // change button text
            login_or_signUp.setText("Sign In");

            // authentication code via function and google sign in


        } else{
            // Sign Up state
            Fullname.setVisibility(View.VISIBLE);
            ConfirmPassword.setVisibility(View.VISIBLE);
            rememberSection.setVisibility(View.GONE);

            // change button text
            login_or_signUp.setText("Sign Up");
            signInSection.setBackgroundTintList(ColorStateList.valueOf(grey));
            signUpSection.setBackgroundTintList(ColorStateList.valueOf(red));

            // authentication via function and google sign in

        }
    }
}