package com.example.healthtagram.RecyclerViewAdapter;

import android.content.Context;
import android.util.Log;
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
import com.example.healthtagram.database.UserPost;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecyclerViewAdapter_grid extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<UserPost> postList = new ArrayList<>();
    private Context context;
    private TextView postNumber;


    /**
     *
        THIS ADAPTER IS FOR PROFILE FRAGMENT
     */
    public RecyclerViewAdapter_grid(String uid, final TextView postNumber, Context context){
        this.context=context;
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        firestore.collection("posts").whereEqualTo("uid",uid).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                postNumber.setText((postList.size())+"");
                notifyDataSetChanged();
            }
        });
    }

    /**
     *
     THIS ADAPTER IS FOR SEARCH FRAGMENT
     */
    public RecyclerViewAdapter_grid(Context context){
        this.context=context;
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        firestore.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int width = context.getResources().getDisplayMetrics().widthPixels/3;
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width,width));
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
}
