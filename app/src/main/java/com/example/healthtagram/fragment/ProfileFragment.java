package com.example.healthtagram.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.healthtagram.R;
import com.example.healthtagram.activity.EditProfileActivity;
import com.example.healthtagram.activity.SignupActivity;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.listener.EditProfileListener;
import com.example.healthtagram.loading.BaseApplication;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.loading.BaseFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.auth.User;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.facebook.share.internal.DeviceShareDialogFragment.TAG;


public class ProfileFragment extends BaseFragment {
    private TextView nickname, bio;
    private TextView postNumber,followerNumber,followingNumber;
    private ImageView profilePicture;
    private Button button;
    private Button editProfileBtn;
    private FirebaseFirestore db;
    private FirebaseFirestore firebaseStore;
    private FirebaseUser user;
    private DocumentReference docRef;
    private RecyclerView accountRecyclerView;
    private String uid;
    private ArrayList<String> following_uid_list;
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
        db = FirebaseFirestore.getInstance();
        uid = getArguments().getString("destinationUid");
        firebaseStore = FirebaseFirestore.getInstance();

        button=view.findViewById(R.id.logoutBtn);
        profilePicture = view.findViewById(R.id.profilePicture);
        nickname = view.findViewById(R.id.nickname);
        bio = view.findViewById(R.id.introduction);
        postNumber = view.findViewById(R.id.post_number);
        followerNumber = view.findViewById(R.id.follower_number);
        followingNumber = view.findViewById(R.id.following_number);
        accountRecyclerView = view.findViewById(R.id.accountRecyclerView);
        editProfileBtn = view.findViewById(R.id.edit_profile_button);

        if(uid.equals(user.getUid())){
            //my profile
            updateProfile(user.getUid());
            button.setOnClickListener(onClickListener);
            editProfileBtn.setOnClickListener(onClickListener);
            accountRecyclerView.setAdapter(new AccountRecyclerViewAdapter(user.getUid()));
        }else{
            //others profile
            updateProfile(uid);
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserData userData = documentSnapshot.toObject(UserData.class);
                    try {
                        if(userData.getFollow().containsKey(uid)) //현재 보고있는 프로필(uid)가 팔로우 한 적이 있는지 확인
                            button.setText("팔로우 취소");
                        else
                            button.setText("팔로우");
                    }catch (Exception e){e.printStackTrace();}
                }
            });
            button.setOnClickListener(onFollowClickListener);
            editProfileBtn.setVisibility(View.GONE);
            accountRecyclerView.setAdapter(new AccountRecyclerViewAdapter(uid));
        }
        accountRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        //progressOFF();

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

    View.OnClickListener onFollowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            followEvent(uid);
        }
    };

    private void startSignupActivity() {
        Intent intent = new Intent(getActivity(), SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startEditProfileActivity() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        intent.putExtra("state",IS_NOT_FIRST_ACCESS);
        startActivity(intent);
    }

    public void updateProfile(String uid){
        docRef = db.collection("users").document(uid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if(userData!=null) {
                    nickname.setText(userData.getUserName());
                    bio.setText(userData.getBio());
                    followerNumber.setText(userData.getFollower_count()+"");
                    followingNumber.setText(userData.getFollowing_count()+"");
                    if (!userData.getProfile().equals(""))
                        profilePicture.setImageURI(Uri.parse(userData.getProfile()));
                }
                progressOFF();
            }
        });
    }

    public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<UserPost> postList = new ArrayList<>();

        AccountRecyclerViewAdapter(String uid){
            firebaseStore.collection("posts").whereEqualTo("uid",uid).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        return;
                    }
                    for (QueryDocumentSnapshot doc : value) {
                        UserPost item = doc.toObject(UserPost.class);
                        postList.add(item);
                    }
                    postNumber.setText((postList.size())+"");
                    //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
                    //lp.gravity = Gravity.CENTER;
                    //tv.setLayoutParams(lp);
                    notifyDataSetChanged();
                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int width = getResources().getDisplayMetrics().widthPixels/3;
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width,width));
            return new CustomViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ImageView imageView = ((CustomViewHolder)holder).imageView;
            Glide.with(holder.itemView.getContext()).load(postList.get(position).getPhoto()).apply(RequestOptions.centerCropTransform()).into(imageView);
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }


        class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            public CustomViewHolder(@NonNull ImageView imageView) {
                super(imageView);
                this.imageView = (ImageView) imageView;
            }
        }
    }

    private void followEvent(final String uid) { //destinationUid를 넘겨받음
        final DocumentReference myRef = firebaseStore.collection("users").document(user.getUid()); //본인 uid
        final DocumentReference destinationRef = firebaseStore.collection("users").document(uid); //본인 uid
        firebaseStore.runTransaction(new Transaction.Function<Void>() {
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
                } else {
                    //when doesn't followed
                    userData.setFollowing_count(userData.getFollowing_count() + 1);
                    userData.getFollow().put(uid, true);
                    otherUserData.setFollower_count(otherUserData.getFollower_count()+1);
                    followerNumber.setText((Integer.parseInt(followerNumber.getText().toString())+1)+"");
                    button.setText("팔로우 취소");
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

}
