package com.example.healthtagram.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.healthtagram.R;
import com.example.healthtagram.database.UserPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment  {
    private static final int FROM_ALBUM = 100;
    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;
    private HomeFragment fragmentHome = new HomeFragment();
    private Uri selectedImageUri;
    private FirebaseUser user;
    private DatabaseReference mDatabase;// ...
    private FirebaseStorage storage;
    private ImageView imageView;
    private Button photoBtn, confirmBtn, cancleBtn;
    private TextInputLayout textInputLayout;

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        fragmentManager = getActivity().getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();

        // 여러 이미지 겟 intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imageView = view.findViewById(R.id.imageView);
        photoBtn = view.findViewById(R.id.pickPhoto);
        confirmBtn = view.findViewById(R.id.confirm_btn);
        cancleBtn = view.findViewById(R.id.btn_cancle);
        textInputLayout = view.findViewById(R.id.textInput);

        photoBtn.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pickPhoto:
                    getPicture();
                    break;
                case R.id.btn_cancle:
                    transaction.replace(R.id.frameLayout, fragmentHome).commitAllowingStateLoss();
                    break;
                case R.id.confirm_btn:

                    transaction.replace(R.id.frameLayout, fragmentHome).commitAllowingStateLoss();
                    break;
            }
        }
    };

    private void getPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FROM_ALBUM && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            if(selectedImageUri!=null)
                imageView.setImageURI(selectedImageUri);
        }
        else{
            Log.e("request, result",requestCode+"  "+resultCode);
        }
    }
}
