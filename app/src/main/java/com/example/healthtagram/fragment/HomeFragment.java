package com.example.healthtagram.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.healthtagram.R;
import com.example.healthtagram.activity.CommentActivity;
import com.example.healthtagram.activity.MainActivity;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.loading.BaseFragment;
import com.google.android.gms.auth.api.Auth;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends BaseFragment {
    public static final String TAG = "HOMEFRAGMENT";
    private FirebaseFirestore firebaseStore;
    private RecyclerView recyclerView;
    private String uid;
    private String userName;
    private String userProfile;
    private String userExplain;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_post, container, false);
        // Inflate the layout for this fragment
        progressON();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseStore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.post_recyclerView);
        recyclerView.setAdapter(new PostRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }


    public class PostRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        private ArrayList<UserPost> postList = new ArrayList<>();
        private ArrayList<String> uidList = new ArrayList<>();

        PostRecyclerViewAdapter() {
            firebaseStore.collection("posts").orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    for (QueryDocumentSnapshot doc : value) {
                        UserPost item = doc.toObject(UserPost.class);
                        postList.add(item);
                        uidList.add(doc.getId());
                    }
                    notifyDataSetChanged();
                }
            });
        }



        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
            //이미지 로딩 라이브러리 glide
            final int p = position;
            Glide.with(holder.itemView.getContext()).load(Uri.parse(postList.get(position).getPhoto())).into(holder.postImageView);
            firebaseStore.collection("users").document(postList.get(position).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserData userData = documentSnapshot.toObject(UserData.class);
                    if(userData.getProfile()!=null) {
                        Glide.with(holder.itemView.getContext()).load(Uri.parse(userData.getProfile())).into(holder.profileImageView);
                        userProfile=userData.getProfile();
                    }
                    if(userData.getUserName()!=null) {
                        holder.nameTextView.setText(userData.getUserName());
                        userName=userData.getUserName();
                    }
                }
            });
            userExplain = postList.get(position).getText();
            holder.explainTextView.setText(userExplain);
            holder.favoriteCountTextView.setText("Likes " + postList.get(position).getFavoriteCount());
            holder.favoriteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "click event");
                    favoriteEvent(p);
                }
            });
            if (postList.get(position).getFavorites().containsKey(uid)) {
                //when btn is clicked
                holder.favoriteImageView.setImageResource(R.drawable.heart_btn_clicked);
            } else {
                holder.favoriteImageView.setImageResource(R.drawable.heart_btn);
            }
            holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileFragment fragment = new ProfileFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("destinationUid",postList.get(position).getUid());
                    bundle.putString("userId",postList.get(position).getUserId());
                    fragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment).commit();
                    ((MainActivity)MainActivity.context).changeBottomNavigationItem(4);
                }
            });
            holder.postSettingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //게시물 숨기기 혹은 해당 유저 팔로우 취소
                }
            });
            holder.commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra("filename",postList.get(position).getUid()+"_"+postList.get(position).getTimestamp());
                    intent.putExtra("name",userName);
                    intent.putExtra("explain",userExplain);
                    intent.putExtra("profile",userProfile);
                    intent.putExtra("destinationUid",postList.get(position).getUid());
                    startActivity(intent);
                }
            });
            holder.explainTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra("filename",postList.get(position).getUid()+"_"+postList.get(position).getTimestamp());
                    intent.putExtra("name",userName);
                    intent.putExtra("explain",userExplain);
                    intent.putExtra("profile",userProfile);
                    intent.putExtra("destinationUid",postList.get(position).getUid());
                    startActivity(intent);
                }
            });
            if(position+1==postList.size())
                progressOFF();
        }


        @Override
        public int getItemCount() {
            return postList.size();
        }

        private void favoriteEvent(final int position) {
            final DocumentReference docRef = firebaseStore.collection("posts").document(uidList.get(position));
            firebaseStore.runTransaction(new Transaction.Function<Void>() {
                @Override
                public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    UserPost userPost = transaction.get(docRef).toObject(UserPost.class);
                    if (userPost.getFavorites().containsKey(uid)) {
                        //when btn is clicked
                        userPost.setFavoriteCount(userPost.getFavoriteCount() - 1);
                        userPost.getFavorites().remove(uid);
                    } else {
                        //when btn isn't clicked
                        userPost.setFavoriteCount(userPost.getFavoriteCount() + 1);
                        userPost.getFavorites().put(uid, true);
                        favoriteAlarm(postList.get(position).getUid());
                    }
                    transaction.set(docRef, userPost);
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Transaction success!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Transaction failure.", e);
                }
            });

        }
        private void favoriteAlarm(String destinationUid){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AlarmData alarmData = new AlarmData(user.getEmail(),user.getUid(),destinationUid,0,"",System.currentTimeMillis());
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmData);
        }

    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImageView;
        private ImageView postImageView;
        private TextView nameTextView;
        private TextView favoriteCountTextView;
        private TextView explainTextView;
        private ImageView favoriteImageView;
        private ImageView postSettingBtn;
        private ImageView commentBtn;

        public ItemViewHolder(View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.detail_view_item_image);
            profileImageView = itemView.findViewById(R.id.detail_view_profile_image);
            nameTextView = itemView.findViewById(R.id.detail_view_profile_name);
            favoriteCountTextView = itemView.findViewById(R.id.detail_view_favorite_count);
            explainTextView = itemView.findViewById(R.id.detail_view_explain);
            favoriteImageView = itemView.findViewById(R.id.detail_view_item_favorite);
            postSettingBtn = itemView.findViewById(R.id.post_setting_btn);
            commentBtn = itemView.findViewById(R.id.detail_view_item_comment);
        }

    }
}
