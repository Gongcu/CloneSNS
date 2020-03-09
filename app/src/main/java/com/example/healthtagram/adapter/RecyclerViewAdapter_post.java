package com.example.healthtagram.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.healthtagram.CircleIndicator;
import com.example.healthtagram.R;
import com.example.healthtagram.ViewPager;
import com.example.healthtagram.activity.CommentActivity;
import com.example.healthtagram.activity.MainActivity;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.fragment.ProfileFragment;
import com.example.healthtagram.listener.PostScrollToPositionListener;
import com.example.healthtagram.loading.BaseApplication;
import com.example.healthtagram.messaging.FCMpush;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;

public class RecyclerViewAdapter_post extends RecyclerView.Adapter<RecyclerViewAdapter_post.ItemViewHolder> {
    private static final String TAG = "POST_ADAPTER";
    private int whatIsTarget = 100;
    private static final int HOME = 0;
    private static final int TIMELINE = 1;
    private BaseApplication progress=BaseApplication.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<UserPost> postList = new ArrayList<>();
    private ArrayList<String> uidList = new ArrayList<>();

    private String uid;
    private String currentUid;
    private String userProfile;
    private String userExplain;

    //Variables For Alarm Data
    private String currentUserName="";
    private String currentUserProfile="";

    private static final int FIRST = 1;
    private int isFirst = 1;
    private Long oldestTimeStamp = 99999999999999L;
    private int postCounter = 0;
    private int counter = 1;

    private int post_number = 0;
    private Long selected_item_timestamp;

    private Activity activity;
    private FragmentManager fragmentManager;
    private RecyclerView recyclerView;
    private CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("posts");
    private PostScrollToPositionListener postScrollToPositionListener; //to notify item change, so parent should know
    private SwipeRefreshLayout refreshLayout;




