package com.example.healthtagram.fragment;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.healthtagram.R;
import com.example.healthtagram.activity.CommentActivity;
import com.example.healthtagram.activity.MainActivity;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.loading.BaseFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends BaseFragment {
    public static final String TAG = "HOMEFRAGMENT";
    public static final int NOT_CHANGED = 1000000;
    private FirebaseFirestore firebaseStore;
    private RecyclerView recyclerView;
    private PostRecyclerViewAdapter adapter;
    private String uid;
    private String userName;
    private String userProfile;
    private String userExplain;

    private Long oldestTimeStamp;
    private int postCounter = 0;
    private int counter = 1;
    private int bind_counter=0;
    private int changedItemPosition=1000000;
    private int post_number=0;

    private ArrayList<UserPost> postList = new ArrayList<>();
    private ArrayList<String> uidList = new ArrayList<>();

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
        postList.clear();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseStore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.post_recyclerView);
        adapter = new PostRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnScrollListener(onScrollListener);
        return view;
    }


    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            //마지막 게시글인지 체크
            if (!recyclerView.canScrollVertically(1)) {
                progressON();
                if (postCounter == counter) {
                    Toast.makeText(getActivity(), getString(R.string.loading_post), Toast.LENGTH_SHORT).show();
                }
                firebaseStore.collection("posts").whereLessThan("timestamp", oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).limit(3).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            UserPost item = doc.toObject(UserPost.class);
                            if(changedItemPosition!=NOT_CHANGED){ //아이템이 바뀐 경우 1.불러온 데이터가 해당 위치의 데이터인지 비교 2.아니라면 continue
                                if(changedItemPosition<3) {
                                    changedItemPosition=NOT_CHANGED; //첫번째 아이템들은 리사이클러뷰 어답터 스냅샷에서 처리 되므로 배제하는 코드
                                }
                                else if(postList.get(changedItemPosition).getTimestamp().equals(item.getTimestamp())) {
                                    postList.get(changedItemPosition).setFavorites(item.getFavorites());
                                    postList.get(changedItemPosition).setFavoriteCount(item.getFavoriteCount());
                                    Log.e("change","find");
                                    changedItemPosition = NOT_CHANGED;
                                    break;
                                }else {
                                    Log.e("change","continue");
                                    continue;
                                }
                            }
                            postList.add(item);
                            uidList.add(doc.getId());
                            counter++;//post 10개 20개 30개 ... 35개 등 확인을 위함 .. 더이상 불러오기 메시지가 뜨지 않게 하기 위함
                            Log.e("change","snapshot");
                        }
                        postCounter = postList.size();
                        oldestTimeStamp = postList.get(postList.size() - 1).getTimestamp();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };


    public class PostRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        PostRecyclerViewAdapter() {
            //postList.clear();
            //progressON();
            firebaseStore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    postList.clear();
                    postCounter = 0;
                    oldestTimeStamp = null;
                    for (QueryDocumentSnapshot doc : value) {
                        UserPost item = doc.toObject(UserPost.class);
                        postList.add(item);
                        Log.e("item", item.getUserId());
                        uidList.add(doc.getId());
                        counter++;
                    }
                    postCounter = postList.size();
                    post_number+=postList.size();
                    oldestTimeStamp = postList.get(postList.size() - 1).getTimestamp();
                    notifyDataSetChanged();
                }
            });
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
            bind_counter++;
            Log.e("counter b, p",bind_counter+","+post_number);
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
                    userProfile = userData.getProfile();
                    //if (!userProfile.equals(""))
                    Glide.with(holder.itemView.getContext()).load(Uri.parse(userProfile)).error(R.drawable.main_profile).listener(requestListener).into(holder.profileImageView);
                   // else
                      //  Glide.with(holder.itemView.getContext()).load(R.drawable.main_profile).into(holder.profileImageView);
                    if (userData.getUserName() != null) {
                        holder.nameTextView.setText(userData.getUserName());
                        userName = userData.getUserName();
                    }
                }
            });
            userExplain = postList.get(position).getText();
            holder.explainTextView.setText(postList.get(position).getText());
            holder.favoriteCountTextView.setText("Likes " + postList.get(position).getFavoriteCount());
            holder.favoriteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    bundle.putString("destinationUid", postList.get(position).getUid());
                    bundle.putString("userId", postList.get(position).getUserId());
                    fragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
                    ((MainActivity) MainActivity.context).changeBottomNavigationItem(4);
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
                    intent.putExtra("filename", postList.get(position).getUid() + "_" + postList.get(position).getTimestamp());
                    intent.putExtra("name", userName);
                    intent.putExtra("explain", userExplain);
                    intent.putExtra("profile", userProfile);
                    intent.putExtra("destinationUid", postList.get(position).getUid());
                    startActivity(intent);
                }
            });
            holder.explainTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra("filename", postList.get(position).getUid() + "_" + postList.get(position).getTimestamp());
                    intent.putExtra("name", userName);
                    intent.putExtra("explain", userExplain);
                    intent.putExtra("profile", userProfile);
                    intent.putExtra("destinationUid", postList.get(position).getUid());
                    startActivity(intent);
                }
            });
        }


        @Override
        public int getItemCount() {
            return postList.size();
        }

        private void favoriteEvent(final int position) {
            progressON();
            changedItemPosition=position;
            final DocumentReference docRef = firebaseStore.collection("posts").document(uidList.get(position));
            firebaseStore.runTransaction(new Transaction.Function<Void>() {
                @Override
                public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    UserPost userPost = transaction.get(docRef).toObject(UserPost.class);
                    if (userPost.getFavorites().containsKey(uid)) {
                        //when btn is clicked before
                        userPost.setFavoriteCount(userPost.getFavoriteCount() - 1);
                        userPost.getFavorites().remove(uid);
                    } else {
                        //when btn isn't clicked before
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
                    progressOFF();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Transaction failure.", e);
                    progressOFF();
                }
            });
        }

        private void favoriteAlarm(String destinationUid) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AlarmData alarmData = new AlarmData(user.getEmail(), user.getUid(), destinationUid, 0, "", System.currentTimeMillis());
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

    private RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
        int counter=0;
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            counter++;
            if(counter>=post_number) {
                Log.e("x", "x");
                progressOFF();
            }
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            counter++;
            if(counter>=post_number) {
                Log.e("x", "x");
                progressOFF();
            }
            Log.e("counter",counter+","+postCounter);
            return false;
        }
    };

}
