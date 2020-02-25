package com.example.healthtagram.crop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthtagram.R;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by Ruily on 16/1/5.
 */
public class CropImageActivity extends AppCompatActivity {
    private CropImageView cropImageView;
    private TextView cropTitle;
    public static final int SQUARE =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_activity);
        cropTitle=findViewById(R.id.cropTitle);
        cropImageView=findViewById(R.id.cropImageView);
        Intent intent = getIntent();
        if(intent.getExtras().getInt("style")==SQUARE) {
            cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
            cropTitle.setText("이미지 설정");
        }else{
            cropImageView.setCropShape(CropImageView.CropShape.OVAL);
        }
        if(intent.getData()!=null)
            cropImageView.setImageUriAsync(intent.getData());

        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                Intent intent = new Intent();
                Log.e("preData",result.getUri()+"");
                intent.setData(result.getUri());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        findViewById(R.id.close_btn).setOnClickListener(onClickListener);
        findViewById(R.id.confirm_btn).setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.close_btn:
                    finish();
                    break;
                case R.id.confirm_btn:
                    Intent intent = new Intent();
                    intent.setData(getImageUri(getApplicationContext(),cropImageView.getCroppedImage()));
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
            }
        }
    };

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

}

