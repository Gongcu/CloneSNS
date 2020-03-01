package com.example.healthtagram.RecyclerViewAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.healthtagram.R;
import com.example.healthtagram.activity.CommentActivity;
import com.example.healthtagram.activity.TimelineActivity;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.Comment;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.fragment.HistoryFragment;
import com.example.healthtagram.fragment.HomeFragment;
import com.example.healthtagram.loading.BaseApplication;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

import io.opencensus.resource.Resource;

public class RecyclerViewAdapter_alarm extends RecyclerView.Adapter<RecyclerViewAdapter_alarm.ItemViewHolder> {
    public static final String TAG = "COMMENT_RECYCLERVIEW";
    private ArrayList<AlarmData> alarmList = new ArrayList<>();
    public static final int LIKE = 0;
    public static final int COMMENT = 1;
    public static final int FOLLOW = 2;

    private int item_counter = 0;
    private int times = 1; //스크롤 횟수
    private Activity activity;
    private BaseApplication progressDialog;
    private String uid;
    private RecyclerView recyclerView;
    private Long oldestTimeStamp;

    public RecyclerViewAdapter_alarm(Activity activity, String uid, RecyclerView recyclerView) {
        this.uid = uid;
        this.activity = activity;
        this.recyclerView = recyclerView;
        progressDialog = BaseApplication.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("alarms").whereEqualTo("destinationUid", uid).orderBy("timestamp", Query.Direction.DESCENDING).limit(10).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                for (DocumentChange dc : value.getDocumentChanges())
                    switch (dc.getType()) {
                        case ADDED:
                            AlarmData item = dc.getDocument().toObject(AlarmData.class);
                            alarmList.add(item);
                    }

                item_counter = alarmList.size();
                if (alarmList.size() > 0)
                    oldestTimeStamp = alarmList.get(alarmList.size() - 1).getTimestamp();
                notifyDataSetChanged();
            }
        });
        recyclerView.addOnScrollListener(listener);
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ItemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        //이미지 로딩 라이브러리 glide
        String text = "";
        holder.ALARM_TYPE = alarmList.get(position).getKind();
        switch (holder.ALARM_TYPE) {
            case LIKE:
                text = "님이 회원님의 게시글을 좋아합니다.";
                break;
            case COMMENT:
                text = "님이 회원심의 게시글에 댓글을 남겼습니다.";
                break;
            case FOLLOW:
                text = "님이 회원님을 팔로우 하기 시작했습니다.";
                break;
        }
        final String comment = text;
        Glide.with(holder.itemView.getContext()).load(Uri.parse(alarmList.get(position).getUserProfile())).error(R.drawable.main_profile).listener(requestListener).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.alarm_username.setText(alarmList.get(position).getUsername());
                holder.comment.setText(comment);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.alarm_username.setText(alarmList.get(position).getUsername());
                holder.comment.setText(comment);
                return false;
            }
        }).into(holder.alarm_profile);
    }


    @Override
    public int getItemCount() {
        return alarmList.size();
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private int ALARM_TYPE = 100;
        private ImageView alarm_profile;
        private TextView alarm_username;
        private TextView comment;

        public ItemViewHolder(View itemView) {
            super(itemView);
            alarm_profile = itemView.findViewById(R.id.comment_profile);
            alarm_username = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.commentText);
            itemView.setOnClickListener(onClickListener);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //posts 값을 가지고 해당 아이템으로 이동
                switch (ALARM_TYPE) {
                    case LIKE:
                        startActivity(TimelineActivity.class, alarmList.get(getAdapterPosition()).getPostFileName());
                        //uid, timestamp 넘겨야함
                        break;
                    case COMMENT:
                        startActivity(CommentActivity.class, alarmList.get(getAdapterPosition()).getPostFileName());
                        break;
                    case FOLLOW:
                        //main actvity, profile fragment 참조
                        activity.finish();
                        //fragment로 이동
                        break;
                }
            }
        };
    }

    private void startActivity(Class activity, String item) {
        Intent intent = new Intent(this.activity, activity);
        String uid;
        Long timestamp;
        if (activity == TimelineActivity.class) {
            uid = item.substring(0, 28);
            timestamp = Long.parseLong(item.substring(29));
            intent.putExtra("uid", uid);
            intent.putExtra("timestamp", timestamp);
        } else {
            intent.putExtra("filename", item);
            intent.putExtra("ByAlarm", true);
        }
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    private RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
        int counter = 0;

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            counter++;
            if (counter >= item_counter) {
                Log.e("glide, cout", counter + ", " + item_counter);
                progressDialog.progressOFF();
            }
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            counter++;
            if (counter >= item_counter) {
                Log.e("glide, cout", counter + ", " + item_counter);
                progressDialog.progressOFF();
            }
            return false;
        }
    };

    RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            //마지막 게시글인지 체크
            if (item_counter <= times * 10) {
                progressDialog.progressOFF();
            }
            if (!recyclerView.canScrollVertically(1)&&dy>0) {
                progressDialog.progressON(activity);
                FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).whereLessThan("timestamp", oldestTimeStamp).orderBy("timestamp", Query.Direction.DESCENDING).limit(10).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            AlarmData item = doc.toObject(AlarmData.class);
                            alarmList.add(item);
                        }
                        item_counter = alarmList.size();
                        times++;
                        oldestTimeStamp = alarmList.get(alarmList.size() - 1).getTimestamp();
                        notifyDataSetChanged();
                    }
                });
            }
        }
    };

}
