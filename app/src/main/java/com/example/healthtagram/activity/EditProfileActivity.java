package com.example.healthtagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.healthtagram.R;
import com.example.healthtagram.crop.CropImageActivity;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.loading.BaseActivity;
import com.example.healthtagram.database.UserData;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class EditProfileActivity extends BaseActivity {
    private boolean FIRST_ACCESS = false;
    private LinearLayout mainLayout;
    private EditText nickName, introduction;
    private ImageView profilePicture;
    private static final int OVAL = 0;
    private static final int FROM_ALBUM = 100;
    private static final int REQUEST_CROP = 22;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser user;
    private int state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        progressON();

        storage = FirebaseStorage.getInstance();
        mainLayout = findViewById(R.id.main_layout);
        profilePicture = findViewById(R.id.profilePicture);
        nickName = findViewById(R.id.nicknameEditText);
        introduction = findViewById(R.id.introEditText);

        Intent intent = getIntent();
        state = intent.getExtras().getInt("state");

        init(); //기존 값을 플레이팅
        findViewById(R.id.close_btn).setOnClickListener(onClickListener);
        findViewById(R.id.confirm_btn).setOnClickListener(onClickListener);
        findViewById(R.id.profile_change_TextView).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.close_btn:
                    if(FIRST_ACCESS)
                        Snackbar.make(mainLayout,"프로필 입력을 완료하세요.", Snackbar.LENGTH_LONG).show();
                    else
                        finish();
                    break;
                case R.id.confirm_btn:
                    edit_profile();
                    //리스너 달아서 프래그먼트에 다시 로딩 시키기
                    break;
                case R.id.profile_change_TextView:
                    getPicture();
            }
        }
    };

    @Override
    public void onBackPressed() {
        if(FIRST_ACCESS)
            Snackbar.make(mainLayout,"프로필 입력을 완료하세요.", Snackbar.LENGTH_LONG).show();
        else
            super.onBackPressed();
    }

    private void edit_profile() {
        progressON();
        final String username = ((EditText) findViewById(R.id.nicknameEditText)).getText().toString();
        final String bio = ((EditText) findViewById(R.id.introEditText)).getText().toString();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Access a Cloud Firestore instance from your Activity
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (username.equals("") || bio.equals("")) {
            Toast.makeText(this, getString(R.string.edit_profile_fail), Toast.LENGTH_LONG).show();
            progressOFF();
            return;
        }
        /**이미지 업로드 없이 프로필 편집할 경우*/
        if (selectedImageUri == null) {
            final UserData userData = new UserData(username, "", bio);
            db.collection("users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DataChanger(user.getUid(),username,"");
                            Toast.makeText(EditProfileActivity.this, getString(R.string.edit_profile_success), Toast.LENGTH_SHORT).show();
                            ((MainActivity) MainActivity.context).callFragmentUpdateMethod(state);
                            finish();
                            progressOFF();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
        /** 이미지 업로드 후 프로필 편집할 경우*/
        StorageReference storageRef = storage.getReference();
        final StorageReference ImagesRef = storageRef.child("profiles/" + user.getUid() + ".jpg");
        profilePicture.setDrawingCacheEnabled(true);
        profilePicture.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = ImagesRef.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return ImagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    selectedImageUri = task.getResult();
                    final UserData userData = new UserData(username, selectedImageUri.toString(), bio);
                    if (user != null) {
                        db.collection("users").document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        DataChanger(user.getUid(),username,userData.getProfile());
                                        Toast.makeText(EditProfileActivity.this, getString(R.string.edit_profile_success), Toast.LENGTH_SHORT).show();
                                        ((MainActivity) MainActivity.context).callFragmentUpdateMethod(state);
                                        finish();
                                        progressOFF();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    //onSuccess : a task  onComplete : the task
    //posts,alarms 콜렉션의 이미지, 닉네임을 교체
    private void DataChanger(final String uid, final String name, final String profile){
        db.collection("posts").whereEqualTo("uid",uid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot item : task.getResult().getDocuments()){
                        UserPost post = item.toObject(UserPost.class);
                        post.setUserName(name);
                        post.setUserProfile(profile);
                        db.collection("posts").document(post.getUid()+"_"+post.getTimestamp()).set(post);
                    }
                }
            }
        });
        db.collection("alarms").whereEqualTo("uid",uid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot item : task.getResult().getDocuments()){
                        AlarmData alarm = item.toObject(AlarmData.class);
                        alarm.setUsername(name);
                        alarm.setUserProfile(profile);
                        db.collection("alarms").document(alarm.getUid()+"_"+alarm.getTimestamp()).set(alarm);
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FROM_ALBUM && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null)
                goCrop(selectedImageUri);

        } else if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
            profilePicture.setImageURI(data.getData());
        } else {
            Log.e("request, result", requestCode + "  " + resultCode);
        }
    }

    private void getPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }


    private void goCrop(Uri sourUri) {
        Intent intent = new Intent(EditProfileActivity.this, CropImageActivity.class);
        intent.setData(sourUri);
        intent.putExtra("style", OVAL);
        startActivityForResult(intent, REQUEST_CROP);
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                Log.e("success","aa");
                if (userData != null) {
                    nickName.setText(userData.getUserName());
                    introduction.setText(userData.getBio());
                    if (!userData.getProfile().equals(""))
                        profilePicture.setImageResource(R.drawable.main_profile);
                    else
                        Glide.with(getApplicationContext()).load(Uri.parse(userData.getProfile()));
                } else{
                    //when userData is null -> cannot operate back press btn
                    FIRST_ACCESS=true;
                }
                progressOFF();
            }
        });
    }

}

