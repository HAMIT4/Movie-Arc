package com.hamit.moviearc.IntroUI;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.hamit.moviearc.MainActivity;
import com.hamit.moviearc.R;

public class AuthenticationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private RelativeLayout Fullname, ConfirmPassword;
    private LinearLayout rememberSection;
    private Button login_or_signUp;
    private Button signInSection, signUpSection, continueGoogle;

    // our data text editors
    private EditText fullName_ET, email_ET, password_ET, confirm_password_ET;
    private ProgressBar loadingProgress;
    private TextView alreadyAccount;

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

        mAuth= FirebaseAuth.getInstance();

        // layouts that need to be hidden depending on the type of authentication
        Fullname = findViewById(R.id.fullname_section);
        ConfirmPassword = findViewById(R.id.confirmPassword);
        rememberSection= findViewById(R.id.rememberSection);
        alreadyAccount= findViewById(R.id.alreadyAccount);

        // actual buttons that have a function
        login_or_signUp = findViewById(R.id.btn_loginOrSignup);
        signInSection= findViewById(R.id.btn_signInSection);
        signUpSection= findViewById(R.id.btn_signUpSection);
        continueGoogle= findViewById(R.id.continue_google);

        // data text editors
        fullName_ET = findViewById(R.id.fullName_ET);
        email_ET = findViewById(R.id.email_ET);
        password_ET = findViewById(R.id.password_ET);
        confirm_password_ET= findViewById(R.id.confirm_password_ET);
        loadingProgress= findViewById(R.id.loadingProgress);


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
            alreadyAccount.setText("Don't have an Account? Sign Up");
            alreadyAccount.setVisibility(View.VISIBLE);
            alreadyAccount.setOnClickListener(v->{
                // switch to signup section
                signUpSection.performClick();
            });
            // show the remember button
            rememberSection.setVisibility(View.VISIBLE);
            signInSection.setBackgroundTintList(ColorStateList.valueOf(red));
            signUpSection.setBackgroundTintList(ColorStateList.valueOf(grey));

            // change button text
            login_or_signUp.setText("Sign In");

            // authentication code via function and google sign in

            login_or_signUp.setOnClickListener(v->{
                // our login data
                String email = email_ET.getText().toString();
                String password = password_ET.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }else if (password.length() < 8) {
                    Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // every condition passed we can register the user
                    loadingProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loadingProgress.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(AuthenticationActivity.this, "Login Successful",
                                                Toast.LENGTH_SHORT).show();
                                        // open our app
                                        Intent intent= new Intent(AuthenticationActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        // If sign in fails, display a message to the user.

                                        Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }

            });



        } else{
            // Sign Up state
            Fullname.setVisibility(View.VISIBLE);
            ConfirmPassword.setVisibility(View.VISIBLE);
            rememberSection.setVisibility(View.GONE);
            alreadyAccount.setText("Already Have an Account? Sign In");
            alreadyAccount.setVisibility(View.VISIBLE);
            alreadyAccount.setOnClickListener(v->{
                //switch to Sing in section
                signInSection.performClick();
            });

            // change button text
            login_or_signUp.setText("Sign Up");
            signInSection.setBackgroundTintList(ColorStateList.valueOf(grey));
            signUpSection.setBackgroundTintList(ColorStateList.valueOf(red));

            // authentication via function

            login_or_signUp.setOnClickListener(v->{

                // our register data
                String fullName = fullName_ET.getText().toString();
                String email = email_ET.getText().toString();
                String password = password_ET.getText().toString();
                String confirmPassword = confirm_password_ET.getText().toString();

                loadingProgress.setVisibility(View.VISIBLE);

                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();

                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();

                } else if (password.length() < 8) {
                    Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();

                } else {
                    // every condition passed we can register the user

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loadingProgress.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null){
                                            UserProfileChangeRequest profileUpdates= new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(fullName)
                                                    .build();
                                            user.updateProfile(profileUpdates)
                                                    .addOnCompleteListener(profileTask -> {
                                                        if (profileTask.isSuccessful()) {
                                                            // Go to MainActivity
                                                            Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });
                                        }
                                        Toast.makeText(AuthenticationActivity.this, "Account Created",
                                                Toast.LENGTH_SHORT).show();

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }
            });



        }
    }
}