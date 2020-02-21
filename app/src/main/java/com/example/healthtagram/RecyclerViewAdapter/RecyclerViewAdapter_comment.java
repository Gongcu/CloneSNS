package com.example.healthtagram.RecyclerViewAdapter;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.healthtagram.R;
import com.example.healthtagram.activity.CommentActivity;
import com.example.healthtagram.activity.MainActivity;
import com.example.healthtagram.database.Comment;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.fragment.HomeFragment;
import com.example.healthtagram.fragment.ProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;

import static com.facebook.share.internal.DeviceShareDialogFragment.TAG;

public class RecyclerViewAdapter_comment extends RecyclerView.Adapter<RecyclerViewAdapter_comment.ItemViewHolder> {
    public static final String TAG = "COMMENT_RECYCLERVIEW";
    private ArrayList<Comment> comments = new ArrayList<>();


    public RecyclerViewAdapter_comment(String filename) {
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        firestore.collection("posts").document(filename).collection("comments").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                for (QueryDocumentSnapshot doc : value) {
                    Comment item = doc.toObject(Comment.class);
                    comments.add(item);
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
        holder.comment.setText(comments.get(position).getComment());
        FirebaseFirestore.getInstance().collection("users").document(comments.get(position).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if (!userData.getProfile().equals("")) {
                    Glide.with(holder.itemView.getContext()).load(Uri.parse(userData.getProfile())).into(holder.comment_profile);
                }
                if (!userData.getUserName().equals(""))
                    holder.comment_username.setText(userData.getUserName());
            }
        });
    }


    @Override
    public int getItemCount() {
        return comments.size();
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView comment_profile;
        private TextView comment_username;
        private TextView comment;

        public ItemViewHolder(View itemView) {
            super(itemView);
            comment_profile = itemView.findViewById(R.id.comment_profile);
            comment_username = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.commentText);
        }

    }
}
