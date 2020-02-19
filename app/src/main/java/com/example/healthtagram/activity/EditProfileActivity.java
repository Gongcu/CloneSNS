package com.example.healthtagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.healthtagram.R;
import com.example.healthtagram.crop.CropImageActivity;
import com.example.healthtagram.fragment.ProfileFragment;
import com.example.healthtagram.listener.EditProfileListener;
import com.example.healthtagram.loading.BaseActivity;
import com.example.healthtagram.loading.BaseApplication;
import com.example.healthtagram.database.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends BaseActivity {
    private EditProfileListener editProfileListener;
    private EditText nickName, introduction;
    private ImageView profilePicture;
    private static final int OVAL = 0;
    private static final int FROM_ALBUM = 100;
    private static final int REQUEST_CROP = 22;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        progressON();
        profilePicture = findViewById(R.id.profilePicture);
        nickName = findViewById(R.id.nicknameEditText);
        introduction = findViewById(R.id.introEditText);

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
                    finish();
                    break;
                case R.id.confirm_btn:
                    edit_profile();
                    ((MainActivity)MainActivity.context).callFragmentUdateMethod();
                    finish();
                    //리스너 달아서 프래그먼트에 다시 로딩 시키기
                    break;
                case R.id.profile_change_TextView:
                    getPicture();
            }
        }
    };

    private void edit_profile() {
        UserData userData;
        String nickname = ((EditText) findViewById(R.id.nicknameEditText)).getText().toString();
        String bio = ((EditText) findViewById(R.id.introEditText)).getText().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(selectedImageUri==null)
            userData = new UserData(nickname, "", bio);
        else
            userData = new UserData(nickname, selectedImageUri.toString(), bio);
        if(user!=null) {
            db.collection("users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditProfileActivity.this, "프로필 편집 성공", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FROM_ALBUM && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            if(selectedImageUri!=null)
                goCrop(selectedImageUri);

        }
        else if(requestCode == REQUEST_CROP && resultCode == RESULT_OK){
            profilePicture.setImageURI(data.getData());
        }
        else{
            Log.e("request, result",requestCode+"  "+resultCode);
        }
    }

    private void getPicture(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }


    private void goCrop(Uri sourUri) {
        Intent intent = new Intent(EditProfileActivity.this, CropImageActivity.class);
        intent.setData(sourUri);
        intent.putExtra("style",OVAL);
        startActivityForResult(intent, REQUEST_CROP);
    }

    private void init(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if(userData!=null) {
                    nickName.setText(userData.getUserName());
                    introduction.setText(userData.getBio());
                    if (!userData.getProfile().equals(""))
                        profilePicture.setImageURI(Uri.parse(userData.getProfile()));
                }
                progressOFF();
            }
        });
        //progressOFF();
    }
    public void setEditProfileListener(EditProfileListener listener){
        this.editProfileListener = listener;
    }
}

