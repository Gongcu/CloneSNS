package com.example.healthtagram.RecyclerViewAdapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.healthtagram.fragment.ProfileFragment;
import com.example.healthtagram.loading.BaseApplication;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;

public class RecyclerViewAdapter_post extends RecyclerView.Adapter<RecyclerViewAdapter_post.ItemViewHolder> {
    private BaseApplication progress;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<UserPost> postList= new ArrayList<>();
    private ArrayList<String> uidList = new ArrayList<>();

    private String uid;
    private String userProfile;
    private String userName;
    private String userExplain;

    private int state=100;
    public static final int INIT = 200;
    public static final int NOT_CHANGED = 1000000;
    private Long oldestTimeStamp=9999999999999l;
    private int postCounter = 0;
    private int counter = 1;
    private int changedItemPosition=1000000;
    private int post_number=0;

    private Activity activity;
    private FragmentManager fragmentManager;
    private RecyclerView recyclerView;

    public RecyclerViewAdapter_post(Activity activity, FragmentManager fragmentManager, RecyclerView recyclerView) {
        progress = BaseApplication.getInstance();
        this.activity = activity; this.fragmentManager = fragmentManager; this.recyclerView = recyclerView; postList.clear();
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                Log.e("adapter","constructor call");
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
                post_number += postList.size();
                oldestTimeStamp = postList.get(postList.size() - 1).getTimestamp();
                notifyDataSetChanged();
            }
        });
        recyclerView.addOnScrollListener(onScrollListener);
    }

    public RecyclerViewAdapter_post(Activity activity, RecyclerView recyclerView,String uid,Long timestamp) {
        progress = BaseApplication.getInstance();
        this.activity = activity; this.recyclerView = recyclerView; postList.clear();
        this.uid=uid;
        firestore.collection("posts").whereEqualTo("uid",uid).orderBy("timestamp", Query.Direction.DESCENDING).limit(3).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                Log.e("adapter","constructor call");
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
                post_number += postList.size();
                oldestTimeStamp = postList.get(postList.size() - 1).getTimestamp();
                Log.e("oldestCon",oldestTimeStamp+"");
                notifyDataSetChanged();
            }
        });
        recyclerView.addOnScrollListener(onScrollListener_timeline);
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
        firestore.collection("users").document(postList.get(position).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                userProfile = userData.getProfile();
                Glide.with(holder.itemView.getContext()).load(Uri.parse(userProfile)).error(R.drawable.main_profile).listener(requestListener).into(holder.profileImageView);
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
                fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit();
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
                Intent intent = new Intent(activity, CommentActivity.class);
                intent.putExtra("filename", postList.get(position).getUid() + "_" + postList.get(position).getTimestamp());
                intent.putExtra("name", userName);
                intent.putExtra("explain", userExplain);
                intent.putExtra("profile", userProfile);
                intent.putExtra("destinationUid", postList.get(position).getUid());
                activity.startActivity(intent);
            }
        });
        holder.explainTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, CommentActivity.class);
                intent.putExtra("filename", postList.get(position).getUid() + "_" + postList.get(position).getTimestamp());
                intent.putExtra("name", userName);
                intent.putExtra("explain", userExplain);
                intent.putExtra("profile", userProfile);
                intent.putExtra("destinationUid", postList.get(position).getUid());
                activity.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    private void favoriteEvent(final int position) {
        progress.progressON(activity);
        changedItemPosition = position;
        final DocumentReference docRef = firestore.collection("posts").document(uidList.get(position));
        firestore.runTransaction(new Transaction.Function<Void>() {
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
                progress.progressOFF();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.progressOFF();
            }
        });
    }

    private void favoriteAlarm(String destinationUid) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AlarmData alarmData = new AlarmData(user.getEmail(), user.getUid(), destinationUid, 0, "", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmData);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
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
            layout = itemView.findViewById(R.id.item_detail_layout);
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
                progress.progressOFF();
            }
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            counter++;
            if(counter>=post_number) {
                Log.e("x", "x");
                progress.progressOFF();
            }
            Log.e("counter",counter+","+postCounter);
            return false;
        }
    };


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
                progress.progressON(activity);
                if (postCounter == counter) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.loading_post), Toast.LENGTH_SHORT).show();
                }
                firestore.collection("posts").whereLessThan("timestamp", oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).limit(3).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            UserPost item = doc.toObject(UserPost.class);
                            if(changedItemPosition!=NOT_CHANGED){ //아이템이 바뀐 경우 1.불러온 데이터가 해당 위치의 데이터인지 비교 2.아니라면 continue
                                Log.e("post size ",":"+postList.size());
                                if(changedItemPosition<3) {
                                    changedItemPosition=NOT_CHANGED; //첫번째 아이템들은 리사이클러뷰 어답터 스냅샷에서 처리 되므로 배제하는 코드
                                }
                                else if(postList.get(changedItemPosition).getTimestamp().equals(item.getTimestamp())) { //java.lang.IndexOutOfBoundsException: Index: 6, Size: 3
                                    postList.get(changedItemPosition).setFavorites(item.getFavorites());
                                    postList.get(changedItemPosition).setFavoriteCount(item.getFavoriteCount());
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
                        notifyDataSetChanged();
                    }
                });
            }
        }
    };

    RecyclerView.OnScrollListener onScrollListener_timeline = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!recyclerView.canScrollVertically(1)&&dy>0) {
                progress.progressON(activity);
                if (postCounter == counter) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.loading_post), Toast.LENGTH_SHORT).show();
                }
                Log.e("oldestScrolbefore",oldestTimeStamp+"");
                firestore.collection("posts").whereEqualTo("uid",uid).whereLessThan("timestamp", oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).limit(3).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            UserPost item = doc.toObject(UserPost.class);
                            if(changedItemPosition!=NOT_CHANGED){ //아이템이 바뀐 경우 1.불러온 데이터가 해당 위치의 데이터인지 비교 2.아니라면 continue
                                Log.e("post size ",":"+postList.size());
                                if(changedItemPosition<3) {
                                    changedItemPosition=NOT_CHANGED; //첫번째 아이템들은 리사이클러뷰 어답터 스냅샷에서 처리 되므로 배제하는 코드
                                }
                                else if(postList.get(changedItemPosition).getTimestamp().equals(item.getTimestamp())) {
                                    postList.get(changedItemPosition).setFavorites(item.getFavorites());
                                    postList.get(changedItemPosition).setFavoriteCount(item.getFavoriteCount());
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
                        Log.e("oldestScrol",oldestTimeStamp+"");
                        notifyDataSetChanged();
                    }
                });
            }
        }
    };
}

