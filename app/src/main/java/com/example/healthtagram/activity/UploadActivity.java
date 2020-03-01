package com.example.healthtagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.healthtagram.R;
import com.example.healthtagram.database.UserData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.loading.BaseActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadActivity extends BaseActivity {
    private static final int FROM_ALBUM = 100;
    private static final int REQUEST_CROP = 22;
    private Uri selectedImageUri;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private CropImageView imageView;
    private ImageView getPhotoBtn;
    private Button  confirmBtn, closeBtn;
    private EditText textInputEditText;

    private String username="";
    private String userProfile="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        progressON();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userItemInit();
        imageView = findViewById(R.id.imageView);
        imageView.setCropShape(CropImageView.CropShape.RECTANGLE);

        getPhotoBtn = findViewById(R.id.pickPhoto);
        confirmBtn = findViewById(R.id.confirm_btn);
        closeBtn = findViewById(R.id.close_btn);
        textInputEditText = findViewById(R.id.textInput);

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
            }
        }
    };

    // 여러 이미지 겟 intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    private void getPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }

    private void uploadPost(){
        progressON();
        final String text = textInputEditText.getText().toString();
        final Long time = System.currentTimeMillis();
        final String filename = ""+user.getUid()+"_"+time;
        StorageReference storageRef = storage.getReference();
        final StorageReference ImagesRef = storageRef.child("posts/"+filename+".jpg");
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getCroppedImage();
        //Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = ImagesRef.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return ImagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    selectedImageUri = task.getResult();
                    if (selectedImageUri != null && !text.equals("")){
                        firestore.collection("posts").document(filename)
                                .set(new UserPost(selectedImageUri.toString(), text,time,user.getUid(),user.getEmail(),username,userProfile))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressOFF();
                                        Toast.makeText(UploadActivity.this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressOFF();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FROM_ALBUM && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if(data.getData()!=null) {
                imageView.setImageUriAsync(data.getData());
                selectedImageUri=data.getData();
            }
        }
        else if(requestCode == REQUEST_CROP && resultCode == RESULT_OK){
            //imageView.setImageURI(data.getData());
            selectedImageUri=data.getData();
        }
        else{
            Log.e("request, result",requestCode+"  "+resultCode);
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
                username=userData.getUserName();
                userProfile=userData.getProfile();
                progressOFF();
            }
        });
    }
}
