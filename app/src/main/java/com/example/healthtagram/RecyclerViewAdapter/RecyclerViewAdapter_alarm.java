package com.example.healthtagram.RecyclerViewAdapter;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.healthtagram.R;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.Comment;
import com.example.healthtagram.database.UserData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import io.opencensus.resource.Resource;

public class RecyclerViewAdapter_alarm extends RecyclerView.Adapter<RecyclerViewAdapter_alarm.ItemViewHolder> {
    public static final String TAG = "COMMENT_RECYCLERVIEW";
    private ArrayList<AlarmData> alarmList = new ArrayList<>();
    public static final int LIKE = 0;
    public static final int COMMENT = 1;
    public static final int FOLLOW = 2;

    public RecyclerViewAdapter_alarm(String uid) {
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        firestore.collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                alarmList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    AlarmData item = doc.toObject(AlarmData.class);
                    alarmList.add(item);
                    Log.e(TAG, "Listen success.");
                }
                notifyDataSetChanged();
            }
        });
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
        String text="";
        switch (alarmList.get(position).getKind()){
            case LIKE:
                text="님이 회원님의 게시글을 좋아합니다.";
                break;
            case COMMENT:
                text="님니 회원심의 게시글에 댓글을 남겼습니다.";
                break;
            case FOLLOW:
                text="님이 회원님을 팔로우 하기 시작했습니다.";
                break;
        }
        holder.comment.setText(text);
        FirebaseFirestore.getInstance().collection("users").document(alarmList.get(position).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if (!userData.getProfile().equals("")) {
                    Glide.with(holder.itemView.getContext()).load(Uri.parse(userData.getProfile())).into(holder.alarm_profile);
                }
                if (!userData.getUserName().equals(""))
                    holder.alarm_username.setText(userData.getUserName());
            }
        });
    }


    @Override
    public int getItemCount() {
        return alarmList.size();
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView alarm_profile;
        private TextView alarm_username;
        private TextView comment;

        public ItemViewHolder(View itemView) {
            super(itemView);
            alarm_profile = itemView.findViewById(R.id.comment_profile);
            alarm_username = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.commentText);
        }

    }
}
