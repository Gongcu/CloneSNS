package com.example.healthtagram.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.healthtagram.R;
import com.example.healthtagram.RecyclerViewAdapter.RecyclerViewAdapter_comment;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.Comment;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.loading.BaseActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class CommentActivity extends BaseActivity {
    private FirebaseUser user;
    private String filename;
    private String name;
    private String explain;
    private String profile;
    private String destinationUid;

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

        init();

        adapter = new RecyclerViewAdapter_comment(filename);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
        comment = new Comment(user.getUid(),user.getEmail(),commentEditText.getText().toString(),System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("posts").document(filename).collection("comments").document().set(comment);
        commentAlarm(destinationUid,commentEditText.getText().toString());
        commentEditText.setText("");
    }
    private void init(){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        filename = intent.getExtras().getString("filename");
        name=intent.getExtras().getString("name");
        explain=intent.getExtras().getString("explain");
        profile=intent.getExtras().getString("profile");
        destinationUid=intent.getExtras().getString("destinationUid");


        Glide.with(this).load(Uri.parse(profile)).into(post_owner_profile);
        post_owner_name.setText(name);
        post_owner_explain.setText(explain);

        firestore.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if(userData.getProfile()!=null)
                    Glide.with(getApplicationContext()).load(Uri.parse(userData.getProfile())).error(R.drawable.main_profile).into(write_commnet_profile);
            }
        });
    }

    private void commentAlarm(String destinationUid,String message){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AlarmData alarmData = new AlarmData(user.getEmail(),user.getUid(),destinationUid,1,message,System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmData);
    }
}
