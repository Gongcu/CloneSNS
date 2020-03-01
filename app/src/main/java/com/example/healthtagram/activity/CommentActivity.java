package com.example.healthtagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.healthtagram.R;
import com.example.healthtagram.RecyclerViewAdapter.RecyclerViewAdapter_comment;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.Comment;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.loading.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;


public class CommentActivity extends BaseActivity {
    public static final String TAG ="CommentActivity";
    private FirebaseUser user;
    private Intent intent;
    private String filename;
    private String name;
    private String explain;
    private String profile;
    private String destinationUid;
    private String currentUserProfile="";
    private String currentUserName="";

    private ImageView post_owner_profile;
    private TextView post_owner_name;
    private TextView post_owner_explain;

    private ImageView write_commnet_profile;
    private EditText commentEditText;
    private TextView commentUploadBtn;
    private Comment comment;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter_comment adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        user= FirebaseAuth.getInstance().getCurrentUser();

        post_owner_explain=findViewById(R.id.post_owner_explain);
        post_owner_name=findViewById(R.id.post_owner_name);
        post_owner_profile=findViewById(R.id.post_owner_profile);
        write_commnet_profile = findViewById(R.id.write_comment_profile);
        commentEditText = findViewById(R.id.commentEditText);
        commentUploadBtn = findViewById(R.id.comment_upload_btn);
        recyclerView = findViewById(R.id.commentRecyclerView);

        intent = getIntent();
        init();

        adapter = new RecyclerViewAdapter_comment(filename);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        commentUploadBtn.setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.comment_upload_btn:
                    commentUpload();
                    break;
            }
        }
    };
    private void commentUpload(){
        comment = new Comment(user.getUid(),user.getEmail(),currentUserName,currentUserProfile,commentEditText.getText().toString(),System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("posts").document(filename).collection("comments").document().set(comment);
        commentAlarm(destinationUid,commentEditText.getText().toString());
        commentEditText.setText("");
    }

    private void init(){
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        progressON();
        filename = intent.getExtras().getString("filename");
        firestore.collection("posts").document(filename).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    UserPost post = task.getResult().toObject(UserPost.class);
                    explain = post.getText();
                    destinationUid = post.getUid();
                    firestore.collection("users").document(destinationUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                UserData user = task.getResult().toObject(UserData.class);
                                name = user.getUserName();
                                profile = user.getProfile();
                                Glide.with(getApplicationContext()).load(Uri.parse(profile)).error(R.drawable.main_profile).listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        post_owner_name.setText(name);
                                        post_owner_explain.setText(explain);
                                        recyclerView.setAdapter(adapter);
                                        progressOFF();
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        post_owner_name.setText(name);
                                        post_owner_explain.setText(explain);
                                        recyclerView.setAdapter(adapter);
                                        progressOFF();
                                        return false;
                                    }
                                }).into(post_owner_profile);
                            }
                        }
                    });
                }
            }
        });


        //댓글 달기 프로필 설정
        firestore.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if(userData.getProfile()!=null) {
                    currentUserProfile = userData.getProfile();
                    Glide.with(getApplicationContext()).load(Uri.parse(currentUserProfile)).error(R.drawable.main_profile).into(write_commnet_profile);
                }
                if(userData.getUserName()!=null)
                    currentUserName=userData.getUserName();
            }
        });
    }

    private void commentAlarm(String destinationUid,String message){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AlarmData alarmData = new AlarmData(user.getEmail(),user.getUid(),currentUserName,currentUserProfile,destinationUid,1,message,System.currentTimeMillis(),filename);
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmData);
    }
}
