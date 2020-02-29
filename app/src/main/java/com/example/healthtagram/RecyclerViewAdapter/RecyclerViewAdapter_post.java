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
import androidx.recyclerview.widget.LinearSmoothScroller;
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
import com.example.healthtagram.fragment.HomeFragment;
import com.example.healthtagram.fragment.ProfileFragment;
import com.example.healthtagram.listener.PostScrollToPositionListener;
import com.example.healthtagram.loading.BaseApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class RecyclerViewAdapter_post extends RecyclerView.Adapter<RecyclerViewAdapter_post.ItemViewHolder> {
    public static final String TAG = "POST_ADAPTER";
    private int whatIsTarget = 100;
    public static final int HOME = 0;
    public static final int TIMELINE = 1;
    private BaseApplication progress;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<UserPost> postList = new ArrayList<>();
    private ArrayList<String> uidList = new ArrayList<>();

    private String uid;
    private String currentUid;
    private String userProfile;
    private String userName;
    private String userExplain;

    public static final int NOT_CHANGED = 1000000;
    public static final int FIRST = 1;
    private int isFirst = 1;
    private Long oldestTimeStamp = 99999999999999L;
    private int postCounter = 0;
    private int counter = 1;
    private int changedItemPosition = 1000000;
    private int post_number = 0;
    private Long selected_item_timestamp;

    private Activity activity;
    private FragmentManager fragmentManager;
    private RecyclerView recyclerView;
    private CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("posts");
    private PostScrollToPositionListener postScrollToPositionListener; //to notify item change, so parent should know

    /**
     * Constructor For PostActivity
     */
    public RecyclerViewAdapter_post(Activity activity, FragmentManager fragmentManager, RecyclerView recyclerView) {
        whatIsTarget = HOME;
        progress = BaseApplication.getInstance();
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.recyclerView = recyclerView;
        postList.clear();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        collectionReference.orderBy("timestamp", Query.Direction.DESCENDING).limit(3).addSnapshotListener(postListener);
        recyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Constructor For TimelineActivity
     */
    public RecyclerViewAdapter_post(Activity activity, RecyclerView recyclerView, final String uid, final Long timestamp) {
        whatIsTarget = TIMELINE;
        progress = BaseApplication.getInstance();
        this.activity = activity;
        this.recyclerView = recyclerView;
        postList.clear();
        this.uid = uid;
        this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.selected_item_timestamp = timestamp;

        Log.e("Timestamp", "getIntent" + timestamp);
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
                    //2번작업. 그 timestamp보다 값이 큰 아이템들을 가져와 snapshot 리스너 사용.
                    collectionReference.whereEqualTo("uid", uid).whereGreaterThanOrEqualTo("timestamp", oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(postListener);
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
    private EventListener<QuerySnapshot> postListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            int index = 0;
            progress.progressON(activity);
            for (DocumentChange dc : value.getDocumentChanges()) { //https://stackoverflow.com/questions/53439196/firestore-query-listener 바뀐 데이터 스냅샷만 가져오기
                UserPost item = dc.getDocument().toObject(UserPost.class);
                switch (dc.getType()) {
                    case ADDED:
                        Log.e(whatIsTarget+"/0:HOME,1:TIME", dc.getDocument().getId() + " => add");
                        postList.add(postList.size(), item);
                        uidList.add(uidList.size(), dc.getDocument().getId());
                        notifyItemChanged(postList.size());
                        counter++;
                        break;
                    case MODIFIED:
                        Log.e(whatIsTarget+"/0:HOME,1:TIME", dc.getDocument().getId() + " => modi");
                        for (UserPost post : postList) {
                            if (post.getTimestamp().equals(item.getTimestamp()))
                                break;
                            index++;
                        }
                        Log.e(whatIsTarget+"/0:HOME,1:TIME", index + " => modi");
                        postList.set(index, item);
                        notifyItemChanged(index);
                        recyclerView.smoothScrollToPosition(index);
                        break;
                    case REMOVED:
                        Log.e(whatIsTarget+"/0:HOME,1:TIME", dc.getDocument().getId() + " => remove");
                        postList.remove(item);
                        uidList.remove(dc.getDocument().getId());
                        notifyDataSetChanged();
                        break;
                }
            }
            progress.progressOFF();
            post_number = postList.size();
            postCounter += postList.size();
            if (postList.size() > 1)
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
            } else {
                //if(index!=0)
                //postScrollToPositionListener.onSuccessListener(index);
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
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        //이미지 로딩 라이브러리 glide
        Glide.with(holder.itemView.getContext()).load(Uri.parse(postList.get(position).getPhoto())).into(holder.postImageView);
        firestore.collection("users").document(postList.get(position).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                userProfile = userData.getProfile();
                try { //IllegalArgumentException: You cannot start a load for a destroyed activity
                    Glide.with(holder.itemView.getContext()).load(Uri.parse(userProfile)).error(R.drawable.main_profile).listener(requestListener).into(holder.profileImageView);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                if (userData.getUserName() != null) {
                    holder.nameTextView.setText(userData.getUserName());
                    userName = userData.getUserName();
                }
            }
        });
        userExplain = postList.get(position).getText();
        holder.explainTextView.setText(postList.get(position).getText());
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

            profileImageView.setOnClickListener(onClickListener);
            favoriteImageView.setOnClickListener(onClickListener);
            postSettingBtn.setOnClickListener(onClickListener);
            commentBtn.setOnClickListener(onClickListener);
            explainTextView.setOnClickListener(onClickListener);
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
                        intent.putExtra("name", userName);
                        intent.putExtra("explain", userExplain);
                        intent.putExtra("profile", userProfile);
                        intent.putExtra("destinationUid", postList.get(position).getUid());
                        activity.startActivity(intent);
                        break;
                    case R.id.detail_view_item_favorite:
                        favoriteEvent(position);
                        Log.e("favorite", "click");
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
                    case R.id.post_setting_btn:
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
            Log.e("counter", counter + "," + postCounter);
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
                            .whereLessThan("timestamp", oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(3).addSnapshotListener(postListener);
                else if (whatIsTarget == HOME)
                    collectionReference.whereLessThan("timestamp", oldestTimeStamp)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(3).addSnapshotListener(postListener);
            }
        }
    };

    public void setPostScrollToPositionListener(PostScrollToPositionListener listener) {
        this.postScrollToPositionListener = listener;
    }
}

