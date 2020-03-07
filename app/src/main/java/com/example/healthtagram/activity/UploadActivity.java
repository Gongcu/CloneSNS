package com.example.healthtagram.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.healthtagram.R;
import com.example.healthtagram.adapter.ViewPageAdapter_upload;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPostTest;
import com.example.healthtagram.loading.BaseActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UploadActivity extends BaseActivity {
    private static final int FROM_ALBUM = 100;
    private static final int REQUEST_CROP = 22;
    private ArrayList<String> selectedImageUris = new ArrayList<>();
    private Uri selectedImageUri;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    //private CropImageView imageView;
    private ViewPager viewpager;
    private ImageView getPhotoBtn, nextPhotoBtn,prevPhotoBtn;
    private Button  confirmBtn, closeBtn;
    private EditText textInputEditText;
    private ArrayList<Uri> path = new ArrayList<>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private String username="";
    private String userProfile="";
    private ViewPageAdapter_upload pageAdapter;
    private int currentPosition =0;
    private int i;
    private int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        progressON();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userItemInit();

        /*
        imageView = findViewById(R.id.imageView);
        imageView.setCropShape(CropImageView.CropShape.RECTANGLE);*/
        viewpager = findViewById(R.id.view_pager);
        nextPhotoBtn = findViewById(R.id.nextPhoto);
        prevPhotoBtn = findViewById(R.id.prevPhoto);
        getPhotoBtn = findViewById(R.id.pickPhoto);
        confirmBtn = findViewById(R.id.confirm_btn);
        closeBtn = findViewById(R.id.close_btn);
        textInputEditText = findViewById(R.id.textInput);

        pageAdapter = new ViewPageAdapter_upload(this);
        viewpager.setOffscreenPageLimit(4);
        viewpager.setAdapter(pageAdapter);
        prevPhotoBtn.setOnClickListener(onClickListener);
        nextPhotoBtn.setOnClickListener(onClickListener);
        closeBtn.setOnClickListener(onClickListener);
        confirmBtn.setOnClickListener(onClickListener);
        getPhotoBtn.setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pickPhoto:
                    getPicture();
                    break;
                case R.id.close_btn:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case R.id.confirm_btn:
                    uploadPost();
                    break;
                case R.id.nextPhoto:
                    currentPosition=(currentPosition+1)%3;
                    viewpager.setCurrentItem(currentPosition,true);
                    break;
                case R.id.prevPhoto:
                    if(currentPosition==0)
                        currentPosition=2;
                    else
                        currentPosition=(currentPosition-1)%3;
                    viewpager.setCurrentItem(currentPosition,true);
                    break;
            }
        }
    };

    private void getPicture() {
        FishBun.with(this)
                .setImageAdapter(new GlideAdapter())
                .setActionBarColor(Color.parseColor("#ffffff"), Color.parseColor("#ffffff"), true)
                .setActionBarTitleColor(Color.parseColor("#000000"))
                .setIsUseDetailView(false)
                .setMaxCount(5)
                .setMinCount(1)
                .exceptGif(true)
                .setHomeAsUpIndicatorDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp))
                .setDoneButtonDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_black_24dp))
                .setActionBarTitle("이미지 선택")
                .textOnImagesSelectionLimitReached("이미지는 5장이 최대입니다.")
                .textOnNothingSelected("이미지를 선택해주세요.")
                .startAlbum();
    }

    private void uploadPost(){
        progressON();
        final String text = textInputEditText.getText().toString();
        final Long time = System.currentTimeMillis();
        final String filename = user.getUid()+"_"+time;
        StorageReference storageRef = storage.getReference();
        final ArrayList<StorageReference> ImagesRefs=new ArrayList<>();
        for(int i =0; i<path.size(); i++){
            ImagesRefs.add(storageRef.child("posts/"+filename+String.valueOf(i)+".jpg"));
        }
        bitmaps = pageAdapter.getBitmapImages();
        Log.e("bitmaps size",bitmaps.size()+"");
        if(bitmaps.size()==0 || text.equals("")){
            Toast.makeText(UploadActivity.this, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
            progressOFF();
            return;
        }
        ArrayList<ByteArrayOutputStream> baos = new ArrayList<>();
        ArrayList<byte[]> data = new ArrayList<>();
        UploadTask uploadTask;
        for(int i=0; i<bitmaps.size(); i++) {
            baos.add(new ByteArrayOutputStream());
            bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 20,baos.get(i));
            data.add(baos.get(i).toByteArray());
            uploadTask=ImagesRefs.get(i).putBytes(data.get(i));
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ImagesRefs.get(count++).getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                        selectedImageUris.add(task.getResult().toString());
                    if (selectedImageUris.size() == bitmaps.size()) {
                        firestore.collection("posts").document(filename)
                                .set(new UserPostTest(selectedImageUris, text, time, user.getUid(), user.getEmail(), username, userProfile))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(UploadActivity.this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        progressOFF();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(UploadActivity.this, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                                        progressOFF();
                                    }
                                });
                    }
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Define.ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    path.clear();
                    path = data.getParcelableArrayListExtra(Define.INTENT_PATH);
                    try {
                        for (int i = 0; i < path.size(); i++) {
                            bitmaps.add(MediaStore.Images.Media.getBitmap(this.getContentResolver(), path.get(i)));
                        }
                    }catch (IOException e){e.printStackTrace();}
                    pageAdapter.setImages(path);
                    break;
                }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
        finish();
    }

    private void userItemInit(){
        firestore.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if(userData!=null) {
                    username = userData.getUserName();
                    userProfile = userData.getProfile();
                }
                progressOFF();
            }
        });
    }
}
