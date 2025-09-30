package com.hamit.moviearc.Fragments.ProfileFragment;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hamit.moviearc.IntroUI.AuthenticationActivity;
import com.hamit.moviearc.R;

public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser user;

    private TextView btn_editAccount, btnLogout, userName, userEmail;

    private ProfileViewModel mViewModel;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View contentView= inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        btn_editAccount = contentView.findViewById(R.id.btn_editAccount);
        btnLogout = contentView.findViewById(R.id.btnLogout);
        userName = contentView.findViewById(R.id.username);
        userEmail = contentView.findViewById(R.id.email);

        // update our user info UI
        updateUserInfo(user);
        btnLogout.setOnClickListener(v->{
            // create the logout option and exit user from the app
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getActivity(), "Logging out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), AuthenticationActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return contentView;
    }

    private void updateUserInfo(FirebaseUser user) {
        if (user != null){
            // set our text
            userName.setText(user.getDisplayName());
            userEmail.setText(user.getEmail());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }

}