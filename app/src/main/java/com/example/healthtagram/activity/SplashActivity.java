package com.example.healthtagram.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthtagram.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new splashHandler(),1000);
    }

    private class splashHandler implements Runnable{
        @Override
        public void run() {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            SplashActivity.this.finish();
        }
    }

    @Override
    public void onBackPressed(){
        //스플래시 화면에서 뒤로가기 불가
    }
}
