package com.example.healthtagram.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.healthtagram.R;
import com.example.healthtagram.activity.EditProfileActivity;
import com.example.healthtagram.activity.SignupActivity;
import com.example.healthtagram.listener.EditProfileListener;
import com.example.healthtagram.loading.BaseApplication;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.loading.BaseFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class ProfileFragment extends BaseFragment {
    private TextView nickname, bio;
    private ImageView profilePicture;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private DocumentReference docRef;
    public ProfileFragment() {
        // Required empty public constructor
    }
/*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            activity = (Activity) context;
            progressON();
        }

    }
*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressON();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("users").document(user.getUid());
        updateProfile();
        //progressOFF();
        profilePicture = view.findViewById(R.id.profilePicture);
        nickname = view.findViewById(R.id.nickname);
        bio = view.findViewById(R.id.introduction);

        view.findViewById(R.id.logoutBtn).setOnClickListener(onClickListener);
        view.findViewById(R.id.edit_profile_button).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.logoutBtn:
                    FirebaseAuth.getInstance().signOut();
                    startSignupActivity();
                    getActivity().finish();
                    break;
                case R.id.edit_profile_button:
                    startEditProfileActivity();
            }
        }
    };

    private void startSignupActivity() {
        Intent intent = new Intent(getActivity(), SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startEditProfileActivity() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void updateProfile(){
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if(userData!=null) {
                    nickname.setText(userData.getUserName());
                    bio.setText(userData.getBio());
                    if (!userData.getProfile().equals(""))
                        profilePicture.setImageURI(Uri.parse(userData.getProfile()));
                }
                progressOFF();
            }
        });
    }

}
