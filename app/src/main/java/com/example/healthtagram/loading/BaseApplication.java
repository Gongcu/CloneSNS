package com.example.healthtagram.loading;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatDialog;

import com.example.healthtagram.R;

/**
 * Created by TedPark on 2017. 3. 18..
 */

public class BaseApplication extends Application {

    private static BaseApplication baseApplication;
    AppCompatDialog progressDialog;

    public static BaseApplication getInstance() {
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }

    public void progressON(Activity activity) {

        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
        } else {
            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.progress_loading);
            progressDialog.show();
        }


        final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
        img_loading_frame.post(new Runnable() {
            @Override
            public void run() {
                frameAnimation.start();
            }
        });



    }

    public void progressOFF() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}