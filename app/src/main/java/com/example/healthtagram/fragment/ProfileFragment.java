package com.example.healthtagram.fragment;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.healthtagram.RecyclerViewAdapter.RecyclerViewAdapter_grid;
import com.example.healthtagram.R;
import com.example.healthtagram.activity.EditProfileActivity;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.loading.BaseFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;


public class ProfileFragment extends BaseFragment {
    private TextView nickname, bio;
    private TextView postNumber,followerNumber,followingNumber;
    private ImageView profilePicture;
    private Button button;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private DocumentReference docRef;
    private RecyclerView accountRecyclerView;
    private RecyclerViewAdapter_grid adapter;

    private String uid;
    private String currentUserNmae="";
    private String currentUserProfile="";


    private static final int IS_NOT_FIRST_ACCESS = 20;
    public ProfileFragment() {
        // Required empty public constructor
    }

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
        firestore = FirebaseFirestore.getInstance();
        uid = getArguments().getString("destinationUid");

        profilePicture = view.findViewById(R.id.profilePicture);
        nickname = view.findViewById(R.id.nickname);
        bio = view.findViewById(R.id.introduction);
        postNumber = view.findViewById(R.id.post_number);
        followerNumber = view.findViewById(R.id.follower_number);
        followingNumber = view.findViewById(R.id.following_number);
        accountRecyclerView = view.findViewById(R.id.accountRecyclerView);
        button = view.findViewById(R.id.edit_profile_button);

        if(uid.equals(user.getUid())){
            //my profile
            updateProfile(user.getUid()); //본인 uid
            button.setOnClickListener(onClickListener);
            adapter=new RecyclerViewAdapter_grid(user.getUid(),postNumber,getActivity(),accountRecyclerView);
        }else{
            //others profile
            updateProfile(uid); //타인 uid
            DocumentReference docRef = firestore.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserData userData = documentSnapshot.toObject(UserData.class);
                    try {
                        if(userData.getFollow().containsKey(uid)) { //현재 보고있는 프로필(uid)가 팔로우 한 적이 있는지 확인
                            button.setText("팔로우 취소");
                            button.setTextColor(getResources().getColor(R.color.black));
                            button.setBackgroundResource(R.drawable.rounded_white_view);
                        }
                        else {
                            button.setText("팔로우");
                            button.setTextColor(getResources().getColor(R.color.white));
                            button.setBackgroundResource(R.drawable.rounded_blue_view);
                        }
                    }catch (Exception e){e.printStackTrace();}
                }
            });
            button.setOnClickListener(onFollowClickListener);
            adapter=new RecyclerViewAdapter_grid(uid,postNumber,getActivity(),accountRecyclerView);
        }
        accountRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.edit_profile_button:
                    startEditProfileActivity();
            }
        }
    };

    View.OnClickListener onFollowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            followEvent(uid);
        }
    };


    private void startEditProfileActivity() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        intent.putExtra("state",IS_NOT_FIRST_ACCESS);
        startActivity(intent);
    }

    public void updateProfile(String uid){
        docRef = firestore.collection("users").document(uid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final UserData userData = documentSnapshot.toObject(UserData.class);
                if(userData!=null) {
                    if (!userData.getProfile().equals(""))
                        Glide.with(getActivity()).load(Uri.parse(userData.getProfile())).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressOFF();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                nickname.setText(userData.getUserName());
                                bio.setText(userData.getBio());
                                followerNumber.setText((userData.getFollower_count()+""));
                                followingNumber.setText((userData.getFollowing_count()+""));
                                adapter.setPostNumber();
                                accountRecyclerView.setAdapter(adapter);
                                progressOFF();
                                return false;
                            }
                        }).into(profilePicture);
                    else
                        profilePicture.setImageResource(R.drawable.main_profile);
                }
            }
        });
    }

    private void followEvent(final String uid) { //destinationUid를 넘겨받음
        final DocumentReference myRef = firestore.collection("users").document(user.getUid()); //본인 uid
        final DocumentReference destinationRef = firestore.collection("users").document(uid); //본인 uid
        firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                UserData userData = transaction.get(myRef).toObject(UserData.class);
                UserData otherUserData = transaction.get(destinationRef).toObject(UserData.class);
                if (userData.getFollow().containsKey(uid)) {
                    //when followed
                    userData.setFollowing_count(userData.getFollowing_count() - 1);
                    userData.getFollow().remove(uid);
                    otherUserData.setFollower_count(otherUserData.getFollower_count()-1);
                    followerNumber.setText((Integer.parseInt(followerNumber.getText().toString())-1)+"");
                    button.setText("팔로우");
                    button.setTextColor(getResources().getColor(R.color.white));
                    button.setBackgroundResource(R.drawable.rounded_blue_view);
                } else {
                    //when doesn't followed
                    userData.setFollowing_count(userData.getFollowing_count() + 1);
                    userData.getFollow().put(uid, true);
                    otherUserData.setFollower_count(otherUserData.getFollower_count()+1);
                    followerNumber.setText((Integer.parseInt(followerNumber.getText().toString())+1)+"");
                    currentUserNmae=userData.getUserName();
                    currentUserProfile=userData.getProfile();
                    followAlarm(uid);
                    button.setText("팔로우 취소");
                    button.setTextColor(getResources().getColor(R.color.black));
                    button.setBackgroundResource(R.drawable.rounded_white_view);
                }
                transaction.set(myRef, userData);
                transaction.set(destinationRef, otherUserData);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("transaction", "Transaction failure.", e);
            }
        });

    }

    private void followAlarm(String destinationUid){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AlarmData alarmData = new AlarmData(user.getEmail(),user.getUid(),currentUserNmae,currentUserProfile,destinationUid,2,"",System.currentTimeMillis(),"");
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmData);
    }
}