    /**
     * Constructor For PostActivity
     */
    public RecyclerViewAdapter_post(final Activity activity, FragmentManager fragmentManager, RecyclerView recyclerView, SwipeRefreshLayout refreshLayout) {
        whatIsTarget = HOME;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.recyclerView = recyclerView;   this.refreshLayout = refreshLayout;
        postList.clear();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //현재 유저의 이름과 프로필 사진을 가져오는 쿼리
        firestore.collection("users").document(currentUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                try {
                    currentUserName = userData.getUserName();
                    currentUserProfile=userData.getProfile();
                }catch (NullPointerException e){e.printStackTrace();}
            }
        });

        collectionReference.orderBy("timestamp", Query.Direction.DESCENDING).limit(3).get().addOnCompleteListener(onCompleteListener);
        recyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Constructor For TimelineActivity
     */
    public RecyclerViewAdapter_post(Activity activity, RecyclerView recyclerView, final String uid, final Long timestamp, SwipeRefreshLayout refreshLayout) {
        whatIsTarget = TIMELINE; this.refreshLayout=refreshLayout;
        this.activity = activity;
        this.recyclerView = recyclerView;
        postList.clear();
        this.uid = uid;
        this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.selected_item_timestamp = timestamp;
        firestore.collection("users").document(currentUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                try {
                    currentUserName = userData.getUserName();
                    currentUserProfile=userData.getProfile();
                }catch (NullPointerException e){e.printStackTrace();}
            }
        });
        //1번작업. 선택된 아이템 이후 세번째 아이템의 timestamp를 구해온 뒤
        collectionReference.whereEqualTo("uid", uid).whereLessThan("timestamp", timestamp)
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(3).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                oldestTimeStamp = timestamp;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        UserPost item = document.toObject(UserPost.class);
                        oldestTimeStamp = item.getTimestamp();
                    }
                    //2번작업. 그 timestamp보다 값이 큰 아이템들을 가져와 snapshot 리스너 사용. => 여기서 list에 값들이 추가됨
                    collectionReference.whereEqualTo("uid", uid).whereGreaterThanOrEqualTo("timestamp", oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(onCompleteListener);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        recyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Constructor For TimelineActivity's adapter
     */
    OnCompleteListener<QuerySnapshot> onCompleteListener = new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful()){
                progress.progressON(activity);
                for(QueryDocumentSnapshot item : task.getResult()) {
                    UserPost post = item.toObject(UserPost.class);
                    postList.add(post);
                    uidList.add(item.getId());
                }
                notifyDataSetChanged();
                progress.progressOFF();
                counter=post_number = postList.size();
                postCounter += postList.size();
                if (postList.size() > 0)
                    oldestTimeStamp = postList.get(postList.size() - 1).getTimestamp();
                if (isFirst == FIRST) {
                    int position = 0;
                    for (UserPost post : postList) {
                        if (post.getTimestamp().equals(selected_item_timestamp))
                            break;
                        position++;
                    }
                    postScrollToPositionListener.onSuccessListener(position);
                    isFirst = 2;
                }
                refreshLayout.setRefreshing(false);
            }
        }

    };

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return postList.get(position).getTimestamp();
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        //이미지 로딩 라이브러리 glide
        holder.setAdapter(postList.get(position).getPhoto());
        if(postList.get(position).getUserName()==null)
        firestore.collection("users").document(postList.get(position).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                userProfile = userData.getProfile();
                try { //IllegalArgumentException: You cannot start a load for a destroyed activity
                    //fallback,placeholder 사용할것
                    Glide.with(holder.itemView.getContext()).load(Uri.parse(userProfile)).error(R.drawable.main_profile).listener(requestListener).into(holder.profileImageView);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                if (userData.getUserName() != null) {
                    holder.nameTextView.setText(userData.getUserName());
                }
            }
        });
        else{
            Glide.with(holder.itemView.getContext()).load(Uri.parse(postList.get(position).getUserProfile())).error(R.drawable.main_profile).listener(requestListener).into(holder.profileImageView);
            holder.nameTextView.setText(postList.get(position).getUserName());
        }
        userExplain = postList.get(position).getText();
        holder.explainTextView.setText(userExplain);
        holder.favoriteCountTextView.setText("Likes " + postList.get(position).getFavoriteCount());

        if (postList.get(position).getFavorites().containsKey(currentUid)) {
            //when btn is clicked
            holder.favoriteImageView.setImageResource(R.drawable.heart_btn_clicked);
        } else {
            holder.favoriteImageView.setImageResource(R.drawable.heart_btn);
        }
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }


    private void favoriteEvent(final int position, final ImageView favoriteImageView, final TextView favoriteCountTextView) {
        progress.progressON(activity);
        final DocumentReference docRef = firestore.collection("posts").document(uidList.get(position));
        firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final UserPost userPost = transaction.get(docRef).toObject(UserPost.class);
                if (userPost.getFavorites().containsKey(uid)) {
                    //when btn is clicked before
                    userPost.setFavoriteCount(userPost.getFavoriteCount() - 1);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            favoriteCountTextView.setText("Likes " + userPost.getFavoriteCount());
                            favoriteImageView.setImageResource(R.drawable.heart_btn);
                            }
                        });
                    userPost.getFavorites().remove(uid);
                } else {
                    //when btn isn't clicked before
                    userPost.setFavoriteCount(userPost.getFavoriteCount() + 1);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            favoriteCountTextView.setText("Likes " + userPost.getFavoriteCount());
                            favoriteImageView.setImageResource(R.drawable.heart_btn_clicked);
                        }
                    });
                    userPost.getFavorites().put(uid, true);
                    favoriteAlarm(postList.get(position).getUid(),postList.get(position).getTimestamp());
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
                e.printStackTrace();
                progress.progressOFF();
            }
        });
    }

    private void favoriteAlarm(String destinationUid, Long timestamp) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AlarmData alarmData = new AlarmData(user.getEmail(), user.getUid(),currentUserName,currentUserProfile, destinationUid, 0, "", System.currentTimeMillis(),destinationUid+"_"+timestamp);
        FirebaseFirestore.getInstance().collection("alarms").document(alarmData.getUid()+"_"+alarmData.getTimestamp()).set(alarmData);
        FCMpush.getInstance().sendMessage(destinationUid,activity.getResources().getString(R.string.app_name),currentUserName+activity.getResources().getString(R.string.like_alarm));
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private ViewPager viewPager;
        private TextView nameTextView;
        private TextView favoriteCountTextView;
        private TextView explainTextView;
        private TextView itemCountTextView;
        private ImageView favoriteImageView;
        private ImageView commentBtn;
        private ImageView profileImageView;
        private CircleIndicator circleIndicator;

        public ItemViewHolder(View itemView) {
            super(itemView);
            viewPager = itemView.findViewById(R.id.detail_view_item_view_pager);
            profileImageView = itemView.findViewById(R.id.detail_view_profile_image);
            nameTextView = itemView.findViewById(R.id.detail_view_profile_name);
            favoriteCountTextView = itemView.findViewById(R.id.detail_view_favorite_count);
            explainTextView = itemView.findViewById(R.id.detail_view_explain);
            favoriteImageView = itemView.findViewById(R.id.detail_view_item_favorite);
            commentBtn = itemView.findViewById(R.id.detail_view_item_comment);
            itemCountTextView = itemView.findViewById(R.id.item_count_text_view);
            circleIndicator = itemView.findViewById(R.id.circle_indicator);

            profileImageView.setOnClickListener(onClickListener);
            favoriteImageView.setOnClickListener(onClickListener);
            commentBtn.setOnClickListener(onClickListener);
            explainTextView.setOnClickListener(onClickListener);

        }
        private void setAdapter(ArrayList<String> images){
            final int totalPageNumber=images.size();
            viewPager.setAdapter(new ViewPageAdapter_post(activity,images));
            viewPager.setOffscreenPageLimit(totalPageNumber);
            if(totalPageNumber>1) {
                itemCountTextView.setText(1 + "/" + totalPageNumber);
                circleIndicator.createDotPanel(totalPageNumber,R.drawable.indicator_dot_off,R.drawable.indicator_dot_on,0);
                viewPager.addOnPageChangeListener(new androidx.viewpager.widget.ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

                    @Override
                    public void onPageSelected(int position) {
                        itemCountTextView.setText((position+1)+"/"+totalPageNumber);
                        circleIndicator.selectDot(position);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {}
                });
            }
            else
                itemCountTextView.setVisibility(View.GONE);
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                switch (v.getId()) {
                    case R.id.detail_view_explain:
                    case R.id.detail_view_item_comment:
                        Intent intent = new Intent(activity, CommentActivity.class);
                        intent.putExtra("filename", postList.get(position).getUid() + "_" + postList.get(position).getTimestamp());
                        intent.putExtra("ByAlarm", true);
                        activity.startActivity(intent);
                        break;
                    case R.id.detail_view_item_favorite:
                        favoriteEvent(position,favoriteImageView,favoriteCountTextView);
                        break;
                    case R.id.detail_view_profile_image:
                        ProfileFragment fragment = new ProfileFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("destinationUid", postList.get(position).getUid());
                        bundle.putString("userId", postList.get(position).getUserId());
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit();
                        ((MainActivity) MainActivity.context).changeBottomNavigationItem(4);
                        break;
                }
            }
        };
    }

    private RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
        int counter = 0;

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            counter++;
            if (counter >= post_number) {
                progress.progressOFF();
            }
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            counter++;
            if (counter >= post_number) {
                progress.progressOFF();
            }
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
            if (!recyclerView.canScrollVertically(1) && dy > 0) {
                if (whatIsTarget == TIMELINE)
                    collectionReference.whereEqualTo("uid", uid)
                            .whereLessThan("timestamp", oldestTimeStamp)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(3).get().addOnCompleteListener(onCompleteListener);
                else if (whatIsTarget == HOME)
                    collectionReference.whereLessThan("timestamp", oldestTimeStamp)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(3).get().addOnCompleteListener(onCompleteListener);
            }
        }
    };

    public void swipeUpdate(final int whatIsTarget, SwipeRefreshLayout refreshLayout){
        postList.clear();
        uidList.clear();
        if(whatIsTarget==HOME)
            collectionReference.orderBy("timestamp", Query.Direction.DESCENDING).limit(3).get().addOnCompleteListener(onCompleteListener);
        else
            collectionReference.whereEqualTo("uid", uid).
                    whereGreaterThanOrEqualTo("timestamp", oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).
                    get().addOnCompleteListener(onCompleteListener);
    }

    public void setPostScrollToPositionListener(PostScrollToPositionListener listener) {
        this.postScrollToPositionListener = listener;
    }


}

