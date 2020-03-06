package com.example.healthtagram.adapter;

import android.app.Activity;
import android.content.Intent;
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
import com.example.healthtagram.activity.TimelineActivity;
import com.example.healthtagram.database.UserPost;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecyclerViewAdapter_grid extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<UserPost> postList = new ArrayList<>();
    private int whatIsTarget = 100;
    private static final int PROFILE = 0;
    private static final int SEARCH = 1;
    private Activity activity;
    private RecyclerView recyclerView;
    private String uid;
    private Long oldestTimeStamp;
    private TextView postNumber;
    private FirebaseFirestore firestore;

    /**
     *
        THIS ADAPTER IS FOR PROFILE FRAGMENT
     */
    public RecyclerViewAdapter_grid(String uid, TextView postNumber, Activity activity, RecyclerView recyclerView){
        this.uid=uid;this.activity=activity; this.recyclerView = recyclerView; this.postNumber=postNumber;
        whatIsTarget=PROFILE;   firestore=FirebaseFirestore.getInstance();
        firestore.collection("posts").whereEqualTo("uid",uid).orderBy("timestamp",Query.Direction.DESCENDING).limit(12).addSnapshotListener(postListener);
        recyclerView.addOnScrollListener(onScrollListener);
    }

    public void setPostNumber(){
        postNumber.setText((postList.size())+"");
    }

    /**
     *
     THIS ADAPTER IS FOR SEARCH FRAGMENT
     */
    public RecyclerViewAdapter_grid(Activity activity, RecyclerView recyclerView){
        this.activity=activity; this.recyclerView=recyclerView; whatIsTarget=SEARCH;
        firestore=FirebaseFirestore.getInstance();
        firestore.collection("posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(12).addSnapshotListener(postListener);
        recyclerView.addOnScrollListener(onScrollListener);
    }

    private EventListener<QuerySnapshot> postListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            int index = 0;
            for (DocumentChange dc : value.getDocumentChanges()) { //https://stackoverflow.com/questions/53439196/firestore-query-listener 바뀐 데이터 스냅샷만 가져오기
                UserPost item = dc.getDocument().toObject(UserPost.class);
                switch (dc.getType()) {
                    case ADDED:
                        Log.e("/0:HOME,1:TIME", dc.getDocument().getId() + " => add");
                        postList.add(postList.size(), item);
                        notifyItemChanged(postList.size());
                        break;
                    case MODIFIED:
                        Log.e("/0:HOME,1:TIME", dc.getDocument().getId() + " => modi");
                        for (UserPost post : postList) {
                            if (post.getTimestamp().equals(item.getTimestamp()))
                                break;
                            index++;
                        }
                        Log.e("/0:HOME,1:TIME", index + " => modi");
                        postList.set(index, item);
                        notifyItemChanged(index);
                        recyclerView.smoothScrollToPosition(index);
                        break;
                    case REMOVED:
                        Log.e("/0:HOME,1:TIME", dc.getDocument().getId() + " => remove");
                        postList.remove(item);
                        notifyDataSetChanged();
                        break;
                }
            }
            if (postList.size() > 0)
                oldestTimeStamp = postList.get(postList.size() - 1).getTimestamp();
        }
    };

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
            imageView.setOnClickListener(onClickListener);
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭시 해당 유저의 포스트들만 homefragment처럼 보며줌 프로필바텀아이템 클릭상태
                Intent intent = new Intent(activity, TimelineActivity.class);
                intent.putExtra("uid",postList.get(getAdapterPosition()).getUid());
                intent.putExtra("timestamp",postList.get(getAdapterPosition()).getTimestamp());
                activity.startActivity(intent);
            }
        };
    }

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!recyclerView.canScrollVertically(1) && dy > 0) {
                if (whatIsTarget == PROFILE)
                    FirebaseFirestore.getInstance().collection("posts").whereEqualTo("uid", uid)
                            .whereLessThan("timestamp", oldestTimeStamp)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(12).addSnapshotListener(postListener);
                else if (whatIsTarget == SEARCH)
                    FirebaseFirestore.getInstance().collection("posts").whereLessThan("timestamp", oldestTimeStamp)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(12).addSnapshotListener(postListener);
            }
        }
    };
}
