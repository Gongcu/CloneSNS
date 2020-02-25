package com.example.healthtagram.RecyclerViewAdapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.healthtagram.R;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.UserPost;
import com.facebook.login.LoginManager;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING;

public class RecyclerViewAdapter_grid extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<UserPost> postList = new ArrayList<>();
    private Activity activity;
    private RecyclerView recyclerView;
    private String uid;
    private Long oldestTimeStamp;
    private int item_counter=0;
    private int times=0;

    /**
     *
        THIS ADAPTER IS FOR PROFILE FRAGMENT
     */
    public RecyclerViewAdapter_grid(String uid, final TextView postNumber, Activity activity, RecyclerView recyclerView){
        this.uid=uid;this.activity=activity; this.recyclerView = recyclerView;
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        firestore.collection("posts").whereEqualTo("uid",uid).orderBy("timestamp",Query.Direction.DESCENDING).limit(12).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                item_counter=postList.size();
                oldestTimeStamp=postList.get(postList.size()-1).getTimestamp();
                postNumber.setText((postList.size())+"");
                notifyDataSetChanged();
            }
        });
        recyclerView.addOnScrollListener(listener_profile);
    }

    /**
     *
     THIS ADAPTER IS FOR SEARCH FRAGMENT
     */
    public RecyclerViewAdapter_grid(Activity activity, RecyclerView recyclerView){
        this.activity=activity; this.recyclerView=recyclerView;
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        firestore.collection("posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(12).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                item_counter=postList.size();
                oldestTimeStamp=postList.get(postList.size()-1).getTimestamp();
                notifyDataSetChanged();
            }
        });
        recyclerView.addOnScrollListener(listener_search);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int width = activity.getResources().getDisplayMetrics().widthPixels/3;
        ImageView imageView = new ImageView(parent.getContext());
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(width,width);
        layoutParams.setMargins(2,2,2,2);
        imageView.setLayoutParams(layoutParams);
        return new RecyclerViewAdapter_grid.CustomViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ImageView imageView = ((RecyclerViewAdapter_grid.CustomViewHolder)holder).imageView;
        Glide.with(holder.itemView.getContext()).load(postList.get(position).getPhoto()).apply(RequestOptions.centerCropTransform()).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭시 해당 유저의 포스트들만 homefragment처럼 보며줌 프로필바텀아이템 클릭상태
            }
        });
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

    RecyclerView.OnScrollListener listener_search = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            //마지막 게시글인지 체크
            if(item_counter<=times*12){
                //
            }
            if (!recyclerView.canScrollVertically(1)) {
                FirebaseFirestore.getInstance().collection("posts").whereLessThan("timestamp",oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).limit(12).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        item_counter=postList.size();
                        times++;
                        oldestTimeStamp = postList.get(postList.size() - 1).getTimestamp();
                        notifyDataSetChanged();
                    }
                });
            }
        }
    };
    RecyclerView.OnScrollListener listener_profile = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            //마지막 게시글인지 체크
            if(item_counter<=times*12){
                //
            }
            if (!recyclerView.canScrollVertically(1)) {
                FirebaseFirestore.getInstance().collection("posts").whereEqualTo("uid",uid).whereLessThan("timestamp",oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).limit(12).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        item_counter=postList.size();
                        times++;
                        oldestTimeStamp = postList.get(postList.size() - 1).getTimestamp();
                        notifyDataSetChanged();
                    }
                });
            }else{
                //끝이 위에 닿았을때
            }
        }
    };
}
