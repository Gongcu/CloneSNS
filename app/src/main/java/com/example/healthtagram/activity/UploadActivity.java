package com.example.healthtagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.healthtagram.R;
import com.example.healthtagram.crop.CropImageActivity;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.fragment.HomeFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadActivity extends AppCompatActivity {
    private static final int FROM_ALBUM = 100;
    private static final int REQUEST_CROP = 22;
    private Uri selectedImageUri;
    private FirebaseUser user;
    private DatabaseReference mDatabase;// ...
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ImageView imageView;
    private Button photoBtn, confirmBtn, closeBtn;
    private TextInputEditText textInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        imageView = findViewById(R.id.imageView);
        photoBtn = findViewById(R.id.pickPhoto);
        confirmBtn = findViewById(R.id.confirm_btn);
        closeBtn = findViewById(R.id.close_btn);
        textInputEditText = findViewById(R.id.textInput);

        closeBtn.setOnClickListener(onClickListener);
        confirmBtn.setOnClickListener(onClickListener);
        photoBtn.setOnClickListener(onClickListener);
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
                    setResult(RESULT_OK);
                    finish();
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        String text = textInputEditText.getText().toString();
        String filename = ""+user.getUid()+sdf.format(date);

        StorageReference storageRef = storage.getReference();
        final StorageReference ImagesRef = storageRef.child("posts/"+filename+".jpg");
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
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
                return ImagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    selectedImageUri = task.getResult();
                } else {
                    // Handle failures
                    // ...
                }
            }
        });

        if (selectedImageUri != null && !text.equals("")){
            firestore.collection("posts").document(filename)
                    .set(new UserPost(selectedImageUri.toString(), text,System.currentTimeMillis(),user.getUid(),user.getEmail()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UploadActivity.this, "프로필 편집 성공", Toast.LENGTH_SHORT).show();
                            Log.e("업로드","성공");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("업로드","실패");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FROM_ALBUM && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if(data.getData()!=null)
                goCrop(data.getData());
        }
        else if(requestCode == REQUEST_CROP && resultCode == RESULT_OK){
            imageView.setImageURI(data.getData());
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

    private void goCrop(Uri sourUri) {
        final int SQUARE  =1;
        Intent intent = new Intent(UploadActivity.this, CropImageActivity.class);
        intent.setData(sourUri);
        intent.putExtra("style",SQUARE);
        startActivityForResult(intent, REQUEST_CROP);
    }
}
