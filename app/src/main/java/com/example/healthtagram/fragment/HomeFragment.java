package com.example.healthtagram.fragment;


import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.healthtagram.R;
import com.example.healthtagram.database.UserPost;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final String TAG = "HOMEFRAGMENT";
    private FirebaseFirestore firebaseStore;
    private RecyclerView recyclerView;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_post, container, false);
        // Inflate the layout for this fragment
        firebaseStore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.post_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new PostRecyclerViewAdapter());

        return view;
    }


    public class PostRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        private ArrayList<UserPost> postList = new ArrayList<>();
        private ArrayList<String> uidList = new ArrayList<>();

        PostRecyclerViewAdapter() {
            firebaseStore.collection("posts").orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    for (QueryDocumentSnapshot doc : value) {
                        UserPost item = doc.toObject(UserPost.class);
                        postList.add(item);
                        uidList.add(doc.getId());
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            //이미지 로딩 라이브러리 glide
            Glide.with(holder.itemView.getContext()).load(Uri.parse(postList.get(position).getPhoto())).into(holder.postImageView);

            holder.nameTextView.setText(postList.get(position).getUserId());
            holder.explainTextView.setText(postList.get(position).getText());
            holder.favoriteCountTextView.setText("Likes "+postList.get(position).getFavoriteCount());
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }
    }
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        private ImageView profileImageView;
        private ImageView postImageView;
        private TextView nameTextView;
        private TextView favoriteCountTextView;
        private TextView explainTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.detail_view_item_image);
            profileImageView = itemView.findViewById(R.id.detail_view_profile_image);
            nameTextView = itemView.findViewById(R.id.detail_view_profile_name);
            favoriteCountTextView = itemView.findViewById(R.id.detail_view_favorite_count);
            explainTextView = itemView.findViewById(R.id.detail_view_explain);
        }

    }
}
