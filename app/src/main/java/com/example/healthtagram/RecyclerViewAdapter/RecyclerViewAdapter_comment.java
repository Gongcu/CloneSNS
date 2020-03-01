package com.example.healthtagram.RecyclerViewAdapter;

import android.content.Context;
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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
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
import com.google.firebase.firestore.DocumentChange;
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
                for (DocumentChange dc : value.getDocumentChanges()){
                    Comment item = dc.getDocument().toObject(Comment.class);
                    switch (dc.getType()){
                        case ADDED:
                            comments.add(item);
                            break;
                        case REMOVED:
                            comments.remove(item);
                            break;
                    }
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
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        //이미지 로딩 라이브러리 glide
        final int this_position = position;
        Glide.with(holder.itemView.getContext()).load(comments.get(position).getProfile()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.comment.setText(comments.get(this_position).getComment());
                holder.comment_username.setText(comments.get(this_position).getUsername());
                return false;
            }
        }).into(holder.comment_profile);

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
